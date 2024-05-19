package es.ujaen.ssccdd.curso2023_24;
import javax.jms.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Usuario implements Runnable, Constantes {

    private final String iD;
    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private MessageConsumer consumer;
    private MessageConsumer consumerReservaViaje;
    private MessageConsumer consumerReservaEstancia;
    private MessageConsumer consumerRespuestaDisponibilidadViaje;
    private MessageConsumer consumerRespuestaDisponibilidadEstancia;
    private MessageProducer producerReservaViaje;
    private MessageProducer producerReservaEstancia;
    private MessageProducer producerConsultaDisponibilidadViaje;
    private MessageProducer producerConsultaDisponibilidadEstancia;
    private ExecutorService executorService;

    public Usuario(int id) throws JMSException {
        this.iD = String.valueOf(id);
        executorService = Executors.newFixedThreadPool(10);
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        consumerReservaEstancia = session.createConsumer(session.createTopic(DESTINO_CONSULTA_DISPONIBILIDAD_ESTANCIA));
        consumerReservaViaje = session.createConsumer(session.createTopic(DESTINO_CONSULTA_DISPONIBILIDAD_VIAJE));
        consumerRespuestaDisponibilidadEstancia = session.createConsumer(session.createTopic(DESTINO_RESPUESTA_CONSULTA_DISPONIBILIDAD_ESTANCIA));
        consumerRespuestaDisponibilidadViaje = session.createConsumer(session.createTopic(DESTINO_RESPUESTA_CONSULTA_DISPONIBILIDAD_VIAJE));

        Destination destinationReservaViaje = session.createTopic(DESTINO_RESERVA_VIAJE);
        producerReservaViaje = session.createProducer(destinationReservaViaje);

        Destination destinationReservaEstancia = session.createTopic(DESTINO_RESERVA_ESTANCIA);
        producerReservaEstancia = session.createProducer(destinationReservaEstancia);

        Destination destinationConsultaDisponibilidadViaje = session.createTopic(DESTINO_CONSULTA_DISPONIBILIDAD_VIAJE);
        producerConsultaDisponibilidadViaje = session.createProducer(destinationConsultaDisponibilidadViaje);

        Destination destinationConsultaDisponibilidadEstancia = session.createTopic(DESTINO_CONSULTA_DISPONIBILIDAD_ESTANCIA);
        producerConsultaDisponibilidadEstancia = session.createProducer(destinationConsultaDisponibilidadEstancia);

        connection.start();
    }

    public void run() {

        while (!Thread.currentThread().isInterrupted()) {
            if (quiereReservarViaje()) {
                executorService.submit(() -> { // Un hilo espera a la respuesta
                    try {
                        sendConsultaDisponibilidadViaje("Usuario_Reserva_" + iD + "_" + Constantes.generarViajeAleatorio());
                        Message message = consumerRespuestaDisponibilidadViaje.receive();
                        System.out.println("Usuario: Recibida respuesta de disponibilidad de viaje");
                        if (message instanceof TextMessage && message.getBooleanProperty("respuestaDisponibilidad")) {
                            sendReservaViaje("reservaViaje");
                        }
                    } catch (JMSException | InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } else if (quiereReservarEstancia()) {
                executorService.submit(() -> {
                    try {
                        sendConsultaDisponibilidadEstancia("Usuario_Reserva_" + iD + "_" + Constantes.generarEstanciaAleatoria());
                        Message message = consumerRespuestaDisponibilidadEstancia.receive();
                        System.out.println("Usuario: Recibida respuesta de disponibilidad de estancia");
                        if (message instanceof TextMessage && message.getBooleanProperty("respuestaDisponibilidad")) {
                            sendReservaEstancia("reservaViaje");
                        }
                    } catch (JMSException | InterruptedException e) {
                        Thread.currentThread().interrupt();
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
                TimeUnit.MILLISECONDS.sleep(TIEMPO_ESPERA_USUARIO);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restaurar el estado interrumpido
            }

        }

        executorService.shutdown();
    }

    private boolean finTarea() {
        return false; // lógica para determinar si la tarea ha terminado
    }

    private boolean quiereReservarViaje() {
        return true; // lógica para determinar si quiere reservar un viaje
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
        message.setStringProperty("tipo", "reservaViajeUsuario");
        producerReservaViaje.send(message);
        System.out.println("Agencia: Reserva de viaje enviada");
        try {
            TimeUnit.MILLISECONDS.sleep(TIEMPO_ESPERA_SOLICITUD);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaurar el estado interrumpido
        }
    }

    private void sendReservaEstancia(String reserva) throws JMSException, InterruptedException{
        TextMessage message = session.createTextMessage(reserva);
        message.setStringProperty("tipo", "reservaEstanciaAgencia");
        producerReservaEstancia.send(message);
        System.out.println("Usuario: Reserva de estancia enviada");
        try {
            TimeUnit.MILLISECONDS.sleep(TIEMPO_ESPERA_SOLICITUD);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaurar el estado interrumpido
        }
    }

    private void sendConsultaDisponibilidadViaje(String consulta) throws JMSException, InterruptedException {
        TextMessage message = session.createTextMessage(consulta);
        message.setStringProperty("tipo", "consultaDisponibilidad");
        producerConsultaDisponibilidadViaje.send(message);
        System.out.println("Usuario: Reserva de viaje enviada");
        try {
            TimeUnit.MILLISECONDS.sleep(TIEMPO_ESPERA_SOLICITUD);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaurar el estado interrumpido
        }
    }

    private void sendConsultaDisponibilidadEstancia(String consulta) throws JMSException, InterruptedException {
        TextMessage message = session.createTextMessage(consulta);
        message.setStringProperty("tipo", "consultaDisponibilidad");
        producerConsultaDisponibilidadEstancia.send(message);
        System.out.println("Usuario: Reserva de estancia enviada");
        try {
            TimeUnit.MILLISECONDS.sleep(TIEMPO_ESPERA_SOLICITUD);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaurar el estado interrumpido
        }
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
