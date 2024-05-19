package es.ujaen.ssccdd.curso2023_24;

// Interface Constantes
public interface Constantes {
    public static final String DESTINO = "ssccdd.curso2024.3.";
    public static final String DIRECCION = "tcp://localhost:61616";
    public static final String TERMINAR = "TERMINAR";

    // Definición colas
    public static final String DESTINO_RESERVA_VIAJE = "ssccdd.curso2024.3.reservaViaje";
    public static final String DESTINO_RESERVA_ESTANCIA = "ssccdd.curso2024.3.reservaEstancia";
    public static final String DESTINO_PAGO_BASICO = "ssccdd.curso2024.3.pagoBasico";
    public static final String DESTINO_PAGO_CANCELACION = "ssccdd.curso2024.3.pagoCancelacion";
    public static final String DESTINO_CONSULTA_DISPONIBILIDAD = "ssccdd.curso2024.3.consultaDisponibilidad";
    public static final String DESTINO_CANCELACION_RESERVA = "ssccdd.curso2024.3.cancelacionReserva";

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

}

