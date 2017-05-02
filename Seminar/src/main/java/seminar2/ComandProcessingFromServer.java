package seminar2;

import java.io.*;

public class ComandProcessingFromServer {

    private static final byte PING_ID_RESP = 2;
    private static final byte LOGIN_ID_RESP_REG_OK = 6;
    private static final byte LOGIN_ID_RESP_LOGIN_OK = 7;
    private static final byte MSG_ID_RESP_SENT = 16;
    private static final byte FILE_ID_RESP_SENT = 21;
    private static final byte RECEIVE_MSG_ID_RESP_NO_MESSAGES = 26;
    private static final byte RECEIVE_FILE_ID_RESP_NO_FILE = 31;

//    Error ID 
    private static final byte SERVER_ERROR_ID = 100;
    private static final byte WRONG_SIZE_ID = 101;
    private static final byte SERIALIZATION_ID = 102;
    private static final byte UNKNOW_ID = 103;
    private static final byte INCORRECT_PARAMETERS_ID = 104;
    private static final byte WRONG_PASSWORD_ID = 110;
    private static final byte NOT_LOGGED_IN_ID = 112;
    private static final byte SENDING_FAILED_ID = 113;

    public void pingFromServer(byte[] serverResp) {

        if (serverResp[0] == PING_ID_RESP) {
            System.out.println("Ping sucessfull\n");
        } else {
            getError(serverResp[0]);
        }
    }

    public void ehoFromServer(byte[] serverResp) throws UnsupportedEncodingException {
        String st = new String(serverResp, "UTF-8");
        System.out.println(st);
    }

    private boolean isLogin = false;

    public void loginFromServer(byte[] serverResp) {
        switch (serverResp[0]) {
            case LOGIN_ID_RESP_REG_OK:
                System.out.println("new user, registration ok;\n");
                isLogin = true;
                break;
            case LOGIN_ID_RESP_LOGIN_OK:
                System.out.println("login ok;\n");
                isLogin = true;
                break;
            default:
                getError(serverResp[0]);
                break;
        }
    }

    public void listFromServer(byte[] serverResp) throws ClassNotFoundException, IOException {
        if (serverResp.length != 1) {
            for (String str : deserialize(serverResp, 0, String[].class)) {
                System.out.println(str);
            }
        } else {
            getError(serverResp[0]);
        }
    }

    public void msgFromServer(byte[] serverResp) {
        if (isLogin && serverResp[0] == MSG_ID_RESP_SENT) {
            System.out.println("Message sent;\n");
        } else {
            getError(serverResp[0]);
        }
    }

    public void fileFromServer(byte[] serverResp) {
        if (serverResp[0] == FILE_ID_RESP_SENT) {
            System.out.println("File sent;\n");
        } else {
            getError(serverResp[0]);
        }

    }

    public void receiveMsg(byte[] serverResp) throws ClassNotFoundException, IOException {
        if (serverResp.length != 1) {
            String[] mess = deserialize(serverResp, 0, String[].class);
            System.out.println("\nMessege from: " + mess[0] + " : " + mess[1]);
        } else if (serverResp[0] == RECEIVE_MSG_ID_RESP_NO_MESSAGES) {
            System.out.println("No msg;");
        } else {
            getError(serverResp[0]);
        }
    }

    public void receiveFile(byte[] serverResp) throws ClassNotFoundException, IOException {
        if (serverResp.length != 1) {
            Object[] resFile = deserialize(serverResp, 0, Object[].class);
            if (resFile.length != 3
                    || resFile[0] == null || resFile[1] == null || resFile[2] == null
                    || !resFile[0].getClass().equals(String.class) 
                    || !resFile[1].getClass().equals(String.class)
                    || !resFile[2].getClass().equals(byte[].class)) {
                System.out.println("Bad file parameters from server");
            }
            String sender = (String) resFile[0];
            String fileName = (String) resFile[1];
            System.out.println("File from user:  " + sender + "\nfile name : " + fileName);

            try (FileOutputStream fos = new FileOutputStream(
                    new File(sender + "_" + fileName))) {
                fos.write((byte[]) resFile[2]);
            } catch (Exception ex) {
                System.out.println("Problem with write file");
            }
        } else if (serverResp[0] == RECEIVE_FILE_ID_RESP_NO_FILE) {
            System.out.println("No file;");
        } else {
            getError(serverResp[0]);
        }
    }

    public void getError(byte error) {
        switch (error) {
            case SERVER_ERROR_ID:
                System.out.println("SERVER ERROR");
                break;
            case WRONG_SIZE_ID:
                System.out.println("WRONG SIZE");
                break;
            case SERIALIZATION_ID:
                System.out.println("SERIALIZATION");
                break;
            case UNKNOW_ID:
                System.out.println("UNKNOW");
                break;
            case INCORRECT_PARAMETERS_ID:
                System.out.println("INCORRECT PARAMETERS");
                break;
            case WRONG_PASSWORD_ID:
                System.out.println("WRONG PASSWORD");
                break;
            case NOT_LOGGED_IN_ID:
                System.out.println("NOT LOGGED IN");
                break;
            case SENDING_FAILED_ID:
                System.out.println("SENDING FAILED");
                break;
            default:
                System.out.println(error);
                break;
        }
    }

    public static <T> T deserialize(byte[] data, int offset, Class<T> clazz) throws ClassNotFoundException, IOException {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(data, offset, data.length - offset);
                ObjectInputStream objectStream = new ObjectInputStream(stream)) {
            return (T) objectStream.readObject();
        }
    }
}
