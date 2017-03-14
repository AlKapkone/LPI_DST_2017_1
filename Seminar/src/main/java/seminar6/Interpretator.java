package seminar6;

import javax.jms.Session;


public class Interpretator {
    private final CommandProcessing comP;
    private final Parser parser = new Parser(); 
    
    public Interpretator(Session session, Session sessionReceiveMess, Session sessionReceiveFile) {
        comP = new CommandProcessing(session, sessionReceiveMess, sessionReceiveFile);        
    }
    
    public void interpretator(String inLine){

        String[] comandMas = parser.parsForComand(inLine);

        try {
            switch (comandMas[0]) {

                case "ping":
                    comP.ping();
                    break;

                case "echo":
                    comP.echo(comandMas);
                    break;

                case "login":
                    comP.login(comandMas);
                    break;

                case "list":
                    comP.list();
                    break;

                case "msg":
                    comP.msg(comandMas);
                    break;

                case "file":
                    comP.file(comandMas);
                    break;

                case "exit":
                    comP.exit();
                    break;

                default:
                    System.out.println("No this comand");
                    break;
            }
        } catch (Exception ex) {
            System.out.println("Interpretator problem");
        }
    }
}
