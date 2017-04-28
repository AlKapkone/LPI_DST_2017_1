package seminar6;

import java.util.List;
import javax.jms.Session;

public class Interpretator {

    private final CommandProcessing comandProcess;
    private final Parser parser = new Parser();

    public Interpretator(List<Session> sessions) {
        comandProcess = new CommandProcessing(sessions);
    }

    public void interpretator(String inLine) {

        String[] comandMas = parser.parsForComand(inLine);
        String command = comandMas[0];

        switch (command) {

            case "ping":
                comandProcess.ping();
                break;

            case "echo":
                comandProcess.echo(comandMas);
                break;

            case "login":
                comandProcess.login(comandMas);
                break;

            case "list":
                comandProcess.list();
                break;

            case "msg":
                comandProcess.msg(comandMas);
                break;

            case "file":
                comandProcess.file(comandMas);
                break;

            case "exit":
                comandProcess.exit();
                break;

            default:
                System.out.println("No this comand");
                break;
        }
    }
}
