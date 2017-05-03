package seminar6;

import java.io.*;
import javax.jms.*;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import lpi.server.mq.FileInfo;

public class CommandProcessing {

    private static final String PING_QUEUE = "chat.diag.ping";
    private static final String ECHO_QUEUE = "chat.diag.echo";
    private static final String LOGIN_QUEUE = "chat.login";
    private static final String LIST_USERS_QUEUE = "chat.listUsers";
    private static final String SEND_MESSAGE_QUEUE = "chat.sendMessage";
    private static final String MESSAGES_QUEUE = "chat.messages";
    private static final String SEND_FILE_QUEUE = "chat.sendFile";
    private static final String FILES_QUEUE = "chat.files";
    private static final String EXIT_QUEUE = "chat.exit";

    private final int ECHO_VALID_PARAMETERS_LENGTH = 2;
    private final int LOGIN_VALID_PARAMETERS_LENGTH = 3;
    private final int MESSAGE_VALID_PARAMETERS_LENGTH = 3;
    private final int FILE_VALID_PARAMETERS_LENGTH = 3;

    private final Session session, sessionReceiveMess, sessionReceiveFile;
    private MessageReceiver mesReceive;
    private FileReceiver fileReceive;

    public CommandProcessing(List<Session> sessions) {
        this.session = sessions.get(0);
        this.sessionReceiveMess = sessions.get(1);
        this.sessionReceiveFile = sessions.get(2);
    }

    private boolean islogged = false;
    private String myLogin;
    public static Instant timestamp;

    public void ping() {
        try {
            Message msg = session.createMessage();
            Message ms = contactServer(msg, PING_QUEUE);

            responsAnalize(ms instanceof Message, "Ping succesfull",
                    "Unexpected message type: " + msg.getClass());

        } catch (JMSException ex) {
            System.out.println("connections problem");
        }
    }

    public void echo(String[] comandMas) {
        if (!isValidNumberOfParameter(comandMas.length, ECHO_VALID_PARAMETERS_LENGTH)) {
            return;
        }
        try {
            String echoText = comandMas[1];

            TextMessage msg = session.createTextMessage(echoText);
            Message ms = contactServer(msg, ECHO_QUEUE);

            responsAnalize(ms instanceof TextMessage, ((TextMessage) ms).getText(),
                    "Unexpected message type: " + msg.getClass());

        } catch (JMSException ex) {
            System.out.println("connections problem");
        }
    }

    public void login(String[] comandMas) {
        if (isValidNumberOfParameter(comandMas.length, LOGIN_VALID_PARAMETERS_LENGTH)) {
            String login = comandMas[1];
            String password = comandMas[2];

            try {
                MapMessage request = session.createMapMessage();
                request.setString("login", login);
                request.setString("password", password);
                MapMessage response = (MapMessage) contactServer(request, LOGIN_QUEUE);

                if (responsAnalize(response.getBoolean("success"), "logged in",
                        "Failed to login: " + response.getString("message"))) {
                    setLogin(login);
                    createFileAndMessageReceivers();
                }
            } catch (JMSException ex) {
                System.out.println("connections problem");
            }
        }
    }

    private void setLogin(String login) {
        islogged = true;
        myLogin = login;
    }

    private void createFileAndMessageReceivers() throws JMSException {
        mesReceive = new MessageReceiver(sessionReceiveMess);
        mesReceive.receive(MESSAGES_QUEUE);

        fileReceive = new FileReceiver(sessionReceiveFile);
        fileReceive.receive(FILES_QUEUE);
    }

    public void list() {
        if (isLogged()) {
            try {
                Message request = session.createMapMessage();
                ObjectMessage response = (ObjectMessage) contactServer(request, LIST_USERS_QUEUE);

                Serializable obj = ((ObjectMessage) response).getObject();
                if (obj != null && obj instanceof String[]) {
                    String[] users = (String[]) obj;
                    System.out.println("\nOnline users :");
                    for (String user : users) {
                        System.out.println(user);
                    }
                } else {
                    throw new IOException("Unexpected content: " + obj);
                }
            } catch (JMSException | IOException ex) {
                System.out.println("connections problem");
            }
        }
    }

