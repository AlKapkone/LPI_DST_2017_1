package seminar2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Interpretator {

    public static String sendComand = null;

    CommandProcessingToServer comTo = new CommandProcessingToServer();

    public byte[] interpretator(String inLine) throws IOException {

        String[] comandMas = parsForComand(inLine);

        switch (comandMas[0]) {
            case "ping":
                return comTo.pingToServer(comandMas[0]);

            case "eho":
                return comTo.ehoToServer(comandMas);

            case "login":
                return comTo.loginToServer(comandMas);

            case "list":
                return comTo.listToServer(comandMas);

            case "msg":
                return comTo.msgToServer(comandMas);

            case "file":
                return comTo.fileToServer(comandMas);

            case "receivemsg":
                return comTo.receiveMsgToServer();

            case "receivefile":
                return comTo.receiveFileToServer();

            default:
                System.out.println("No this comand");
                return null;
        }
    }
    
    ComandProcessingFromServer comFrom = new ComandProcessingFromServer();

    public void interpretator(byte[] serverResp) {
        if (serverResp != null) {
            try {
                switch (sendComand) {
                    case "ping":
                        comFrom.pingFromServer(serverResp);
                        break;

                    case "eho":
                        comFrom.ehoFromServer(serverResp);
                        break;

                    case "login":
                        comFrom.loginFromServer(serverResp);
                        break;

                    case "list":
                        comFrom.listFromServer(serverResp);
                        break;

                    case "msg":
                        comFrom.msgFromServer(serverResp);
                        break;

                    case "file":
                        comFrom.fileFromServer(serverResp);
                        break;

                    case "receivemsg":
                        comFrom.receiveMsg(serverResp);
                        break;

                    case "receivefile":
                        comFrom.receiveFile(serverResp);
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("problem with interpretation responds");
            }
        }
    }

    public static byte[] serialize(Object object) throws IOException {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            objectStream.writeObject(object);
            return byteStream.toByteArray();
        }
    }

    public static <T> T deserialize(byte[] data, int offset, Class<T> clazz) throws ClassNotFoundException, IOException {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(data, offset, data.length - offset);
                ObjectInputStream objectStream = new ObjectInputStream(stream)) {
            return (T) objectStream.readObject();
        }
    }

    private String[] parsForComand(String line) {
        String[] outMas = null;
        String[] parsMas = line.split(" ", 2);

        switch (parsMas[0]) {
            case "eho":
                outMas = line.split(" ", 2); // comand _ anyText                  
                break;

            case "msg":
                outMas = line.split(" ", 3); // comand _ destination _ messegeText 
                break;

            default:
                outMas = line.split(" ");
                break;
        }
        return outMas;
    }
}
