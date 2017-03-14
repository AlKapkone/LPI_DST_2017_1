package seminar6;

import java.util.Arrays;
import java.util.Scanner;

public class Client {

    public static boolean flug = true;

    public void start() {

        try (Scanner scanner = new Scanner(System.in)) {

            org.apache.activemq.ActiveMQConnectionFactory connectionFactory = 
                    new org.apache.activemq.ActiveMQConnectionFactory("tcp://localhost:61616");
            connectionFactory.setTrustedPackages(Arrays.asList("lpi.server.mq"));
            
            javax.jms.Connection connection = connectionFactory.createConnection();
            connection.start();

            javax.jms.Session session = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
            javax.jms.Session sessionReceiveMess = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
            javax.jms.Session sessionReceiveFile = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);

            System.out.println("Welcome to server");            

            Interpretator inter = new Interpretator(session, sessionReceiveMess, sessionReceiveFile);

            while (flug) {
                String inLine = scanner.nextLine().trim();

                if (!inLine.equals("")) {
                    inter.interpretator(inLine);
                }
            }
            connection.close();
            
        } catch(Exception ex){
            System.out.println("Connections problem");
        }
    }
}
