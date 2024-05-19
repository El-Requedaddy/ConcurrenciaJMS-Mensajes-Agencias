package es.ujaen.ssccdd.curso2023_24;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.*;

public class ReservaEstancia implements Runnable{
    private final String queue;
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    Deque<String> cola;

    public ReservaEstancia(String queue) {
        this.queue = queue;
        this.cola = new LinkedList<>();
    }

    @Override
    public void run() {
        System.out.println(" Acceso("+queue+") activado...");

        try {
            before();

            execution();
        } catch (Exception ex) {
            System.out.println(" Acceso("+queue+") finalizado..." + "\n" + ex);
        } finally {
            after();
        }
    }

    public void before() throws Exception {
        connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        destination = session.createTopic(queue);
    }

    public void after() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception ex) {
            // No hacer nada
        }
    }

    public void execution() throws Exception {
        TextMessage msg;
        MessageConsumer consumer = session.createConsumer(destination);
        consumer.setMessageListener(new MensajeListener(cola));
        connection.start();

        while (true) {
            if (cola.size() > 0) {
                System.out.println("Mensaje recibido: " + cola.peek());
                cola.removeLast();
            }
        }
    }

    private void reservarViaje(String codigoReserva) {
    }

    private String ProcesarOrigenMenasaje(TextMessage msg,String Reserva) {
        return "Agencia";
    }
}

