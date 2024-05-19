package es.ujaen.ssccdd.curso2023_24;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReservaViaje implements Runnable, Constantes{
    Resultado resultado;
    private final String queue;
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    Deque<String> colaReserva;
    Deque<String> colaCancelacion;
    Deque<String> colaPago;
    Deque<String> colaConsulta;
    Deque<String> colaPagoCancelacion;
    ExecutorService executor;
    HashMap<Constantes.Viajes, List<Reserva>> reservasEEDD;

    // Mensajería y buffers
    MessageConsumer consumer;
    MessageConsumer consumerCancelacion;
    private MessageProducer producerConsultaDisponibilidadViaje;
    private MessageProducer producerConsultaDisponibilidadEstancia;
    Lock lock;

    public ReservaViaje(String queue, Resultado resultado) throws JMSException {
        this.queue = queue;
        this.colaReserva = new LinkedList<>();
        this.colaCancelacion = new LinkedList<>();
        this.colaPago = new LinkedList<>();
        this.colaPagoCancelacion = new LinkedList<>();
        this.colaConsulta = new LinkedList<>();
        executor = Executors.newFixedThreadPool(10);
        lock = new ReentrantLock();
        reservasEEDD = new HashMap<Constantes.Viajes, List<Reserva>>();
        reservasEEDD.put(Viajes.VIAJE1, new ArrayList<>());
        reservasEEDD.put(Viajes.VIAJE2, new ArrayList<>());
        reservasEEDD.put(Viajes.VIAJE3, new ArrayList<>());
        reservasEEDD.put(Viajes.VIAJE4, new ArrayList<>());
        reservasEEDD.put(Viajes.VIAJE5, new ArrayList<>());

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Destination destinationCancelacionReservaViaje = session.createTopic(DESTINO_RESPUESTA_CONSULTA_DISPONIBILIDAD_VIAJE);
        producerConsultaDisponibilidadViaje = session.createProducer(destinationCancelacionReservaViaje);

        Destination destinationCancelacionReservaEstancia = session.createTopic(DESTINO_RESPUESTA_CONSULTA_DISPONIBILIDAD_ESTANCIA);
        producerConsultaDisponibilidadEstancia = session.createProducer(destinationCancelacionReservaEstancia);

        connection.start();
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
                resultado.addReservaViaje(reservasEEDD);
                executor.shutdown();
                consumer.close();
                connection.close();
            }
        } catch (Exception ex) {
            // No hacer nada
        }
    }

    public void execution() throws Exception {
        System.out.println(" Acceso("+queue+") en ejecución...");
        TextMessage msg;
        consumer = session.createConsumer(session.createTopic(DESTINO_RESERVA_VIAJE));
        consumer.setMessageListener(new MensajeListener(colaReserva,"Reserva")); // listener para la cola de reserva
        connection.start();

        consumerCancelacion = session.createConsumer(session.createTopic(DESTINO_CANCELACION_RESERVA_VIAJE));
        consumerCancelacion.setMessageListener(new MensajeListener(colaCancelacion,"Cancelacion")); // listener para la cola de cancelación
        connection.start();

        MessageConsumer consumerPago = session.createConsumer(session.createTopic(DESTINO_PAGO_BASICO));
        consumerPago.setMessageListener(new MensajeListener(colaPago,"PAgo")); // listener para la cola de pago
        connection.start();

        MessageConsumer consumerPagoCancelacion = session.createConsumer(session.createTopic(DESTINO_PAGO_CANCELACION));
        consumerPagoCancelacion.setMessageListener(new MensajeListener(colaPagoCancelacion,"PAgoCancelacion")); // listener para la cola de pago cancelación
        connection.start();

        MessageConsumer consumerConsulta = session.createConsumer(session.createTopic(DESTINO_CONSULTA_DISPONIBILIDAD_VIAJE));
        consumerConsulta.setMessageListener(new MensajeListener(colaConsulta,"Consulta")); // listener para la cola de consulta
        connection.start();


        while (!Thread.currentThread().isInterrupted()) {
            synchronized (colaReserva) {
                procesarMensaje(colaReserva, "Reserva");
            }
            synchronized (colaCancelacion) {
                procesarMensaje(colaCancelacion, "Cancelacion");
            }
            synchronized (colaPago) {
                procesarMensaje(colaPago, "Pago");
            }
            synchronized (colaConsulta) {
                procesarMensaje(colaConsulta, "Consulta");
            }
            synchronized (colaPagoCancelacion) {
                procesarMensaje(colaPagoCancelacion, "PagoCancelacion");
            }

        }

        executor.shutdown();
        consumer.close();
        consumerCancelacion.close();
        consumerPago.close();
        consumerPagoCancelacion.close();
        consumerConsulta.close();

    }
    private void procesarMensaje(Deque<String> cola, String tipoPeticion) throws JMSException, InterruptedException {
        //lock.lock();
        int i = 0;
        boolean procesado = false;
        String tipoCliente = "";
        String peticionAProcesar="";

        Iterator<String> iterator = cola.iterator();
        if (!cola.isEmpty()) {
            System.out.println("Mensajes en la cola: " + cola.size());
            while (i < PRIORIDAD && iterator.hasNext()) { // Procesamos los 5 primeros mensajes en búsqueda de petición de Agencia
                String mensajeActual = iterator.next(); // iterar sobre la cola
                tipoCliente = ComprobarReserva(mensajeActual);// Obtengo el tipo de cliente
                if (Objects.equals(tipoCliente, "Agencia")) {
                    peticionAProcesar = mensajeActual;
                    procesado = true;
                    i = 5;
                    iterator.remove(); // elimino la petición de la cola
                }
                //System.out.println("Mensaje recibido: " + mensajeActual);
                i++;
            }

            if (!procesado) { // Si no se ha encontrado petición de Agencia, se procesa el primer mensaje
                peticionAProcesar = cola.peek();
                cola.pop();
                procesado = false;
                i = 0;
            }

            if (peticionAProcesar != "")
                procesarPeticion(peticionAProcesar,tipoPeticion);
            else
                System.out.println("No hay mensajes en la cola");

        }
        TimeUnit.SECONDS.sleep(2);
        //lock.unlock();
    }



    private void procesarPeticion(String peticionAProcesar, String tipoPeticion) throws JMSException {
        switch (tipoPeticion) {
            case "Reserva":
                reservarViaje(peticionAProcesar);
                break;
            case "Cancelacion":
                System.out.println("Cancelación de reserva: ");
                break;
            case "Consulta":
                System.out.println("Consulta de disponibilidad: ");
                consultarDisponibilidad(peticionAProcesar);
                break;
            case "Pago":
                System.out.println("Pago básico: ");
                break;
            default:
                System.out.println("Petición no reconocida");
                break;
        }
    }

    private String ComprobarReserva(String cadena) {
        String[] partes = cadena.split("_");

        // Imprimir las partes izquierda y derecha
        return partes[0];

    }

    private void reservarViaje(String cadena) {
        String[] partes = cadena.split("_");

        String tipoCliente = partes[0];
        String tipoPeticion = partes[1];
        String idCliente = partes[2];
        String viaje = partes[3];
        String codigoReserva = UUID.randomUUID().toString();

        TareaReservaViaje tarea = new TareaReservaViaje(codigoReserva, tipoCliente, idCliente, reservasEEDD, viaje, lock);
        executor.submit(tarea); // creo una tarea para reservar el viaje

    }

    private void consultarDisponibilidad(String cadena) {
        String[] partes = cadena.split("_");

        String tipoCliente = partes[0];
        String tipoPeticion = partes[1];
        String idCliente = partes[2];
        int viaje = Integer.parseInt(partes[3]);
        executor.submit(() -> {
            lock.lock(); // bloquear
            try {
                Constantes.Viajes c = Constantes.Viajes.values()[viaje];
                int capacidadConsumida = reservasEEDD.get(Constantes.Viajes.values()[viaje]).size();
                int capacidad = Constantes.Viajes.values()[viaje].getCapacidad();
                if (capacidadConsumida < capacidad) { // Si hay plazas disponibles
                    TextMessage message = session.createTextMessage("true");
                    message.setStringProperty("tipo", "respuestaDisponibilidad");
                    producerConsultaDisponibilidadEstancia.send(message);
                    return true;
                } else {
                    TextMessage message = session.createTextMessage("false");
                    message.setStringProperty("tipo", "respuestaDisponibilidad");
                    producerConsultaDisponibilidadEstancia.send(message);
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                lock.unlock(); // desbloquear
            }
        });

    }

    /*private void cancelarReserva(String peticionAProcesar) {
        // lógica para cancelar reserva
        String[] partes = peticionAProcesar.split("_");

        String tipoCliente = partes[0];
        String tipoPeticion = partes[1];
        String idCliente = partes[2];
        String viaje = partes[3];
        String codigoReserva = partes[4 ];
        System.out.println("1111111111111111111111111111111111");
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

    }*/

    private String ProcesarOrigenMenasaje(TextMessage msg,String Reserva) {
        return "Agencia";
    }
}