package seminar5;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

public class CommandProcessing {

    private final int ECHO_VALID_PARAMETERS_LENGTH = 2;
    private final int LOGIN_VALID_PARAMETERS_LENGTH = 3;
    private final int MESSAGE_VALID_PARAMETERS_LENGTH = 3;
    private final int FILE_VALID_PARAMETERS_LENGTH = 3;

    private final javax.ws.rs.client.Client client;

    public CommandProcessing(javax.ws.rs.client.Client client) {
        this.client = client;
    }

    private final ParsResp pR = new ParsResp();

    private final String uri = "http://localhost:8080/chat/server/";

    private int receiveCod;
    private boolean enteredComand;

    private final Timer timer = new Timer();

    private final UserInfo userInfo = new UserInfo();
    private static Entity userInfoEntity;
    private boolean logg = true;

    private final FileInfo fileInfo = new FileInfo();
    private Entity fileInfoEntity;

    public void ping() {
        try {
            System.out.println(client.target(uri + "ping")
                    .request(MediaType.TEXT_PLAIN_TYPE)
                    .get(String.class));
        } catch (Exception ex) {
            System.out.println("connections problem");
        }
    }

    public void echo(String[] comandMas) {

        if (isValidNumberOfParameter(comandMas.length, ECHO_VALID_PARAMETERS_LENGTH)) {
            try {
                System.out.println(client.target(uri + "echo")
                        .request(MediaType.TEXT_PLAIN_TYPE)
                        .post(Entity.text(comandMas[1]), String.class));

            } catch (Exception ex) {
                System.out.println("connections problem");
            }
        }
    }

    public void login(String[] comandMas) {
        if (!logg) {
            System.out.println("You are logged");
            return;
        }

        if (!isValidNumberOfParameter(comandMas.length, LOGIN_VALID_PARAMETERS_LENGTH)) {
            userInfo.login = comandMas[1];
            userInfo.password = comandMas[2];
            userInfoEntity = Entity.entity(userInfo, MediaType.APPLICATION_JSON_TYPE);

            receiveCod = client.target(uri + "user")
                    .request(MediaType.TEXT_PLAIN_TYPE)
                    .put(userInfoEntity)
                    .getStatus();

            if (receiveCod == 201 || receiveCod == 202) {
                if (receiveCod == 201) {
                    System.out.println("New user registered");
                }
                if (receiveCod == 202) {
                    System.out.println("the provided login/password are correct");
                }
                this.client.register(HttpAuthenticationFeature.basic(userInfo.login, userInfo.password));

                if (logg) {
                    logg = false;
                    timer.schedule(receive, 0, 1500);
                }
            } else {
                getRespCod(receiveCod);
            }
        }
    }

    public void list() {
        receiveCod = client.target(uri + "users")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Response.class)
                .getStatus();

