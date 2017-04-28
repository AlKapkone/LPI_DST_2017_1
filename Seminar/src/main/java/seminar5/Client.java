package seminar5;

import java.util.Scanner;

public class Client {

    public static boolean flug = true;

    public void start() {

        try (Scanner scanner = new Scanner(System.in)) {

            javax.ws.rs.client.Client client = javax.ws.rs.client.ClientBuilder.newClient();            

            Interpretator inter = new Interpretator(client);
            System.out.println("Welcome to server");            

            while (flug) {                
                inter.interpretator(scanner.nextLine().trim());                
            }

        } catch(Exception ex){
            System.out.println("Connections problem");
        }
    }
}
