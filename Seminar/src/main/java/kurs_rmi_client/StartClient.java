package kurs_rmi_client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import rmi_server.Compute;

public class StartClient {    
    
    private final Interpretator interpretator;
    private final Registry registry;
    private final Compute remoteCompute;
    
    StartClient() throws Exception{
        getSecurityManager();
        registry = LocateRegistry.getRegistry(Compute.PORT);
        remoteCompute = (Compute) registry.lookup(Compute.SERVER_NAME);
        interpretator = new Interpretator(remoteCompute);
    }

    private void getSecurityManager() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
    }
    
    public void starting() {
        try {            
            sayHello();
            waitComand();
        } catch (Exception ex) {
            System.err.println("Connections problem ");
        }
    }
    
    private void sayHello() {
        System.out.println("Welcom to the RMI server, enter \"?\" to show available comand.\n"
                + "Enter \"exit\" to closing client\n");
    }

    public static boolean working = true;
    
    private void waitComand(){

        try (Scanner scanner = new Scanner(System.in)) {
            while (working) 
                interpretator.interpretator(scanner.nextLine());            
        }
    }

}
