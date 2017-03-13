package seminar2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Client {    

    public void startClient(String ip, int port) {
        Timer timer = new Timer();
        try (Socket soket = new Socket(ip, port);
                Scanner scanner = new Scanner(System.in)) {
            System.out.println("Welcome to server");

            final Interpretator inter = new Interpretator();
            
            final DataOutputStream dataOut = new DataOutputStream(soket.getOutputStream());
            final DataInputStream dataIn = new DataInputStream(soket.getInputStream());
            final List<String> userStory = new LinkedList<>();

            TimerTask receive = new TimerTask() {
                @Override
                public void run() {
                    byte[] out = null;
                    try {
//                        check msg
                        out = writeRead(dataOut, new byte[]{25}, dataIn);
                        if (out[0] != 26 && out[0] != 112) {
                            Interpretator.sendComand = "receivemsg";
                            inter.interpretator(out);
                        }
//                        check file
                        out = writeRead(dataOut, new byte[]{30}, dataIn);
                        if (out[0] != 31 && out[0] != 112) {
                            Interpretator.sendComand = "receivefile";
                            inter.interpretator(out);
                        }
//                        check list
                        out = writeRead(dataOut, new byte[]{10}, dataIn);
                        if (out[0] != 112) {
                            String[] users = Interpretator.deserialize(out, 0, String[].class);
                            List<String> newUser = new LinkedList<>();
                            for (String us : users) {
                                if (!newUser.contains(us)) {
                                    newUser.add(us);
                                }
                                if (!userStory.contains(us)) {
                                    userStory.add(us);
                                    System.out.println(us + " login to server\n");
                                }
                            }
                            for (String h : userStory) {
                                if (!newUser.contains(h)) {
                                    System.out.println(h + " login out\n");
                                    userStory.remove(h);
                                }
                            }
                        }
                    } catch (IOException | ClassNotFoundException ex) {
                        System.out.println("Timer error");
                    }
                }
            };
            
            timer.schedule(receive, 2000, 2000);

            while (true) {
                String inLine = scanner.nextLine();
                if (inLine.equals("exit")) {
                    System.out.println("Exit from server");
                    timer.cancel();
                    break;
                }

                if (!inLine.isEmpty()) {
                    byte[] data = inter.interpretator(inLine);

                    if (data != null) {
                        try {
                            byte[] out = writeRead(dataOut, data, dataIn);
                            if (out != null) {
                                inter.interpretator(out);
                            }
                        } catch (IOException ex) {
                            System.out.println("dataOut.write/dataIn.read problem");
                        }
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("Error build Socket");
            timer.cancel();
        }
    }

    private synchronized byte[] writeRead(DataOutputStream dataOut, byte[] data, DataInputStream dataIn) throws IOException {
        dataOut.writeInt(data.length);
        dataOut.write(data);

        int respLength = dataIn.readInt();
        byte[] inData = null;
        if (respLength > 0) {
            inData = new byte[respLength];
            dataIn.readFully(inData);          
        }
        return inData;
    }
}