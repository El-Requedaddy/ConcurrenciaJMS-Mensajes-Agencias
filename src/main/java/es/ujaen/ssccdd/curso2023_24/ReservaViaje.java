package es.ujaen.ssccdd.curso2023_24;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.*;

public class ReservaViaje implements Runnable{
    private final String queue;
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    Deque<String> cola;

    public ReservaViaje(String queue) {
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

        int i = 0;
        boolean procesado = false;
        String mensajeReserva = "";


        Iterator<String> iterator = cola.iterator();
        while (iterator.hasNext() && i < 5) { // Procesamos los 5 primeros mensajes en búsqueda de petición de Agencia
            String mensajeActual = iterator.next();
            String tipoCliente = (String) ComprobarReserva(mensajeActual, mensajeReserva);// Obtengo el tipo de cliente y su mensaje de reserva
            if(tipoCliente.equals("Agencia")) {
                reservarViaje(mensajeReserva);
                procesado = true;
                i = 5;
            }
            System.out.println("Mensaje recibido: " + mensajeActual);
            i++;
        }

        if (!procesado) { // Si no se ha encontrado petición de Agencia, se procesa el primer mensaje
            ComprobarReserva(cola.peek(),mensajeReserva);
            reservarViaje(mensajeReserva);
            cola.pop();
            procesado = false;
            i = 0;
        }


        /*
        // Se confirma el acceso al puente
        Destination respuesta = session.createQueue(QUEUE + RESPUESTA.getValor() + coche.getName());
        MessageProducer producer = session.createProducer(respuesta);
        msg = session.createTextMessage(gsonUtil.encode(coche, MsgCoche.class));
        producer.send(msg);
        producer.close();*/
    }

    private Object ComprobarReserva(String cadena,String mensaje){
        String[] partes = cadena.split("_");

        // Imprimir las partes izquierda y derecha
        String izquierda = partes[0];
        mensaje = partes[1];
        if(izquierda.equals("Agencia")) {
            return "Agencia";
        }else{
            return "Usuario";
        }
    }

    private void reservarViaje(String codigoReserva) {
    }

    private String ProcesarOrigenMenasaje(TextMessage msg,String Reserva) {
        return "Agencia";
    }
}