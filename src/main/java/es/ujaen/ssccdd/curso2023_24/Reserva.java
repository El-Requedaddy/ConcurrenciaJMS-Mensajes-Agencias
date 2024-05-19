package es.ujaen.ssccdd.curso2023_24;

public class Reserva {
    private String codigoReserva;
    private String tipoCliente;
    private String idCliente;
    private boolean conCancelacion;

    public Reserva(String codigoReserva, String tipoCliente, String idCliente) {
        this.codigoReserva = codigoReserva;
        this.tipoCliente = tipoCliente;
        this.idCliente = idCliente;
        this.conCancelacion = false;
    }

    public String getCodigoReserva() {
        return codigoReserva;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public boolean isConCancelacion() {
        return conCancelacion;
    }

    public void setConCancelacion(boolean conCancelacion) {
        this.conCancelacion = conCancelacion;
    }
}
