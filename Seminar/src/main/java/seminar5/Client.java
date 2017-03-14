package seminar5;

import java.util.Scanner;

public class Client {

    public static boolean flug = true;

    public void start() {

        try (Scanner scanner = new Scanner(System.in)) {

            javax.ws.rs.client.Client client = javax.ws.rs.client.ClientBuilder.newClient();
            
            System.out.println("Welcome to server");            

            Interpretator inter = new Interpretator(client);

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
