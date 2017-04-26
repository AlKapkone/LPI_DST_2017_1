package kurs_rmi_client;

import java.io.IOException;
import java.rmi.RemoteException;
import rmi_server.Compute;

public class Interpretator {
    private final CommandProcessing comandProcess;
    
    public Interpretator(Compute remoteCompute) {
        comandProcess = new CommandProcessing(remoteCompute);
    }

    public void interpretator(String inLine) {
        try {            
            callComand(inLine);            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private final Parser parser = new Parser();
    private void callComand(String inLine) throws RemoteException, IOException {
        
        String[] comandMas = parser.parsForComand(inLine);

        switch (comandMas[0]) {
            
            case "?":
                comandProcess.avaibleComand();
                break;
            
            case "exit":
                comandProcess.exit();
                break;

            case "ping":
                comandProcess.ping();
                break;

            case "echo":
                comandProcess.echo(comandMas);
                break;

            case "sort":
                comandProcess.sort(comandMas);
                break;
                
            default:
                System.out.println("No this comand");
                break;
        }
    }
}
