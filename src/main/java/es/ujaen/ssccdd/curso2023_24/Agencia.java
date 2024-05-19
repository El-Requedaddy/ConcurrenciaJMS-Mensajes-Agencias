package es.ujaen.ssccdd.curso2023_24;
import javax.jms.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Agencia implements Runnable, Constantes {

    private final String iD;
    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private MessageConsumer consumer;
    private MessageProducer producerReservaViaje;
    private MessageProducer producerReservaEstancia;
    private ExecutorService executorService;
    private int i = 1;

    public Agencia() throws JMSException {
        iD = "1";
        executorService = Executors.newFixedThreadPool(10);
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Destination destination = session.createTopic(DESTINO_RESERVA_VIAJE);

        Destination destinationReservaViaje = session.createTopic(DESTINO_RESERVA_VIAJE);
        producerReservaViaje = session.createProducer(destinationReservaViaje);

        Destination destinationReservaEstancia = session.createTopic(DESTINO_RESERVA_ESTANCIA);
        producerReservaEstancia = session.createProducer(destinationReservaEstancia);

        connection.start();
    }

    public void run() {

        while (!finTarea() && !Thread.currentThread().isInterrupted()) {
            if (quiereReservarViaje()) {
                executorService.submit(() -> { // Un hilo espera a la respuesta
                    try {
                        sendConsultaDisponibilidad("consultaViaje");
                        Message message = consumer.receive();
                        if (message instanceof TextMessage && message.getBooleanProperty("respuestaDisponibilidad")) {
                            sendReservaViaje("reservaViaje");
                        }
                    } catch (JMSException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else if (quiereReservarEstancia()) {
                executorService.submit(() -> {
                    try {
                        sendReservaEstanciaAgencia("reservaEstancia");
                    } catch (JMSException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else if (quiereCancelarReserva()) {
                // ...
            }
        }

        // Asegúrate de cerrar el ExecutorService cuando ya no lo necesites
        executorService.shutdown();
    }

    private boolean finTarea() {
        return false; // lógica para determinar si la tarea ha terminado
    }

    private boolean quiereReservarViaje() {
        return false; // lógica para determinar si quiere reservar un viaje
    }

    private boolean quiereReservarEstancia() {
        return true; // lógica para determinar si quiere reservar una estancia
    }

    private boolean quiereCancelarReserva() {
        return false; // lógica para determinar si quiere cancelar una reserva
    }

    private boolean quierePagoConCancelacion() {
        return false; // lógica para determinar si quiere pago con cancelación
    }

    private void sendReservaViaje(String reserva) throws JMSException, InterruptedException {
        TextMessage message = session.createTextMessage(reserva);
        message.setStringProperty("tipo", "reservaViajeAgencia");
        producerReservaViaje.send(message);
        System.out.println("Agencia: Reserva de viaje enviada");
        TimeUnit.MILLISECONDS.sleep(5000);
    }

    private void sendReservaEstanciaAgencia(String reserva) throws JMSException, InterruptedException{
        TextMessage message = session.createTextMessage(reserva);
        message.setStringProperty("tipo", "reservaEstanciaAgencia");
        producerReservaEstancia.send(message);
        System.out.println("Agencia: Reserva de estancia enviada");
        TimeUnit.MILLISECONDS.sleep(5000);
    }

    private void sendConsultaDisponibilidad(String consulta) throws JMSException, InterruptedException {
        TextMessage message = session.createTextMessage(consulta);
        message.setStringProperty("tipo", "consultaDisponibilidad");
        producerReservaEstancia.send(message);
        System.out.println("Agencia: Reserva de estancia enviada");
        TimeUnit.MILLISECONDS.sleep(5000);
    }

    private void sendPagoBasico(String pago) throws JMSException {
        TextMessage message = session.createTextMessage(pago);
        message.setStringProperty("tipo", "PagoBasicoAgencia");
        producer.send(message);
    }

    private void sendPagoConCancelacion(String pago) throws JMSException {
        TextMessage message = session.createTextMessage(pago);
        message.setStringProperty("tipo", "PagoConCancelacionAgencia");
        producer.send(message);
    }

    private void sendCancelacionReserva(String reserva) throws JMSException {
        TextMessage message = session.createTextMessage(reserva);
        message.setStringProperty("tipo", "cancelacionReservaAgencia");
        producer.send(message);
    }

}
