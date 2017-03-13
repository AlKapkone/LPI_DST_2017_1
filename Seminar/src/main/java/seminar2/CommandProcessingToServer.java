package seminar2;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class CommandProcessingToServer {

    private static final byte[] PING_ID = new byte[]{1};
    private static final byte[] EHO_ID = new byte[]{3};
    private static final byte[] LOGIN_ID = new byte[]{5};
    private static final byte[] LIST_ID = new byte[]{10};
    private static final byte[] MSG_ID = new byte[]{15};
    private static final byte[] FILE_ID = new byte[]{20};
    private static final byte[] RECEIVE_MSG_ID = new byte[]{25};
    private static final byte[] RECEIVE_FILE_ID = new byte[]{30};

    private static final int MAX_CONTENT_SIZE = 10_500_000;

    public byte[] pingToServer(String comand) {
        Interpretator.sendComand = comand;
        return PING_ID;
    }

    public byte[] ehoToServer(String[] comand) {
        Interpretator.sendComand = comand[0];
        if (comand.length == 1) {
            return EHO_ID;
        } else {
            byte[] respByte = new byte[1 + comand[1].getBytes().length];
            respByte[0] = EHO_ID[0];

            System.arraycopy(comand[1].getBytes(), 0, respByte, 1, comand[1].getBytes().length);
            return respByte;
        }
    }

    private String[] user = null;
    private byte[] serialize = null;

    public byte[] loginToServer(String[] comand) throws IOException {
        Interpretator.sendComand = comand[0];

        if (comand.length == 3) {
            user = new String[2];
            user[0] = comand[1];
            user[1] = comand[2];
        }
        serialize = Interpretator.serialize(user);
        byte[] respByte = new byte[1 + serialize.length];
        respByte[0] = LOGIN_ID[0];

        System.arraycopy(serialize, 0, respByte, 1, serialize.length);
        return respByte;
    }

    public byte[] listToServer(String[] comand) {
        Interpretator.sendComand = comand[0];
        return LIST_ID;
    }

    public byte[] msgToServer(String[] comand) throws IOException {
        Interpretator.sendComand = comand[0];
        if (comand.length == 3) {
            user = new String[2];
            user[0] = comand[1];
            user[1] = comand[2];

            serialize = Interpretator.serialize(user);
            byte[] respByte = new byte[1 + serialize.length];
            respByte[0] = MSG_ID[0];

            System.arraycopy(serialize, 0, respByte, 1, serialize.length);
            return respByte;
        } else {
            return null;
        }
    }

    public byte[] fileToServer(String[] comand) {
        byte[] respByte = null;
        if (comand.length == 3) {
            Interpretator.sendComand = comand[0];
            user = comand;
            try {
                if (!new File(user[2]).exists()) {
                    System.out.println("No file");
                    return null;
                }
                Path path = FileSystems.getDefault().getPath(user[2]);
                byte[] file = Files.readAllBytes(path);
                if (!(file.length > 0 && file.length < MAX_CONTENT_SIZE)) {
                    System.out.println("Bad file size");
                    return null;
                }
                serialize = Interpretator.serialize(new Object[]{user[1], user[2], file});
                respByte = new byte[1 + serialize.length];
                respByte[0] = FILE_ID[0];
                System.arraycopy(serialize, 0, respByte, 1, serialize.length);
            } catch (IOException ex) {
                System.out.println("Problem wich opened file");
            }
        }
        return respByte;
    }

    public byte[] receiveMsgToServer() {
        Interpretator.sendComand = "receivemsg";
        return RECEIVE_MSG_ID;
    }

    public byte[] receiveFileToServer() {
        Interpretator.sendComand = "receivefile";
        return RECEIVE_FILE_ID;
    }
}
