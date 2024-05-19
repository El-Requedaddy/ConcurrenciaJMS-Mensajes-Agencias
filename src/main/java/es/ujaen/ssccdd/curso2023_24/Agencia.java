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
    private MessageConsumer consumerReservaViaje;
    private MessageConsumer consumerReservaEstancia;
    private MessageConsumer consumerConsultaDisponibilidadViaje;
    private MessageConsumer consumerConsultaDisponibilidadEstancia;
    private MessageProducer producerReservaViaje;
    private MessageProducer producerReservaEstancia;
    private MessageProducer producerConsultaDisponibilidadViaje;
    private MessageProducer producerConsultaDisponibilidadEstancia;
    private MessageProducer producerCancelacionReservaViaje;
    private MessageProducer producerCancelacionReservaEstancia;
    private ExecutorService executorService;
    private int i = 1;

    public Agencia() throws JMSException {
        iD = "1";
        executorService = Executors.newFixedThreadPool(10);
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        consumerReservaEstancia = session.createConsumer(session.createTopic(DESTINO_CONSULTA_DISPONIBILIDAD_ESTANCIA));
        consumerReservaViaje = session.createConsumer(session.createTopic(DESTINO_CONSULTA_DISPONIBILIDAD_VIAJE));
        consumerConsultaDisponibilidadEstancia = session.createConsumer(session.createTopic(DESTINO_RESPUESTA_CONSULTA_DISPONIBILIDAD_ESTANCIA));
        consumerConsultaDisponibilidadViaje = session.createConsumer(session.createTopic(DESTINO_RESPUESTA_CONSULTA_DISPONIBILIDAD_VIAJE));

        // Se crean los productores para enviar mensajes
        Destination destinationReservaViaje = session.createTopic(DESTINO_RESERVA_VIAJE);
        producerReservaViaje = session.createProducer(destinationReservaViaje);

        Destination destinationReservaEstancia = session.createTopic(DESTINO_RESERVA_ESTANCIA);
        producerReservaEstancia = session.createProducer(destinationReservaEstancia);

        Destination destinationConsultaDisponibilidadViaje = session.createTopic(DESTINO_CONSULTA_DISPONIBILIDAD_VIAJE);
        producerConsultaDisponibilidadViaje = session.createProducer(destinationConsultaDisponibilidadViaje);

        Destination destinationConsultaDisponibilidadEstancia = session.createTopic(DESTINO_CONSULTA_DISPONIBILIDAD_ESTANCIA);
        producerConsultaDisponibilidadEstancia = session.createProducer(destinationConsultaDisponibilidadEstancia);

        Destination destinationCancelacionReservaViaje = session.createTopic(DESTINO_CANCELACION_RESERVA_VIAJE);
        producerCancelacionReservaViaje = session.createProducer(destinationCancelacionReservaViaje);

        Destination destinationCancelacionReservaEstancia = session.createTopic(DESTINO_CANCELACION_RESERVA_ESTANCIA);
        producerCancelacionReservaEstancia = session.createProducer(destinationCancelacionReservaEstancia);

        connection.start();
    }

    public void run() {

        while (!Thread.currentThread().isInterrupted()) {
            if (quiereReservarViaje()) {
                executorService.submit(() -> { // Un hilo espera a la respuesta
                    try {
                        sendConsultaDisponibilidadViaje("Agencia_Consulta_" + iD + "_" + Constantes.generarViajeAleatorio());
                        Message message = consumerConsultaDisponibilidadViaje.receive();
                        System.out.println("Agencia: Recibida respuesta de disponibilidad de viaje");
                        if (message instanceof TextMessage && message.getBooleanProperty("respuestaDisponibilidad")) {
                            System.out.println("Agencia: Existe disponibilidad de viaje y procede a pedir reserva");
                            sendReservaViaje("reservaViaje");
                        }
                    } catch (JMSException | InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } else if (quiereReservarEstancia()) {
                executorService.submit(() -> {
                    try {
                        sendConsultaDisponibilidadEstancia("Agencia_Consulta_" + iD + "_" + Constantes.generarEstanciaAleatoria());
                        Message message = consumerConsultaDisponibilidadEstancia.receive();
                        System.out.println("Agencia: Recibida respuesta de disponibilidad de estancia");
                        if (message instanceof TextMessage && message.getBooleanProperty("respuestaDisponibilidad")) {
                            sendReservaEstanciaAgencia("reservaViaje");
                        }
                    } catch (JMSException | InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } else if (quiereCancelarReserva()) {
                executorService.submit(() -> {
                    try {
                        sendCancelacionReserva("Agencia_" + iD + "_" + Constantes.getTipoCancelacionAleatorio());
                    } catch (JMSException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            // Simulamos tiempo de espera para volver a realizar consultas
            try {
                TimeUnit.MILLISECONDS.sleep(TIEMPO_ESPERA_AGENCIA);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (Thread.currentThread().isInterrupted()) {
                executorService.shutdown();
                break;
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
        try {
            TimeUnit.MILLISECONDS.sleep(TIEMPO_ESPERA_SOLICITUD);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaurar el estado interrumpido
        }
    }

    private void sendReservaEstanciaAgencia(String reserva) throws JMSException, InterruptedException{
        TextMessage message = session.createTextMessage(reserva);
        message.setStringProperty("tipo", "reservaEstanciaAgencia");
        producerReservaEstancia.send(message);
        //System.out.println("Agencia: Reserva de estancia enviada");
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
        //System.out.println("Agencia: Reserva de estancia enviada");
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
        //System.out.println("Agencia: Reserva de estancia enviada");
        try {
            TimeUnit.MILLISECONDS.sleep(TIEMPO_ESPERA_SOLICITUD);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaurar el estado interrumpido
        }
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

    }

}