    public void msg(String[] comandMas) {
        if (!isLogged()) {
            return;
        }
        if (isValidNumberOfParameter(comandMas.length, MESSAGE_VALID_PARAMETERS_LENGTH)) {
            return;
        }
        String receiver = comandMas[1];
        String message = comandMas[2];
        try {
            MapMessage request = session.createMapMessage();
            request.setString("receiver", receiver);
            request.setString("message", message);
            MapMessage response = (MapMessage) contactServer(request, SEND_MESSAGE_QUEUE);

            responsAnalize(response.getBoolean("success"), "Message send",
                    "Failed sending message: " + response.getString("message"));

        } catch (JMSException ex) {
            ex.printStackTrace();
            System.out.println("connections problem");
        }
    }

    public void file(String[] comandMas) {
        if (!isLogged()) {
            return;
        }
        if (isValidNumberOfParameter(comandMas.length, FILE_VALID_PARAMETERS_LENGTH)) {
            return;
        }
        String receiver = comandMas[1];
        String fileName = comandMas[2];
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("No this file");
            return;
        }
        try {
            ObjectMessage request = session.createObjectMessage(new FileInfo(myLogin, receiver,
                    fileName, Files.readAllBytes(file.toPath())));

            MapMessage response = (MapMessage) contactServer(request, SEND_FILE_QUEUE);

            responsAnalize(response.getBoolean("success"), "File send",
                    "Failed sending file: " + response.getString("message"));
        } catch (JMSException | IOException ex) {
            System.out.println("file sending problem");
        }
    }

    public void exit() {
        try {
            if (islogged) {
                mesReceive.exit();
                fileReceive.exit();
            }

            Message msg = session.createMessage();
            Message ms = contactServer(msg, EXIT_QUEUE);

            if (responsAnalize(ms instanceof Message, "Exit gracefull",
                    "Unexpected message type: " + msg.getClass())) {
                Client.flug = false;
            }
        } catch (JMSException ex) {
            System.out.println("connections problem");
        }
    }

    private boolean responsAnalize(boolean instance, String yesMsg, String noMsg) {
        return instance ? isInstance(yesMsg) : noInstance(noMsg);
    }

    private boolean isInstance(String yesMsg) {
        System.out.println(yesMsg);
        return true;
    }

    private boolean noInstance(String noMsg) {
        System.out.println("Unexpected message type: " + noMsg.getClass());
        return false;
    }

    private boolean isLogged() {
        return islogged ? true : falseLogin();
    }

    private boolean falseLogin() {
        System.out.println("Login first");
        return false;
    }

    private boolean isValidNumberOfParameter(int ComandMasLength, int validLength) {
        return ComandMasLength == validLength ? true : falseNumberOfParameter();
    }

    private boolean falseNumberOfParameter() {
        System.out.println("Bad argument");
        return false;
    }

    private Message contactServer(Message request, String broker_URI) {
        MessageProducer producer = null;
        MessageConsumer consumer = null;
        Message mess = null;
        timestamp = Instant.now();

        try {
            Destination targetQueue = session.createQueue(broker_URI);
            Destination replyQueue = session.createTemporaryQueue();

            producer = session.createProducer(targetQueue);
            consumer = session.createConsumer(replyQueue);

            request.setJMSReplyTo(replyQueue);
            producer.send(request);
            mess = consumer.receive(1500);

        } catch (JMSException ex) {
            ex.printStackTrace();
        } finally {
            close_Prod_Cons(producer, consumer);
        }
        return mess;
    }

    private void close_Prod_Cons(MessageProducer producer, MessageConsumer consumer) {
        try {
            if (producer != null) {
                producer.close();
            }
            if (consumer != null) {
                consumer.close();
            }
        } catch (Exception ex) {
            System.out.println("Closing producer/consumer problem");
        }
    }
}
