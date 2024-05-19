package es.ujaen.ssccdd.curso2023_24;
import javax.jms.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class Usuario implements Runnable, Constantes {

    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private MessageConsumer consumer;
    private int id;

    public Usuario(int id) throws JMSException {
        this.id = id;
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Destination destination = session.createQueue(DESTINO);
        producer = session.createProducer(destination);
        consumer = session.createConsumer(destination);

        connection.start();
    }

    public void run() {
        try {
            while (!finTarea()) {
                if (quiereReservarViaje()) {
                    sendConsultaDisponibilidad("consultaViaje");
                    Message message = consumer.receive();
                    if (message instanceof TextMessage && message.getBooleanProperty("respuestaDisponibilidad")) {
                        sendReservaViaje("reservaViaje");
                        if (quierePagoConCancelacion()) {
                            sendPagoConCancelacion("pagoConCancelacion");
                        } else {
                            sendPagoBasico("pagoBasico");
                        }
                    }
                } else if (quiereReservarEstancia()) {
                    sendConsultaDisponibilidad("consultaEstancia");
                    Message message = consumer.receive();
                    if (message instanceof TextMessage && message.getBooleanProperty("respuestaDisponibilidad")) {
                        sendReservaEstancia("reservaEstancia");
                        if (quierePagoConCancelacion()) {
                            sendPagoConCancelacion("pagoConCancelacion");
                        } else {
                            sendPagoBasico("pagoBasico");
                        }
                    }
                } else if (quiereCancelarReserva()) {
                    sendCancelacionReserva("cancelacionReserva");
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
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

    private void sendConsultaDisponibilidad(String consulta) throws JMSException {
        TextMessage message = session.createTextMessage(consulta);
        message.setStringProperty("tipo", "consultaDisponibilidad");
        producer.send(message);
    }

    private void sendReservaViaje(String reserva) throws JMSException {
        TextMessage message = session.createTextMessage(reserva);
        message.setStringProperty("tipo", "reservaViaje");
        producer.send(message);
    }

    private void sendReservaEstancia(String reserva) throws JMSException {
        TextMessage message = session.createTextMessage(reserva);
        message.setStringProperty("tipo", "reservaEstancia");
        producer.send(message);
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
