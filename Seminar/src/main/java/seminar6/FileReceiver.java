package seminar6;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
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
                    if (time > 30) {
                        sendAfk(fi.getSender(), time);
                    } else {
                        writeFile(fi);
                    }
                }
            } catch (JMSException ex) {
                Logger.getLogger(FileReceiver.class.getName()).log(Level.SEVERE, null, ex);
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

        try (FileOutputStream fos = new FileOutputStream(
                new File(fi.getSender() + "_" + fi.getFilename()))) {
            fos.write(fi.getFileContent());
        } catch (Exception ex) {
            System.out.println("Problem with write file");
        }
    }
}
