package seminar4;

import java.util.Scanner;
import lpi.server.soap.*;

public class Client {

    public static boolean flug = true;

    public void start() {

        try (Scanner scanner = new Scanner(System.in)) {

            ChatServer serverWrapper = new ChatServer();
            IChatServer serverProxy = serverWrapper.getChatServerProxy();

            System.out.println("Welcome to server");

            Interpretator inter = new Interpretator(serverProxy);

            while (flug) {
                String inLine = scanner.nextLine().trim();

                if (!inLine.equals("")) {
                    inter.interpretator(inLine);
                }
            }

        } catch(Exception ex){
            System.out.println("Connections problem");
        }
    }
}
