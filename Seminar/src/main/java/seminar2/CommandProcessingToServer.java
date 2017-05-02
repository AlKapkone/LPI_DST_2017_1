package seminar2;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

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
        return PING_ID;
    }

    public byte[] ehoToServer(String[] comand) {
        if (comand.length == 1) {
            return EHO_ID;
        } else {
            String echoText = comand[1];
            return prepareDataToSend(echoText.getBytes(), EHO_ID);
        }
    }

    public byte[] loginToServer(String[] comand) {

        if (comand.length != 3) {
            return null;
        }
        String[] user = Arrays.copyOfRange(comand, 1, comand.length);

        byte[] serialized = serialize(user);
        return prepareDataToSend(serialized, LOGIN_ID);
    }

    public byte[] listToServer(String[] comand) {
        return LIST_ID;
    }

    public byte[] msgToServer(String[] comand) {
        if (comand.length != 3) {
            return null;
        }
        String[] user = Arrays.copyOfRange(comand, 1, comand.length);

        byte[] serialized = serialize(user);
        return prepareDataToSend(serialized, MSG_ID);
    }

    public byte[] fileToServer(String[] comand) {
        if (comand.length != 3) {
            return null;
        }
        String receiver = comand[1];
        String fileName = comand[2];

        if (!new File(fileName).exists()) {
            System.out.println("No file");
            return null;
        }
        Path path = FileSystems.getDefault().getPath(fileName);
        byte[] file = {};
        
        try {
            file = Files.readAllBytes(path);
        } catch (IOException ex) {
            System.out.println("Problem wich opened file");
        }
        
        if (!(file.length > 0 && file.length < MAX_CONTENT_SIZE)) {
            System.out.println("Bad file size");
            return null;
        }
        byte[] serialized = serialize(new Object[]{receiver, fileName, file});
        return prepareDataToSend(serialized, FILE_ID);
    }

    private byte[] prepareDataToSend(byte[] serialized, byte[] ObjID) {
        byte[] respByte = new byte[1 + serialized.length];
        respByte[0] = ObjID[0];
        System.arraycopy(serialized, 0, respByte, 1, serialized.length);
        return respByte;
    }

    public byte[] receiveMsgToServer() {
        return RECEIVE_MSG_ID;
    }

    public byte[] receiveFileToServer() {
        return RECEIVE_FILE_ID;
    }

    private byte[] serialize(Object object) {

        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            objectStream.writeObject(object);
            return byteStream.toByteArray();
        } catch (IOException ex) {
            System.out.println("Serialization problem");
        }
        return new byte[]{};
    }
}
