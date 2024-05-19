package es.ujaen.ssccdd.curso2023_24;

import java.util.HashMap;
import java.util.List;

public class TareaReservaViaje implements Runnable {
    private final String codigoReserva;
    private final String tipoCliente;
    private final String idCliente;
    private final HashMap<Constantes.Viajes, List<Reserva>> reservas;
    private final Constantes.Viajes destino;

    public TareaReservaViaje(String codigoReserva, String tipoCliente,String idCliente, HashMap<Constantes.Viajes, List<Reserva>> reservas,Constantes.Viajes destino) {
        this.codigoReserva = codigoReserva;
        this.tipoCliente = tipoCliente;
        this.reservas = reservas;
        this.destino = destino;
        this.idCliente = idCliente;
    }

    @Override
    public void run() {
        System.out.println("Iniciado hilo para reserva de viaje: " + codigoReserva + " por el cliente " + tipoCliente);
        Reserva reservaNueva = new Reserva(codigoReserva, tipoCliente, idCliente);
        reservarViaje(destino, reservaNueva);
        System.out.println("Reserva realizada: " + codigoReserva + " por el cliente " + tipoCliente);
    }

    public boolean reservarViaje(Constantes.Viajes viaje, Reserva reserva) {
        synchronized (reservas) { // exclusión mutua sobre la estructura de datos
            if (reservas.get(viaje).size() < viaje.getCapacidad()) { // Si hay plazas disponibles
                reservas.get(viaje).add(reserva);
                return true;
            } else {
                return false;
            }
        }
    }
}