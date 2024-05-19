package es.ujaen.ssccdd.curso2023_24;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.JMSException;
import java.util.Deque;

public class MensajeListener implements MessageListener {
    private final Deque<String> queue;
    private final String queueName;

    public MensajeListener (Deque<String> queue, String queueName) {
        this.queue = queue;
        this.queueName = queueName;
    }

    @Override
    public void onMessage(Message message) { // Este método se ejecuta cuando llega un mensaje de semen
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                String text = textMessage.getText();
                synchronized (queue) {
                    queue.addLast(text);//meto al final de la cola
                }
                System.out.println("Mensaje recibido: " + text + " en la cola " + queueName);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Received non-text message");
        }
    }
}
