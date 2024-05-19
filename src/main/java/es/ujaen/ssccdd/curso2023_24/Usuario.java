package es.ujaen.ssccdd.curso2023_24;
import javax.jms.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Usuario implements Runnable, Constantes {

    private final String iD;
    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private MessageConsumer consumer;
    private MessageConsumer consumerReservaViaje;
    private MessageConsumer consumerReservaEstancia;
    private MessageProducer producerReservaViaje;
    private MessageProducer producerReservaEstancia;
    private ExecutorService executorService;

    public Usuario(int id) throws JMSException {
        this.iD = String.valueOf(id);
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        consumerReservaEstancia = session.createConsumer(session.createTopic(DESTINO_RESERVA_ESTANCIA));
        consumerReservaViaje = session.createConsumer(session.createTopic(DESTINO_RESERVA_VIAJE));

        Destination destinationReservaViaje = session.createTopic(DESTINO_RESERVA_VIAJE);
        producerReservaViaje = session.createProducer(destinationReservaViaje);

        Destination destinationReservaEstancia = session.createTopic(DESTINO_RESERVA_ESTANCIA);
        producerReservaEstancia = session.createProducer(destinationReservaEstancia);

        Destination destination = session.createQueue(DESTINO);
        producer = session.createProducer(destination);
        consumer = session.createConsumer(destination);

        connection.start();
    }

    public void run() {

        while (!finTarea() && !Thread.currentThread().isInterrupted()) {
            if (quiereReservarViaje()) {
                executorService.submit(() -> { // Un hilo espera a la respuesta
                    try {
                        sendConsultaDisponibilidad(iD + "_" + Constantes.generarViajeAleatorio());
                        Message message = consumerReservaViaje.receive();
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
                        sendConsultaDisponibilidad(iD + "_" + Constantes.generarEstanciaAleatoria());
                        Message message = consumerReservaEstancia.receive();
                        if (message instanceof TextMessage && message.getBooleanProperty("respuestaDisponibilidad")) {
                            sendReservaViaje("reservaViaje");
                        }
                    } catch (JMSException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else if (quiereCancelarReserva()) {
                executorService.submit(() -> {
                    try {
                        sendCancelacionReserva("usuario_" + iD + "_" + Constantes.getTipoCancelacionAleatorio());
                    } catch (JMSException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            // Simulamos tiempo de espera para volver a realizar consultas
            try {
                TimeUnit.MILLISECONDS.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        executorService.shutdown();
    }

    private boolean finTarea() {
        return false; // lógica para determinar si la tarea ha terminado
    }

    private boolean quiereReservarViaje() {
        return false; // lógica para determinar si quiere reservar un viaje
    }

    private boolean quiereReservarEstancia() {
        return false; // lógica para determinar si quiere reservar una estancia
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
        TimeUnit.MILLISECONDS.sleep(TIEMPO_ESPERA_SOLICITUD);
    }

    private void sendReservaEstancia(String reserva) throws JMSException, InterruptedException{
        TextMessage message = session.createTextMessage(reserva);
        message.setStringProperty("tipo", "reservaEstanciaAgencia");
        producerReservaEstancia.send(message);
        System.out.println("Usuario: Reserva de estancia enviada");
        TimeUnit.MILLISECONDS.sleep(TIEMPO_ESPERA_SOLICITUD);
    }

    private void sendConsultaDisponibilidad(String consulta) throws JMSException, InterruptedException {
        TextMessage message = session.createTextMessage(consulta);
        message.setStringProperty("tipo", "consultaDisponibilidad");
        producerReservaEstancia.send(message);
        System.out.println("Usuario: Reserva de estancia enviada");
        TimeUnit.MILLISECONDS.sleep(TIEMPO_ESPERA_SOLICITUD);
    }

    private void sendPagoBasico(String pago) throws JMSException {
        TextMessage message = session.createTextMessage(pago);
        message.setStringProperty("tipo", "PagoBasico");
        producer.send(message);
    }

    private void sendPagoConCancelacion(String pago) throws JMSException {
        TextMessage message = session.createTextMessage(pago);
        message.setStringProperty("tipo", "PagoConCancelacion");
        producer.send(message);
    }

    private void sendCancelacionReserva(String reserva) throws JMSException {
        TextMessage message = session.createTextMessage(reserva);
        message.setStringProperty("tipo", "cancelacionReserva");
        producer.send(message);
    }

}
