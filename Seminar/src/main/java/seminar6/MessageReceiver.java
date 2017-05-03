package seminar6;

import java.time.Instant;
import javax.jms.*;

class MessageReceiver implements MessageListener {

    private final Session sessionReceive;
    private MessageConsumer consumer = null;
    private MessageProducer producer = null;

    MessageReceiver(Session sessionReceive) {
        this.sessionReceive = sessionReceive;
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof MapMessage) {
                int time = (int) (Instant.now().getEpochSecond() - CommandProcessing.timestamp.getEpochSecond());

                MapMessage request = (MapMessage) message;
                String sender = request.getString("sender");
                if (time > 60*5) {
                    sendAfk(sender, time);
                } else {
                    String messag = request.getString("message");
                    System.out.println("\nNew message\nFrom : " + sender + "\nText : " + messag + "\n");
                }
            }
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }

    public void receive(String brokerUri) throws JMSException {
        MapMessage request = sessionReceive.createMapMessage();

        Destination queue = sessionReceive.createQueue(brokerUri);

        producer = sessionReceive.createProducer(queue);
        consumer = sessionReceive.createConsumer(queue);

        consumer.setMessageListener(this);

        request.setJMSReplyTo(queue);
        producer.send(request);
    }

    protected void sendAfk(String sender, int time) throws JMSException {
        MessageProducer producerReq = null;

        try {
            MapMessage request = sessionReceive.createMapMessage();

            request.setString("receiver", sender);
            request.setString("message", "Destination client is afk, last action " + time/60 + " minuts ago");
                        
            Destination targetQueue = sessionReceive.createQueue("chat.sendMessage");
            Destination replyQueue = this.sessionReceive.createTemporaryQueue();

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
