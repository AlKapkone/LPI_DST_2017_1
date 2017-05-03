package rmi_server;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.Instant;

public interface Compute extends Remote {

    public static final int PORT = 17414;
    public static final String SERVER_NAME = "rmi_server";

    public <T> T executeTask(Task<T> t) throws IOException, RemoteException;

    public String echo(String text) throws RemoteException;

    public String ping() throws RemoteException;



    public static class FileInfo implements Serializable {

        private static final long serialVersionUID = 229L;
        private byte[] fileContent;
        private String filename;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public FileInfo() {
        }

        public FileInfo(File file) throws IOException {
            fileContent = Files.readAllBytes(file.toPath());
            filename = file.getName();
        }

        public byte[] getFileContent() {
            return fileContent;
        }

        public void setFileContent(byte[] fileContent) {
            this.fileContent = fileContent;
        }
    }



    public class Algoritm implements Task<Compute.FileInfo>, Serializable {

        private static final long serialVersionUID = 227L;

        private final Compute.FileInfo fileInfo;
        private String[] stringMass;
        private double[] doubleMass;
        private File file;

        public Algoritm(FileInfo fileInfo) throws IOException {
            this.fileInfo = fileInfo;
        }

        @Override
        public Compute.FileInfo execute() throws IOException {
            toStringMass();
            toDoubleMass();
            sortMass();

            file = new File("sorted_" + fileInfo.getFilename());
            writeFile();

            return new Compute.FileInfo(file);
        }

        private void toStringMass() {
            String content = new String(fileInfo.getFileContent(), StandardCharsets.UTF_8).trim();
            stringMass = (content.equals("")) ? new String[]{} : content.split(" ");
        }

        private void toDoubleMass() {
            doubleMass = (stringMass.length == 0) ? new double[]{} : toDouble();
        }

        private double[] toDouble() {
            doubleMass = new double[stringMass.length];
            for (int i = 0; i < stringMass.length; i++) {
                doubleMass[i] = Double.parseDouble(stringMass[i]);
            }
            return doubleMass;
        }

        private void sortMass() {
            int time1 = (int) (Instant.now().getEpochSecond());

            for (int i = 1; i < doubleMass.length; i++) {

                int index = i;
                double value = doubleMass[i];

                while (index > 0 && doubleMass[index - 1] > value) {
                    doubleMass[index] = doubleMass[index - 1];
                    index--;
                }
                doubleMass[index] = value;
            }

            int time2 = (int) (Instant.now().getEpochSecond() - time1);
            System.out.printf("%tF %<tT %s %d %s %n", new Date(), " Sort lasted ", time2, " seconds");
        }

        private void writeFile() {

            if (doubleMass.length == 0) {
                return;
            }

            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
                for (double element : doubleMass) {
                    dos.writeBytes(String.format( Locale.US, "%.1f", element) + " ");
                }
            } catch (Exception ex) {
                System.out.println("Problem with write file");
            }
        }
    }
}
