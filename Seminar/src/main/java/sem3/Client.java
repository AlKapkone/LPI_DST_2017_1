package sem3;

import java.rmi.*;
import java.rmi.registry.*;
import java.util.Scanner;
import lpi.server.rmi.IServer;

public class Client {
    public static boolean flug = true;

    public void start(int port) {

        try (Scanner scanner = new Scanner(System.in)) {

            Registry registry = LocateRegistry.getRegistry(port);
            IServer proxy = (IServer) registry.lookup(IServer.RMI_SERVER_NAME);
            System.out.println("Welcome to server");
            
            Interpretator inter = new Interpretator(proxy);
            
            while (flug) {
                String inLine = scanner.nextLine().trim();
                
                if (!inLine.equals("")) {
                    inter.interpretator(inLine);
                }
            }
        } catch (RemoteException | NotBoundException ex) {
            System.out.println("Problem conections");
        }
    }
}
