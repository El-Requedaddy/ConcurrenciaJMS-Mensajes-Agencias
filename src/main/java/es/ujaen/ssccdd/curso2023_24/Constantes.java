package es.ujaen.ssccdd.curso2023_24;

import java.util.Random;

// Interface Constantes
public interface Constantes {

    // Tiempos de espera
    public static final int TIEMPO_ESPERA_AGENCIA = 10000;
    public static final int TIEMPO_ESPERA_USUARIO = 5000;
    public static final int TIEMPO_ESPERA_SOLICITUD = 5000;
    public static final int TIEMPO_ESPERA_FINALIZACION = 10000;

    public static final String DESTINO = "ssccdd.curso2024.3.";
    public static final String DIRECCION = "tcp://localhost:61616";
    public static final String TERMINAR = "TERMINAR";

    // Definición colas
    public static final String DESTINO_RESERVA_VIAJE = "ssccdd.curso2024.3.reservaViaje";
    public static final String DESTINO_RESERVA_ESTANCIA = "ssccdd.curso2024.3.reservaEstancia";
    public static final String DESTINO_PAGO_BASICO = "ssccdd.curso2024.3.pagoBasico";
    public static final String DESTINO_PAGO_CANCELACION = "ssccdd.curso2024.3.pagoCancelacion";
    public static final String DESTINO_CONSULTA_DISPONIBILIDAD_VIAJE = "ssccdd.curso2024.3.consultaDisponibilidadViaje";
    public static final String DESTINO_CONSULTA_DISPONIBILIDAD_ESTANCIA = "ssccdd.curso2024.3.consultaDisponibilidadEstancia";
    public static final String DESTINO_RESPUESTA_CONSULTA_DISPONIBILIDAD_VIAJE = "ssccdd.curso2024.3.respuestaConsultaDisponibilidadViaje";
    public static final String DESTINO_RESPUESTA_CONSULTA_DISPONIBILIDAD_ESTANCIA = "ssccdd.curso2024.3.respuestaConsultaDisponibilidadEstancia";
    public static final String DESTINO_CANCELACION_RESERVA_VIAJE = "ssccdd.curso2024.3.cancelacionReservaViaje";
    public static final String DESTINO_CANCELACION_RESERVA_ESTANCIA = "ssccdd.curso2024.3.cancelacionReservaEstancia";
    public static final int PRIORIDAD = 5;

    public enum Viajes {
        VIAJE1("VIAJE A JAÉN", 1),
        VIAJE2("VIAJE A JAPÓN", 1),
        VIAJE3("VIAJE A LATAM", 1),
        VIAJE4("VIAJE A YUGOSLAVIA", 1),
        VIAJE5("VIAJE A GUATEMALA", 1);

        private final String valor;
        private final int capacidad;

        private Viajes(String valor, int capacidad) {
            this.capacidad = capacidad;
            this.valor = valor;
        }

        public String getValor() {
            return valor;
        }

        public int getCapacidad() {
            return capacidad;
        }
    }

    public static int generarViajeAleatorio() {
        Viajes[] viajes = Viajes.values();
        Random random = new Random();
        int indiceAleatorio = random.nextInt(viajes.length);
        return viajes[indiceAleatorio].ordinal();
    }

    public enum Estancias {
        ESTANCIA1("ESTANCIA EN JAÉN", 5),
        ESTANCIA2("ESTANCIA EN JAPÓN", 5),
        ESTANCIA3("ESTANCIA EN LATAM", 5),
        ESTANCIA4("ESTANCIA EN YUGOSLAVIA", 5),
        ESTANCIA5("ESTANCIA EN GUATEMALA", 5);

        private final String nombre;
        private final int capacidad;

        private Estancias(String nombre, int capacidad) {
            this.nombre = nombre;
            this.capacidad = capacidad;
        }

        public String getNombre() {
            return nombre;
        }

        public int getCapacidad() {
            return capacidad;
        }
    }

    public static Estancias generarEstanciaAleatoria() {
        Estancias[] estancias = Estancias.values();
        Random random = new Random();
        int indiceAleatorio = random.nextInt(estancias.length);
        return estancias[indiceAleatorio];
    }

    public enum Servicio {
        RESERVA_VIAJE(".reservaViaje."),
        RESERVA_ESTANCIA(".reservaEstancia."),
        PAGO_BASICO(".pagoBasico."),
        PAGO_CANCELACION(".pagoCancelacion."),
        CONSULTA_DISPONIBILIDAD(".consultaDisponibilidad."),
        CANCELACION_RESERVA(".cancelacionReserva.");

        private final String valor;

        private Servicio(String valor) {
            this.valor = valor;
        }

        public String getValor() {
            return valor;
        }
    }

    public static final Servicio[] SERVICIOS_AGENCIA = Servicio.values();

    public enum tipoCancelacion {
        CANCELACION_VIAJE("Cancelación de viaje"),
        CANCELACION_ESTANCAI("Cancelación de estancia");

        private final String valor;

        private tipoCancelacion(String valor) {
            this.valor = valor;
        }

        public String getValor() {
            return valor;
        }
    }

    public static int getTipoCancelacionAleatorio() {
        tipoCancelacion[] cancelaciones = tipoCancelacion.values();
        Random random = new Random();
        int indiceAleatorio = random.nextInt(cancelaciones.length);
        return cancelaciones[indiceAleatorio].ordinal();
    }

}

