package es.ujaen.ssccdd.curso2023_24;

import javax.jms.*;
import java.util.concurrent.*;
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
    private final CountDownLatch finControlador;
    private Resultado resultado;

    public Gestor(Resultado resultado, CountDownLatch finControlador) throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        this.finControlador = finControlador;

        Destination destination = session.createQueue(DESTINO);
        consumer = session.createConsumer(destination);
        producer = session.createProducer(destination);
        this.ejecucion = Executors.newCachedThreadPool();
        this.listaTareas = new ArrayList<>();
        this.resultado = resultado;

        connection.start();
    }

    @Override
    public void run() {
        try {
            for (Servicio servicioAgencia : SERVICIOS_AGENCIA) {
                switch (servicioAgencia) {
                    case RESERVA_VIAJE:
                        String queueFinal = DESTINO_RESERVA_VIAJE;
                        ReservaViaje reservaViaje = new ReservaViaje(queueFinal, resultado);
                        listaTareas.add(ejecucion.submit(reservaViaje));
                        break;
                    case RESERVA_ESTANCIA:
                        String queueFinal2 = DESTINO_RESERVA_ESTANCIA;
                        ReservaEstancia reservaEstancia = new ReservaEstancia(queueFinal2, resultado);
                        listaTareas.add(ejecucion.submit(reservaEstancia));
                        break;
                }
            }

            // Esperar a que se complete el CountDownLatch
            finControlador.await();

        } catch (InterruptedException | JMSException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Se solicita la finalización de las tareas de control
            for (Future<?> tarea : listaTareas) {
                tarea.cancel(true);
            }

            ejecucion.shutdown();
            try {
                if (!ejecucion.awaitTermination(TIEMPO_ESPERA_FINALIZACION, TimeUnit.MILLISECONDS)) {
                    ejecucion.shutdownNow();
                }
            } catch (InterruptedException ex) {
                ejecucion.shutdownNow();
                Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
            }
            close(); // Cerrar recursos JMS
        }
    }

    private void close() {
        try {
            if (consumer != null) {
                consumer.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