        if (receiveCod == 200) {
            List<String> users = pR.pars(client.target(uri + "users")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(String.class));

            System.out.println("Active users:\n");

            users.stream().forEach((us) -> {
                System.out.println(us);
            });

            System.out.println();
        } else {
            getRespCod(receiveCod);
        }
    }

    public void msg(String[] comandMas) {

        if (!isValidNumberOfParameter(comandMas.length, MESSAGE_VALID_PARAMETERS_LENGTH)) {
            receiveCod = client.target(uri + comandMas[1] + "/messages")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.text(comandMas[2]))
                    .getStatus();

            getRespCod(receiveCod);
        }
    }

    public void file(String[] comandMas) throws IOException {
        if (!isValidNumberOfParameter(comandMas.length, FILE_VALID_PARAMETERS_LENGTH)) {
            File file = new File(comandMas[2]);
            if (!file.exists()) {
                System.out.println("No this file");
                return;
            }

            java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
            fileInfo.sender = userInfo.login;
            fileInfo.filename = comandMas[2];
            fileInfo.content = encoder.encodeToString(Files.readAllBytes(file.toPath()));
            fileInfoEntity = Entity.entity(fileInfo, MediaType.APPLICATION_JSON_TYPE);

            receiveCod = client.target(uri + comandMas[1] + "/files")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(fileInfoEntity)
                    .getStatus();

            getRespCod(receiveCod);
        }
    }

    private boolean isValidNumberOfParameter(int ComandMasLength, int validLength) {        
        return (ComandMasLength != validLength) ? true : badParameterLength();
    }
    
    private boolean badParameterLength(){
        System.out.println("Bad argument");
        return false;
    }

    public void receiveMsg() {
        receiveCod = client.target(uri + userInfo.login + "/messages")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Response.class)
                .getStatus();

        if (receiveCod == 200) {

            List<String> messId = pR.pars(client.target(uri + userInfo.login + "/messages/")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(String.class));

            for (String id : messId) {
                List<String> mess = pR.pars(client.target(uri + userInfo.login + "/messages/" + id)
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .get(String.class));

                System.out.println("\nMessage from : " + mess.get(3) + "\nMessage text : " + mess.get(1) + "\n");
                getRespCod(client.target(uri + userInfo.login + "/messages/" + id)
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .delete(Response.class)
                        .getStatus());
            }
        } else {
            getRespCod(receiveCod);
        }
    }

    public void receiveFile() {
        receiveCod = client.target(uri + userInfo.login + "/files")
                .request(MediaType.APPLICATION_JSON_TYPE).get(Response.class)
                .getStatus();

        if (receiveCod == 200) {

            List<String> fileId = pR.pars(client.target(uri + userInfo.login + "/files/")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(String.class));

            for (String id : fileId) {
                FileInfo fI = client.target(uri + userInfo.login + "/files/" + id)
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .get(FileInfo.class);

                java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
                byte[] decodedContent = decoder.decode(fI.content);

                if (decodedContent != null) {
                    System.out.println("\nFile from " + fI.sender
                            + "\nFile name : \"" + fI.filename + "\"\n");

                    try (FileOutputStream fos = new FileOutputStream(
                            new File(fI.sender + "_" + fI.filename))) {
                        fos.write(decodedContent);
                    } catch (Exception ex) {
                        System.out.println("Problem with write file");
                    }
                } else if (enteredComand) {
                    System.out.println("No file");
                }
// delete file from server
                getRespCod(client.target(uri + userInfo.login + "/files/" + id)
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .delete(Response.class)
                        .getStatus());
            }
        } else {
            getRespCod(receiveCod);
        }
    }

    public void exit() {
        Client.flug = false;
        timer.cancel();
        if (client != null) {
            client.close();
        }
        System.out.println("Exit from server");
    }

    private void getRespCod(int cod) {
        switch (cod) {

            case 200:
                System.out.println("deleted successfully");
                break;

            case 201:
                System.out.println("sending is processed");
                break;

            case 204:
                if (enteredComand) {
                    System.out.println("There are nothing for this  user");
                }
                break;

            case 400:
                System.out.println("incorrect data request");
                break;

            case 401:
                System.out.println("authentication is not correct");
                break;

            case 403:
                System.out.println("you are not allowed to access this resource");
                break;

            case 404:
                System.out.println("the specified message was not found");
                break;

            case 406:
                System.out.println("target user has too much pending files");
                break;

            case 500:
                System.out.println("internal server error");
                break;
        }
    }

    TimerTask receive = new TimerTask() {

        @Override
        public void run() {

            enteredComand = false;
            receiveMsg();
            receiveFile();
            activeUser();
            enteredComand = true;
        }
    };
    private final List<String> user = new LinkedList<>();

    private void activeUser() {
        if (client.target(uri + "user")
                .request(MediaType.TEXT_PLAIN_TYPE)
                .put(userInfoEntity)
                .getStatus() == 202) {

            List<String> activeUser = new LinkedList<>();
            List<String> logedOut = new LinkedList<>();
            List<String> list = null;

            if (client.target(uri + "users")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Response.class)
                    .getStatus() == 200) {

                list = pR.pars(client.target(uri + "users")
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .get(String.class));
            }

            for (String f : list) {
                activeUser.add(f);

                if (!user.contains(f)) {
                    user.add(f);
                    System.out.println("\t" + f + " logged in");
                }
            }

            for (String us : user) {
                if (!activeUser.contains(us)) {
                    System.out.println("\t" + us + " logged out");
                    logedOut.add(us);
                }
            }

            logedOut.stream().forEach((out) -> {
                user.remove(out);
            });
        }
    }
}
