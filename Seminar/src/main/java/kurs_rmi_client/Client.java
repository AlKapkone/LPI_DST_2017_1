package kurs_rmi_client;

public class Client {
    
    public static void main(String[] args) {        
        try {            
            StartClient startClient = new StartClient();
            startClient.starting();             
        } catch (Exception ex) {
            ex.printStackTrace();
        }      
    }
}
