package es.ujaen.ssccdd.curso2023_24;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReservaViaje implements Runnable{
    private final String queue;
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    Deque<String> cola;
    ExecutorService executor;
    HashMap<Constantes.Viajes, List<Reserva>> reservasEEDD;

    public ReservaViaje(String queue) {
        this.queue = queue;
        this.cola = new LinkedList<>();
        executor = Executors.newFixedThreadPool(10);
        reservasEEDD = new HashMap<Constantes.Viajes, List<Reserva>>();
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
        String tipoPeticion = "";
        String tipoCliente = "";
        String peticionAProcesar="";
        Iterator<String> iterator = cola.iterator();

        while (iterator.hasNext() && i < 5) { // Procesamos los 5 primeros mensajes en búsqueda de petición de Agencia
            String mensajeActual = iterator.next(); // iterrar sobre la cola
            ComprobarReserva(mensajeActual, tipoCliente, tipoPeticion);// Obtengo el tipo de cliente y el tipo de petición
            if(tipoCliente == "Agencia") {
                peticionAProcesar = mensajeActual;
                procesado = true;
                i = 5;
               iterator.remove(); // elimino la petición de la cola
            }
            System.out.println("Mensaje recibido: " + mensajeActual);
            i++;
        }

        if (!procesado) { // Si no se ha encontrado petición de Agencia, se procesa el primer mensaje
            peticionAProcesar = cola.peek();
            cola.pop();
            procesado = false;
            i = 0;
        }

        if (peticionAProcesar != "")
            procesarPeticion(peticionAProcesar);
        else
            System.out.println("No hay mensajes en la cola");

        /*
        // Se confirma el acceso al puente
        Destination respuesta = session.createQueue(QUEUE + RESPUESTA.getValor() + coche.getName());
        MessageProducer producer = session.createProducer(respuesta);
        msg = session.createTextMessage(gsonUtil.encode(coche, MsgCoche.class));
        producer.send(msg);
        producer.close();*/
    }

    private void procesarPeticion(String peticionAProcesar) {
        String tipoPeticion = "", tipoCliente = "";
        if (peticionAProcesar != "") {
            ComprobarReserva(peticionAProcesar, tipoCliente, tipoPeticion);
            switch (tipoPeticion) {
                case "Reserva":
                    reservarViaje(peticionAProcesar);
                    break;
                case "Cancelacion":
                    //cancelarReserva(peticionAProcesar);
                    break;
                case "Consulta":
                    //consultarDisponibilidad(peticionAProcesar);
                    break;
                case "Pago":
                    //efectuarPago(peticionAProcesar);
                    break;
                default:
                    System.out.println("Petición no reconocida");
                    break;
            }
        }
    }

    private void ComprobarReserva(String cadena,String tipoCliente, String tipoPeticion) {
        String[] partes = cadena.split("_");

        // Imprimir las partes izquierda y derecha
        tipoCliente = partes[0];
        tipoPeticion = partes[1];

    }

    private void reservarViaje(String cadena) {
        String[] partes = cadena.split("_");

        String tipoCliente = partes[0];
        String tipoPeticion = partes[1];
        String idCliente = partes[2];
        String viaje = partes[3];
        String codigoReserva = UUID.randomUUID().toString();

        TareaReservaViaje tarea = new TareaReservaViaje(codigoReserva, tipoCliente, idCliente, reservasEEDD, Constantes.Viajes.valueOf(viaje));
        executor.submit(tarea); // creo una tarea para reservar el viaje

    }

    private void cancelarReserva(String peticionAProcesar) {
        // lógica para cancelar reserva
        String[] partes = peticionAProcesar.split("_");

        String tipoCliente = partes[0];
        String tipoPeticion = partes[1];
        String idCliente = partes[2];
        String viaje = partes[3];
        String codigoReserva = partes[4 ];

        executor.submit(() -> { // Un hilo para cancelar la reserva
            synchronized (reservasEEDD) { // exclusión mutua sobre la estructura de datos
                System.out.println("Tarea de cancelación reserva comenzada: " + peticionAProcesar);
                List<Reserva> reservasViaje = reservasEEDD.get(Constantes.Viajes.valueOf(viaje));
                if (reservasViaje != null) { // Si hay reservas
                    for (Iterator<Reserva> iterator = reservasViaje.iterator(); iterator.hasNext();) {
                        Reserva reserva = iterator.next();
                        if (reserva.getCodigoReserva().equals(codigoReserva)) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                System.out.println("reserva cancelada: " + peticionAProcesar);
            }
        });

    }

    private String ProcesarOrigenMenasaje(TextMessage msg,String Reserva) {
        return "Agencia";
    }
}