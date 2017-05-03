package kurs_rmi_client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.Locale;
import rmi_server.Compute;
import rmi_server.Compute.Algoritm;
import rmi_server.Compute.FileInfo;

public class CommandProcessing {

    private static final int VALID_LENGTH_ECHO_PARAMETERS = 2;
    private static final int VALID_LENGTH_TASK_PARAMETERS = 2;
    private static final int SIZE_ELEMENTS = 1_000_000;
    private static final int MAX_VALUE_FOR_ELEMENTS = 1_000_000_000;

    private final Compute remoteCompute;

    public CommandProcessing(Compute remoteCompute) {
        this.remoteCompute = remoteCompute;
    }

    public void avaibleComand() {
        System.out.println("ping, echo (any_text), sort fileName\n");
    }

    public void exit() throws RemoteException {
        System.out.println("Exit from server");
        StartClient.working = false;
    }

    public void ping() throws RemoteException {
        System.out.println(remoteCompute.ping());
    }

    public void echo(String[] parameters) throws RemoteException {
        if (isValidNumberOfParameter(parameters.length, VALID_LENGTH_ECHO_PARAMETERS)) {
            System.out.println(remoteCompute.echo(parameters[1]));
        }
    }

    public void sort(String[] parameters) throws RemoteException, IOException {
        if (isValidNumberOfParameter(parameters.length, VALID_LENGTH_TASK_PARAMETERS)) {
            FileInfo sendedFileInfo = fillFile(parameters[1]);

            respondsProcessing(remoteCompute.executeTask(new Algoritm(sendedFileInfo)));
            System.out.println("Sort successfully");
        }
    }

    private boolean isValidNumberOfParameter(int ComandMasLength, int validLength) {
        return (ComandMasLength == validLength) ? true : badLength();
    }
    
    private boolean badLength(){
        System.out.println("Bad argument");
        return false;
    }

    private FileInfo fillFile(String fileName) throws IOException {
        String[] stringMass = new String[SIZE_ELEMENTS];
        for (int i = 0; i < stringMass.length; i++) {
            double value = Math.random() * MAX_VALUE_FOR_ELEMENTS;
            stringMass[i] = String.format(Locale.US, "%.1f", value);
        }
        return new FileInfo(writeFile(fileName, stringMass));
    }

    private void respondsProcessing(FileInfo fileInfo) {
        String content = new String(fileInfo.getFileContent(), StandardCharsets.UTF_8);
        writeFile(fileInfo.getFilename(), content.split(" "));
    }

    private File writeFile(String fileName, String[] writeMass) {
        File file = new File(fileName);
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            for (String element : writeMass) {
                dos.writeBytes(element + " ");
            }
        } catch (Exception ex) {
            System.out.println("Problem with write file");
        }
        return file;
    }
}
