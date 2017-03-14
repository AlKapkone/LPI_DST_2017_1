package seminar6;

import java.time.Instant;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;

class MessageReceiver implements MessageListener {

    private final javax.jms.Session sessionReceive;
    private MessageConsumer consumer = null;
    private MessageProducer producer = null;

    MessageReceiver(javax.jms.Session sessionReceive) {
        this.sessionReceive = sessionReceive;
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof MapMessage) {
                int time = (int) (Instant.now().getEpochSecond() - CommandProcessing.timestamp.getEpochSecond());

                MapMessage mapMsg = (MapMessage) message;
                String sender = mapMsg.getString("sender");
                if (time > 60*5) {
                    sendAfk(sender, time);
                } else {
                    String messag = mapMsg.getString("message");
                    System.out.println("\nNew message\nFrom : " + sender + "\nText : " + messag + "\n");
                }
            }
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }

    public void receive(String brokerUri) throws JMSException {
        javax.jms.MapMessage request = sessionReceive.createMapMessage();

        Destination queue = sessionReceive.createQueue(brokerUri);

        producer = sessionReceive.createProducer(queue);
        consumer = sessionReceive.createConsumer(queue);

        consumer.setMessageListener(this);

        request.setJMSReplyTo(queue);
        producer.send(request);
    }

    public void sendAfk(String sender, int time) throws JMSException {
        javax.jms.MessageProducer producerReq = null;

        try {
            javax.jms.MapMessage request = sessionReceive.createMapMessage();

            request.setString("receiver", sender);
            request.setString("message", "Destination client is afk, last action " + time/60 + " minuts ago");
                        
            javax.jms.Destination targetQueue = sessionReceive.createQueue("chat.sendMessage");
            javax.jms.Destination replyQueue = this.sessionReceive.createTemporaryQueue();

            producerReq = sessionReceive.createProducer(targetQueue);

            request.setJMSReplyTo(replyQueue);
            producerReq.send(request);

        } catch (JMSException ex) {
            ex.printStackTrace();
            System.out.println("connections problem");
        } finally {
            producerReq.close();
        }
    }

    public void exit() throws JMSException {
        producer.close();
        consumer.close();
    }
}
