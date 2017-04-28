package seminar6;

public class Main {

    public static void main(String[] args) {
        
        try(Client client = new Client()){
            client.start();
        }        
        
    }    
}
