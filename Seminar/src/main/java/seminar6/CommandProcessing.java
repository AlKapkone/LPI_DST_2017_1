package seminar6;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.time.Instant;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import lpi.server.mq.FileInfo;

public class CommandProcessing {

    private final javax.jms.Session session, sessionReceiveMess, sessionReceiveFile;
    MessageReceiver mesReceive;
    FileReceiver fileReceive;

    public CommandProcessing(Session session, Session sessionReceiveMess, Session sessionReceiveFile) {
        this.session = session;
        this.sessionReceiveMess = sessionReceiveMess;
        this.sessionReceiveFile = sessionReceiveFile;
    }

    private boolean islogged = false;
    private String myLogin;
    public static Instant timestamp;

    public void ping() {

        try {
            javax.jms.Message msg = session.createMessage();

            javax.jms.Message ms = contactServer(msg, "chat.diag.ping");

            if (ms instanceof javax.jms.Message) {
                System.out.println("Ping succesfull");
            } else {
                throw new IOException("Unexpected message type: " + msg.getClass());
            }
        } catch (Exception ex) {
            System.out.println("connections problem");
        }
    }

    public void echo(String[] comandMas) {

        if (comandMas.length == 1) {
            System.out.println("Pleas enter text");
            return;
        }

        try {
            javax.jms.TextMessage msg = session.createTextMessage(comandMas[1]);

            javax.jms.Message ms = contactServer(msg, "chat.diag.echo");

            if (ms instanceof javax.jms.TextMessage) {
                System.out.println(((javax.jms.TextMessage) ms).getText());
            } else {
                throw new IOException("Unexpected message type: " + msg.getClass());
            }

        } catch (JMSException | IOException ex) {
            System.out.println("connections problem");
        }
    }

    public void login(String[] comandMas) {
        if (comandMas.length != 3) {
            System.out.println("Bad argument");
            return;
        }

        try {
            javax.jms.MapMessage request = session.createMapMessage();

            request.setString("login", comandMas[1]);
            request.setString("password", comandMas[2]);

            javax.jms.MapMessage response = (javax.jms.MapMessage) contactServer(request, "chat.login");

            if (response.getBoolean("success")) {
                System.out.println("logged in");
                
                islogged = true;
                myLogin = comandMas[1];
                
                mesReceive = new MessageReceiver(sessionReceiveMess);
                mesReceive.receive("chat.messages");
                
                fileReceive = new FileReceiver(sessionReceiveFile);
                fileReceive.receive("chat.files");

            } else {
                throw new IOException("Failed to login: " + response.getString("message"));
            }
        } catch (JMSException | IOException ex) {
            System.out.println("connections problem");
        }
    }

    public void list() {
        if (!islogged) {
            System.out.println("Login first");
            return;
        }

        try {
            javax.jms.Message request = session.createMapMessage();

            javax.jms.ObjectMessage response = (javax.jms.ObjectMessage) contactServer(request, "chat.listUsers");
            Serializable obj = ((ObjectMessage) response).getObject();
            if (obj != null && obj instanceof String[]) {
                String[] users = (String[]) obj;
                System.out.println("\nOnline users :");
                for (String us : users) {
                    System.out.println(us);
                }
            } else {
                throw new IOException("Unexpected content: " + obj);
            }
        } catch (JMSException | IOException ex) {
            System.out.println("connections problem");
        }
    }

    public void msg(String[] comandMas) {
        if (!islogged) {
            System.out.println("Login first");
            return;
        }

        if (comandMas.length != 3) {
            System.out.println("Bad argument");
            return;
        }

        try {
            javax.jms.MapMessage request = session.createMapMessage();

            request.setString("receiver", comandMas[1]);
            request.setString("message", comandMas[2]);

            javax.jms.MapMessage response = (javax.jms.MapMessage) contactServer(request, "chat.sendMessage");

            if (response.getBoolean("success")) {
                System.out.println("Message send");
            } else {
                System.out.println("Failed sending message: " + response.getString("message"));
            }
        } catch (JMSException ex) {
            ex.printStackTrace();
            System.out.println("connections problem");
        }
    }

    public void file(String[] comandMas) {

        if (!islogged) {
            System.out.println("Login first");
            return;
        }

        if (comandMas.length != 3) {
            System.out.println("Bad argument");
            return;
        }

        File file = new File(comandMas[2]);

        if (!file.exists()) {
            System.out.println("No this file");
            return;
        }

        try {
            javax.jms.ObjectMessage request = session.createObjectMessage(new FileInfo(myLogin, comandMas[1],
                    comandMas[2], Files.readAllBytes(file.toPath())));

            javax.jms.MapMessage response = (javax.jms.MapMessage) contactServer(request, "chat.sendFile");

            if (response.getBoolean("success")) {
                System.out.println("file send");
            } else {
                System.out.println("Failed sending file: " + response.getString("message"));
            }

        } catch (JMSException | IOException ex) {
            System.out.println("file sending problem");
        }
    }
    
    public void exit(){
        try {
            if(islogged){
                mesReceive.exit();
                fileReceive.exit();
            }
            javax.jms.Message msg = session.createMessage();
            javax.jms.Message ms = contactServer(msg, "chat.exit");

            if (ms instanceof javax.jms.Message) {
                Client.flug = false;
                System.out.println("Exit gracefull");
            } else {
                System.out.println("Unexpected message type: " + msg.getClass());
            }
        } catch (JMSException ex) {
            System.out.println("connections problem");
        }
    }
    
    private Message contactServer(Message request, String broker_URI) {
        javax.jms.MessageProducer producer = null;
        javax.jms.MessageConsumer consumer = null;
        javax.jms.Message mess = null;
        timestamp = Instant.now();
        
        try {
            javax.jms.Destination targetQueue = session.createQueue(broker_URI);
            javax.jms.Destination replyQueue = this.session.createTemporaryQueue();

            producer = session.createProducer(targetQueue);
            consumer = session.createConsumer(replyQueue);

            request.setJMSReplyTo(replyQueue);
            producer.send(request);
            mess = consumer.receive(1500);

        } catch (JMSException ex) {
            ex.printStackTrace();
            System.out.println("connections problem");
        } finally {
            close_Prod_Cons(producer, consumer);
        }
        return mess;
    }

    private void close_Prod_Cons(javax.jms.MessageProducer producer,
            javax.jms.MessageConsumer consumer) {

        if (producer == null && consumer == null) {
            return;
        }

        try {
            producer.close();
            consumer.close();
        } catch (Exception ex) {
            System.out.println("Closing producer/consumer problem");
        }
    }
}