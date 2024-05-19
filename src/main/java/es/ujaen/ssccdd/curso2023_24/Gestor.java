package es.ujaen.ssccdd.curso2023_24;
import javax.jms.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.List;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class Gestor implements Runnable, Constantes {

    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    private MessageProducer producer;
    private final ExecutorService ejecucion;
    private final List<Future<?>> listaTareas;



    public Gestor() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Destination destination = session.createQueue(DESTINO);
        consumer = session.createConsumer(destination);
        producer = session.createProducer(destination);
        this.ejecucion = Executors.newCachedThreadPool();
        this.listaTareas = new ArrayList();

        connection.start();
    }

    public void run() {
        for (Servicio servicioAgencia: SERVICIOS_AGENCIA) {
            switch (servicioAgencia) {
                case RESERVA_VIAJE:
                    String queueFinal = DESTINO_RESERVA_VIAJE;
                    ReservaEstancia reservaViaje = new ReservaEstancia(queueFinal);
                    listaTareas.add(ejecucion.submit(reservaViaje));
                    break;
                case RESERVA_ESTANCIA:
                    String queueFinal2 = DESTINO_RESERVA_ESTANCIA;
                    ReservaEstancia reservaEstancia = new ReservaEstancia(queueFinal2);
                    listaTareas.add(ejecucion.submit(reservaEstancia));
                    break;
            }
        }
        while (!finTarea()) {
        }
    }

    private boolean finTarea() {
        return false; // lógica para determinar si la tarea ha terminado
    }

    private void reservarViaje(String reserva) {
        // lógica para reservar viaje
        System.out.println("Reserva de viaje: " + reserva);
    }

    private void reservarEstancia(String reserva) {
        // lógica para reservar estancia
        System.out.println("Reserva de estancia: " + reserva);
    }

    private void efectuarPago(String pago) {
        // lógica para efectuar pago
        System.out.println("Efectuar pago: " + pago);
    }

    private void cancelarReserva(String reserva) {
        // lógica para cancelar reserva
        System.out.println("Cancelar reserva: " + reserva);
    }

    private boolean reservaExiste(String reservaId) {
        return true; // lógica para verificar si la reserva existe
    }

    private boolean reservaPagadaConCancelacion(String reservaId) {
        return true; // lógica para verificar si la reserva está pagada con cancelación
    }

    private void sendRespuestaDisponibilidad(String consulta, boolean disponible) throws JMSException {
        TextMessage response = session.createTextMessage(consulta);
        response.setBooleanProperty("respuestaDisponibilidad", disponible);
        response.setStringProperty("tipo", "respuestaDisponibilidad");
        producer.send(response);
    }

    private boolean consultarDisponibilidad() {
        // lógica para consultar disponibilidad
        return true;
    }
}
