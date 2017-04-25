package rmi_server;

import java.io.IOException;
import java.util.Date;

import static rmi_server.Compute.PORT;

public class Main{
    public static void main(String[] args) throws IOException {

        try(Server server = new Server()) {
            server.startServer(PORT);
            System.out.printf("%tF %<tT %s %n", new Date(), " Server starting");

            System.in.read();
        }finally{
            System.out.printf("%tF %<tT %s %n", new Date(), " Server closed");
        }
    }
}
