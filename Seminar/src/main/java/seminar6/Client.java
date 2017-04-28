package seminar6;

import java.io.Closeable;
import java.util.*;
import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class Client implements Closeable {

    private final String SERVER_URL = "tcp://localhost:61616";
    private final String SERVER_NAME = "lpi.server.mq";

    public static boolean flug = true;

    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Interpretator interetator;
    private final List<Session> sessionsList = new LinkedList<>();

    public void start() {

        try (Scanner scanner = new Scanner(System.in)) {
            prepareToStart();

            interetator = new Interpretator(sessionsList);
            System.out.println("Welcome to server");

            comandExecute(scanner);

        } catch (Exception ex) {
            System.out.println("Connections problem");
        }
    }

    private void prepareToStart() throws JMSException {
        creataeConnectionFactory();
        createConnection();
        createSessionsAndAddToList();
    }

    private void creataeConnectionFactory() {
        connectionFactory = new ActiveMQConnectionFactory(SERVER_URL);
        connectionFactory.setTrustedPackages(Arrays.asList(SERVER_NAME));
    }

    private void createConnection() throws JMSException {
        connection = connectionFactory.createConnection();
        connection.start();
    }

    private void createSessionsAndAddToList() throws JMSException {
        //session, sessionReceiveMess, sessionReceiveFile
        for (int i = 0; i < 3; i++) {
            sessionsList.add(connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE));
        }
    }

    private void comandExecute(Scanner scanner) {
        while (flug) {
            interetator.interpretator(scanner.nextLine().trim());
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }

}
