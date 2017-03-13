package seminar4;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import lpi.server.soap.ArgumentFault;
import lpi.server.soap.FileInfo;
import lpi.server.soap.IChatServer;
import lpi.server.soap.LoginFault;
import lpi.server.soap.Message;
import lpi.server.soap.ServerFault;

public class CommandProcessing {

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
            if (comandMas.length == 2) {
                System.out.println(iServ.echo(comandMas[1]));
            } else {
                System.out.println("bad argument");
            }
        } catch (Exception ex) {
            System.out.println("connections problem");
        }
    }

    private final Timer timer = new Timer();
    private static String myLogin = null;
    private static String sessionId = null;

    public void login(String[] comandMas) throws RemoteException, ArgumentFault, ServerFault, LoginFault {
        if (comandMas.length == 3) {

            if (!comandMas[1].equals(myLogin)) {

                if (sessionId != null) {
                    iServ.exit(sessionId);
                }

                sessionId = iServ.login(comandMas[1], comandMas[2]);

                if (myLogin == null) {
                    timer.schedule(receive, 0, 1500);
                }
                myLogin = comandMas[1];
            }
        } else {
            System.out.println("bad argument");
        }
    }

    public void list() throws RemoteException, ArgumentFault, ServerFault {

        if (sessionId != null) {
            List<String> list = iServ.listUsers(sessionId);
            if (list != null) {
                for (String f : list) {
                    System.out.println(f);
                }
            }
        } else {
            System.out.println("Not login");
        }
    }

    public void msg(String[] comandMas) throws RemoteException, ArgumentFault, ServerFault {
        if (sessionId == null) {
            System.out.println("Not login");
        } else if (comandMas.length == 3) {
            Message mess = new Message();
            mess.setSender(myLogin);
            mess.setReceiver(comandMas[1]);
            mess.setMessage(comandMas[2]);

            if (isItUser(comandMas[1])) {
                iServ.sendMessage(sessionId, mess);
                System.out.println("Message sent");
            }
        } else {
            System.out.println("bad argument");
        }
    }

    public void file(String[] comandMas) throws IOException, ArgumentFault, ServerFault {

        if (sessionId == null) {
            System.out.println("Not login");
        } else if (comandMas.length == 3) {
            File fil = new File(comandMas[2]);

            if (isItUser(comandMas[1])) {
                if (fil.exists()) {

                    FileInfo fille = new FileInfo();

                    fille.setSender(myLogin);
                    fille.setReceiver(comandMas[1]);
                    fille.setFilename(comandMas[2]);
                    fille.setFileContent(Files.readAllBytes(fil.toPath()));

                    iServ.sendFile(sessionId, fille);
                    System.out.println("File sent");
                } else {
                    System.out.println("No this file");
                }
            }
        } else {
            System.out.println("bad argument");
        }
    }

    public void receiveMsg() throws RemoteException, ArgumentFault, ServerFault {
        if (sessionId != null) {
            Message mess = null;
            mess = iServ.receiveMessage(sessionId);
            if (mess != null) {
                System.out.println("Message from: \"" + mess.getSender()
                        + "\" : " + mess.getMessage());
            } else if (flug) {
                System.out.println("No message");
            }
        } else {
            System.out.println("Not loggin");
        }
    }

    public void receiveFile() throws RemoteException, ArgumentFault, ServerFault {
        FileInfo file = null;

        if (sessionId != null) {
            file = iServ.receiveFile(sessionId);

            if (file != null) {
                System.out.println("File from \"" + file.getSender()
                        + "\" file name : \"" + file.getFilename() + "\"");

                try (FileOutputStream fos = new FileOutputStream(
                        new File(file.getSender() + "_" + file.getFilename()))) {
                    fos.write(file.getFileContent());
                } catch (Exception ex) {
                    System.out.println("Problem with write file");
                }
            } else if (flug) {
                System.out.println("No file");
            }
        } else {
            System.out.println("Not loggin");
        }
    }

    public void exit() throws RemoteException, ArgumentFault, ServerFault {
        Client.flug = false;
        timer.cancel();
        iServ.exit(sessionId);
        System.out.println("Exit from server");        
    }

    private boolean isItUser(String user) {
        boolean isIt;
        if (this.user.contains(user)) {
            isIt = true;
        } else {
            System.out.println("No this user");
            isIt = false;
        }
        return isIt;
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

            } catch (RemoteException ex) {
                System.out.println("Problem Timer task");
            } catch (ArgumentFault | ServerFault ex) {
                Logger.getLogger(CommandProcessing.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

    private final List<String> user = new LinkedList<>();

    private void activeUser() throws RemoteException, ArgumentFault, ServerFault {
        if (sessionId != null) {
            List<String> activeUser = new LinkedList<>();
            List<String> logedOut = new LinkedList<>();

            List<String> list = iServ.listUsers(sessionId);

            if (list != null) {

                for (String f : list) {
                    activeUser.add(f);
                    if (!user.contains(f)) {
                        user.add(f);
                        System.out.println(f + " logged in");
                    }
                }

                for (String us : user) {
                    if (!activeUser.contains(us)) {
                        System.out.println(us + " logged out");
                        logedOut.add(us);
                    }
                }
                for (String out : logedOut) {
                    user.remove(out);
                }
            }
        }
    }
}
