package seminar2;

import java.io.*;

public class Interpretator {

    private String comand = null;

    public void setComand(String comand) {
        this.comand = comand;
    }

    public String getComand() {
        return comand;
    }

    private final CommandProcessingToServer comToServer;
    private final ComandProcessingFromServer comFromServer;
    private final Parser parser;

    public Interpretator() {
        this.comToServer = new CommandProcessingToServer();
        this.comFromServer = new ComandProcessingFromServer();
        this.parser = new Parser();
    }

    public byte[] prepareToSend(String inLine){

        String[] comandMas = parser.parsForComand(inLine);
        setComand(comandMas[0]);
        
        switch (getComand()) {
            case "ping":
                return comToServer.pingToServer(comandMas[0]);

            case "eho":
                return comToServer.ehoToServer(comandMas);

            case "login":
                return comToServer.loginToServer(comandMas);

            case "list":
                return comToServer.listToServer(comandMas);

            case "msg":
                return comToServer.msgToServer(comandMas);

            case "file":
                return comToServer.fileToServer(comandMas);

            case "receivemsg":
                return comToServer.receiveMsgToServer();

            case "receivefile":
                return comToServer.receiveFileToServer();

            default:
                System.out.println("No this comand");
                return null;
        }
    }

    public void executeRespons(byte[] serverResp) {
        if (serverResp != null) {
            try {
                switch (getComand()) {
                    case "ping":
                        comFromServer.pingFromServer(serverResp);
                        break;

                    case "eho":
                        comFromServer.ehoFromServer(serverResp);
                        break;

                    case "login":
                        comFromServer.loginFromServer(serverResp);
                        break;

                    case "list":
                        comFromServer.listFromServer(serverResp);
                        break;

                    case "msg":
                        comFromServer.msgFromServer(serverResp);
                        break;

                    case "file":
                        comFromServer.fileFromServer(serverResp);
                        break;

                    case "receivemsg":
                        comFromServer.receiveMsg(serverResp);
                        break;

                    case "receivefile":
                        comFromServer.receiveFile(serverResp);
                        break;
                }
            } catch (ClassNotFoundException | IOException ex) {
                ex.printStackTrace();
                System.out.println("problem with interpretation responds");
            }
        }
    }       
}
