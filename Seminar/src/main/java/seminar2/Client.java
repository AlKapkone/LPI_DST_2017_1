package seminar2;

import java.io.*;
import java.util.*;
import java.net.Socket;

public class Client {

    private TimerTask receive;
    private DataOutputStream dataOut;
    private DataInputStream dataIn;
    private Interpretator interpretator;
    private final Timer timer = new Timer();

    public void startClient(String ip, int port) {

        try (Socket soket = new Socket(ip, port);
                Scanner scanner = new Scanner(System.in)) {

            interpretator = new Interpretator();

            dataOut = new DataOutputStream(soket.getOutputStream());
            dataIn = new DataInputStream(soket.getInputStream());

            createTimerTask();

            timer.schedule(receive, 2000, 2000);
            System.out.println("Welcome to server");

            while (true) {
                String inLine = scanner.nextLine().trim();
                if (inLine.isEmpty()) {
                    break;
                }

                if (inLine.equals("exit")) {
                    exit();
                    return;
                }
                contactServer(inLine);

            }
        } catch (IOException ex) {
            System.out.println("Error build Socket");
            timer.cancel();
        }
    }

    private void exit() {
        System.out.println("Exit from server");
        timer.cancel();
    }

    private void contactServer(String inLine) throws IOException {
        byte[] data = interpretator.prepareToSend(inLine);

        if (data != null) {
            try {
                byte[] out = writeRead(data);
                if (out != null) {
                    interpretator.executeRespons(out);
                }
            } catch (IOException ex) {
                System.out.println("dataOut.write/dataIn.read problem");
            }
        }
    }

    private void createTimerTask() {
        List<String> userStory = new LinkedList<>();
        receive = new TimerTask() {
            @Override
            public void run() {
                try {
                    byte[] out = writeRead(new byte[]{25});
//                        check msg
                    if (out[0] != 26 && out[0] != 112) {
                        interpretator.setComand("receivemsg");
                        interpretator.executeRespons(out);
                    }
//                        check file
                    out = writeRead(new byte[]{30});
                    if (out[0] != 31 && out[0] != 112) {
                        interpretator.setComand("receivefile");
                        interpretator.executeRespons(out);
                    }
//                        check list
                    out = writeRead(new byte[]{10});
                    if (out[0] != 112) {
                        List<String> onlineUser = Arrays.asList(ComandProcessingFromServer.deserialize(out, 0, String[].class));
                        for (String user : onlineUser) {
                            if (!userStory.contains(user)) {
                                userStory.add(user);
                                System.out.println(user + " login to server\n");
                            }
                        }

                        for (String user : userStory) {
                            if (!onlineUser.contains(user)) {
                                System.out.println(user + " login out\n");
                                userStory.remove(user);
                            }
                        }

                    }
                } catch (IOException | ClassNotFoundException ex) {
                    System.out.println("Timer error");
                }
            }
        };
    }

    private synchronized byte[] writeRead(byte[] data) throws IOException {
        dataOut.writeInt(data.length);
        dataOut.write(data);

        int respLength = dataIn.readInt();
        return (respLength > 0) ? readResponse(respLength) : new byte[]{};
    }

    private byte[] readResponse(int respLength) throws IOException {
        byte[] inData = new byte[respLength];
        dataIn.readFully(inData);
        return inData;
    }

}
