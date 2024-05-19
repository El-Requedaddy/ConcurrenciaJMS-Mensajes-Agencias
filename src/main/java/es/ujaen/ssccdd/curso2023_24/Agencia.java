package es.ujaen.ssccdd.curso2023_24;
import javax.jms.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.Objects;
import java.util.Random;
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
    private MessageProducer producerPagoConCancelacionReserva;
    private MessageProducer producerPagoBasicoReserva;
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

        Destination destinationCancelacionReservaViaje = session.createTopic(DESTINO_CANCELACION_RESERVA);
        producerCancelacionReservaViaje = session.createProducer(destinationCancelacionReservaViaje);

        //Destination destinationCancelacionReservaEstancia = session.createTopic(DESTINO_CANCELACION_RESERVA_ESTANCIA);
        //producerCancelacionReservaEstancia = session.createProducer(destinationCancelacionReservaEstancia);

        Destination destinationPagoConCancelacionReserva = session.createTopic(DESTINO_PAGO_CANCELACION);
        producerPagoConCancelacionReserva = session.createProducer(destinationPagoConCancelacionReserva);

        Destination destinationPagoBasicoReserva = session.createTopic(DESTINO_PAGO_BASICO);
        producerPagoBasicoReserva = session.createProducer(destinationPagoBasicoReserva);

        connection.start();
    }

    public void run() {
        int c = 0;
        while (!finTarea() && !Thread.currentThread().isInterrupted()) {
            if (quiereReservarViaje()) {
                executorService.submit(() -> { // Un hilo espera a la respuesta
                    try {
                        sendConsultaDisponibilidadViaje("Agencia_Consulta_" + iD + "_" + Constantes.generarViajeAleatorio());
                        TextMessage message = (TextMessage) consumerConsultaDisponibilidadViaje.receive();
                        System.out.println("Agencia: Recibida respuesta de disponibilidad de viaje:::::::" + message.getText());
                        if (Objects.equals(message.getText(), "true")) {
                            sendReservaViaje("Agencia_Reserva_" + iD + "_" + Constantes.generarViajeAleatorio()+ "_" + "Codigoooo"+c);
                            if (quierePagoConCancelacion()) {
                                sendPagoConCancelacion("Agencia_PagoBasico_" + iD + "_" + Constantes.generarViajeAleatorio()+ "_" + "Codigoooo"+c + "_Cancelacion");
                            }else {
                                sendPagoBasico("Agencia_PagoConCancelacion_" + iD + "_" + Constantes.generarViajeAleatorio()+ "_" + "Codigoooo"+c + "_Basico");
                            }
                        }
                    } catch (JMSException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else if (quiereReservarEstancia()) {
                executorService.submit(() -> {
                    try {
                        sendConsultaDisponibilidadEstancia("Agencia_Consulta_" + iD + "_" + Constantes.generarViajeAleatorio());
                        TextMessage message = (TextMessage) consumerConsultaDisponibilidadEstancia.receive();
                        System.out.println("Agencia: Recibida respuesta de disponibilidad de estancia____" + message.getText());
                        if (Objects.equals(message.getText(), "true")) {
                            sendReservaEstanciaAgencia("Agencia_Reserva_" + iD + "_" + Constantes.generarViajeAleatorio()+ "_" + "EstanciaCode"+c);
                        }
                    } catch (JMSException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            if (quiereCancelarReserva()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(6000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                executorService.submit(() -> {
                    try {
                        System.out.println("Agencia: PEtición de cancelación de reserva");
                        sendCancelacionReserva("Agencia_Cancelacion_" + iD + "_" + Constantes.getTipoCancelacionAleatorio() + "_Codigoooo"+c);
                    } catch (JMSException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            // Simulamos tiempo de espera para volver a realizar consultas
            try {
                TimeUnit.MILLISECONDS.sleep(TIEMPO_ESPERA_AGENCIA);
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
        Random random = new Random();
        int probabilidad = random.nextInt(100); // Genera un número aleatorio entre 0 y 99

        if (probabilidad < 70)
            return true; // lógica para determinar si quiere cancelar una reserva
        return false;
    }

    private boolean quiereReservarEstancia() {
        Random random = new Random();
        int probabilidad = random.nextInt(100); // Genera un número aleatorio entre 0 y 99

        if (probabilidad < 70)
            return true; // lógica para determinar si quiere cancelar una reserva
        return false;
    }

    private boolean quiereCancelarReserva() {
        Random random = new Random();
        int probabilidad = random.nextInt(100); // Genera un número aleatorio entre 0 y 99

        if (probabilidad < 10)
            return true; // lógica para determinar si quiere cancelar una reserva
        return false;
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

    private void sendReservaEstanciaAgencia(String reserva) throws JMSException, InterruptedException{
        TextMessage message = session.createTextMessage(reserva);
        message.setStringProperty("tipo", "reservaEstanciaAgencia");
        producerReservaEstancia.send(message);
        //System.out.println("Agencia: Reserva de estancia enviada");
        TimeUnit.MILLISECONDS.sleep(TIEMPO_ESPERA_SOLICITUD);
    }

    private void sendConsultaDisponibilidadViaje(String consulta) throws JMSException, InterruptedException {
        TextMessage message = session.createTextMessage(consulta);
        message.setStringProperty("tipo", "consultaDisponibilidad");
        producerConsultaDisponibilidadViaje.send(message);
        //System.out.println("Agencia: Reserva de estancia enviada");
        TimeUnit.MILLISECONDS.sleep(TIEMPO_ESPERA_SOLICITUD);
    }

    private void sendConsultaDisponibilidadEstancia(String consulta) throws JMSException, InterruptedException {
        TextMessage message = session.createTextMessage(consulta);
        message.setStringProperty("tipo", "consultaDisponibilidad");
        producerConsultaDisponibilidadEstancia.send(message);
        //System.out.println("Agencia: Reserva de estancia enviada");
        TimeUnit.MILLISECONDS.sleep(TIEMPO_ESPERA_SOLICITUD);
    }

    private void sendPagoBasico(String pago) throws JMSException {
        TextMessage message = session.createTextMessage(pago);
        message.setStringProperty("tipo", "PagoBasicoAgencia");
        producerPagoBasicoReserva.send(message);
    }

    private void sendPagoConCancelacion(String pago) throws JMSException {
        TextMessage message = session.createTextMessage(pago);
        message.setStringProperty("tipo", "PagoConCancelacionAgencia");
        producerPagoConCancelacionReserva.send(message);
    }

    private void sendCancelacionReserva(String reserva) throws JMSException{
        TextMessage message = session.createTextMessage(reserva);
        message.setStringProperty("tipo", "cancelacionReserva");
        producerCancelacionReservaViaje.send(message);
        //System.out.println("Agencia: Reserva de estancia enviada");
    }

}
