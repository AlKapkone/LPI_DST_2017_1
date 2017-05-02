package seminar4;

import java.io.*;
import java.util.*;
import lpi.server.soap.*;
import java.nio.file.Files;
import java.rmi.RemoteException;

public class CommandProcessing {

    private static final int ECHO_PARAMETERS_LENGTH = 2;
    private static final int LOGIN_PARAMETERS_LENGTH = 3;
    private static final int MESSAGE_PARAMETERS_LENGTH = 3;
    private static final int FILE_PARAMETERS_LENGTH = 3;

    private final IChatServer iServ;

    public CommandProcessing(IChatServer iServ) {
        this.iServ = iServ;
    }

    public void ping() {
        try {
            iServ.ping();
            System.out.println("Ping succesfull");
        } catch (Exception ex) {
            System.out.println("connections problem");
        }
    }

    public void echo(String[] comandMas) throws RemoteException {
        try {
            if (isNormalLength(comandMas, ECHO_PARAMETERS_LENGTH)) {
                System.out.println(iServ.echo(comandMas[1]));
            }
        } catch (Exception ex) {
            System.out.println("connections problem");
        }
    }

    private final Timer timer = new Timer();
    private static String myLogin = null;
    private static String sessionId = null;

    public void login(String[] comandMas) throws RemoteException, ArgumentFault, ServerFault, LoginFault {
        if (!isNormalLength(comandMas, LOGIN_PARAMETERS_LENGTH)) {
            return;
        }
        String login = comandMas[1];
        String password = comandMas[2];

        if (login.equals(myLogin)) {
            System.out.println("You are logged");
            return;
        }

        if (sessionId != null) {
            iServ.exit(sessionId);
        }
        sessionId = iServ.login(login, password);
        if (myLogin == null) {
            timer.schedule(receive, 0, 1500);
        }
        myLogin = comandMas[1];
    }

    public void list() throws RemoteException, ArgumentFault, ServerFault {

        if (isLogged(sessionId)) {
            List<String> onlineUser = iServ.listUsers(sessionId);
            if (onlineUser != null) {
                for (String user : onlineUser) {
                    System.out.println(user);
                }
            }
        }
    }

    public void msg(String[] comandMas) throws RemoteException, ArgumentFault, ServerFault {
        if (!isLogged(sessionId) || !isNormalLength(comandMas, MESSAGE_PARAMETERS_LENGTH) || !isItUser(comandMas[1])) {
            return;
        }
        Message mess = new Message();
        mess.setSender(myLogin);
        mess.setReceiver(comandMas[1]);
        mess.setMessage(comandMas[2]);

        iServ.sendMessage(sessionId, mess);
        System.out.println("Message sent");
    }

    public void file(String[] comandMas) throws IOException, ArgumentFault, ServerFault {
        if (!isLogged(sessionId) || !isNormalLength(comandMas, FILE_PARAMETERS_LENGTH) || !isItUser(comandMas[1])) {
            return;
        }
        File file = new File(comandMas[2]);

        if (!file.exists()) {
            System.out.println("No this file");
            return;
        }

        FileInfo fille = new FileInfo();

        fille.setSender(myLogin);
        fille.setReceiver(comandMas[1]);
        fille.setFilename(comandMas[2]);
        fille.setFileContent(Files.readAllBytes(file.toPath()));

        iServ.sendFile(sessionId, fille);
        System.out.println("File sent");
    }

    public void receiveMsg() throws RemoteException, ArgumentFault, ServerFault {
        if (isLogged(sessionId)) {
            Message mess = iServ.receiveMessage(sessionId);
            if (mess == null) {
                if (flug) {
                    System.out.println("No message");
                }
                return;
            }
            System.out.println("Message from: " + mess.getSender() + " : " + mess.getMessage());
        }
    }

    public void receiveFile() throws RemoteException, ArgumentFault, ServerFault {
        if (!isLogged(sessionId)) {
            return;
        }
        FileInfo file = iServ.receiveFile(sessionId);
        if (file == null) {
            if (flug) {
                System.out.println("No file");
            }
            return;
        }
        System.out.println("File from \"" + file.getSender() + "\" file name : \"" + file.getFilename() + "\"");

        try (FileOutputStream fos = new FileOutputStream(
                new File(file.getSender() + "_" + file.getFilename()))) {
            fos.write(file.getFileContent());
        } catch (Exception ex) {
            System.out.println("Problem with write file");
        }
    }

    public void exit() throws RemoteException, ArgumentFault, ServerFault {
        Client.flug = false;
        timer.cancel();
        iServ.exit(sessionId);
        System.out.println("Exit from server");
    }

    private boolean isNormalLength(String[] comandMas, int validLength) {
        return comandMas.length == validLength ? true : badArgument();
    }

    private boolean badArgument() {
        System.out.println("Bad argument");
        return false;
    }

    private boolean isLogged(String sessionId) {
        return sessionId != null ? true : noLoged();
    }

    private boolean noLoged() {
        System.out.println("Please login first");
        return false;
    }

    private boolean isItUser(String user) {
        return this.userStorage.contains(user) ? true : noUser();
    }

    private boolean noUser() {
        System.out.println("No this user");
        return false;
    }

    private boolean flug;

    TimerTask receive = new TimerTask() {

        @Override
        public void run() {
            try {
                flug = false;
                receiveMsg();
                receiveFile();
                activeUser();
                flug = true;
            } catch (RemoteException | ArgumentFault | ServerFault ex) {
                System.out.println("Problem Timer task");
            }
        }
    };

    private final List<String> userStorage = new LinkedList<>();

    private void activeUser() throws RemoteException, ArgumentFault, ServerFault {
        if (sessionId != null) {
            List<String> onlineUser = iServ.listUsers(sessionId);

            if (onlineUser == null) {
                return;
            }
            for (String user : onlineUser) {
                if (!userStorage.contains(user)) {
                    userStorage.add(user);
                    System.out.println(user + " logged in");
                }
            }
            for (String user : userStorage) {
                if (!onlineUser.contains(user)) {
                    System.out.println(user + " logged out");
                    userStorage.remove(user);
                }
            }
        }
    }
}
