package seminar6;

import java.io.*;
import javax.jms.*;
import java.time.Instant;
import lpi.server.mq.FileInfo;

public class FileReceiver extends MessageReceiver {

    public FileReceiver(Session sessionReceive) {
        super(sessionReceive);
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof ObjectMessage) {

            try {
                int time = (int) (Instant.now().getEpochSecond() - CommandProcessing.timestamp.getEpochSecond());
                ObjectMessage response = (ObjectMessage) message;

                Serializable fileInfoSerializable = response.getObject();
                FileInfo fi = (FileInfo) fileInfoSerializable;

                if (fi instanceof FileInfo) {
                    if (time > 60*5) {
                        sendAfk(fi.getSender(), time);
                    } else {
                        writeFile(fi);
                    }
                }
            } catch (JMSException ex) {
            }
        }
    }

    private void writeFile(FileInfo fi) {
        if (fi.getFileContent() == null) {
            System.out.println("No file content");
            return;
        }
        System.out.println("\nNew file\nFrom: " + fi.getSender()
                + "\nFile name : \"" + fi.getFilename() + "\"\n");

        File file = new File(fi.getSender() + "_" + fi.getFilename());
        
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fi.getFileContent());
        } catch (Exception ex) {
            System.out.println("Problem with write file");
        }
    }
}
