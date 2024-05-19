package es.ujaen.ssccdd.curso2023_24;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TareaReservaEstancia implements Runnable {
    private final String codigoReserva;
    private final String tipoCliente;
    private final String idCliente;
    private final HashMap<Constantes.Estancias, List<Reserva>> reservas;
    private final int destino;
    private final Lock lock;

    public TareaReservaEstancia(String codigoReserva, String tipoCliente,String idCliente, HashMap<Constantes.Estancias, List<Reserva>> reservas,String destino, Lock lock) {
        this.codigoReserva = codigoReserva;
        this.tipoCliente = tipoCliente;
        this.reservas = reservas;
        this.destino = Integer.parseInt(destino);
        this.idCliente = idCliente;
        this.lock = lock;
    }

    @Override
    public void run() {
        System.out.println("Iniciado hilo para reserva de estancia: " + codigoReserva + " por el cliente " + tipoCliente + "___" + idCliente);
        Reserva reservaNueva = new Reserva(codigoReserva, tipoCliente, idCliente);
        if(reservarViaje(destino, reservaNueva))
            System.out.println("Reserva realizada: " + codigoReserva + " por el cliente " + tipoCliente + "___" + idCliente);
        else
            System.out.println("Reserva no realizada: " + codigoReserva + " por el cliente " + tipoCliente + "___" + idCliente);
        try {
            TimeUnit.MICROSECONDS.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean reservarViaje(int estancia, Reserva reserva) {
        lock.lock(); // bloquear
        try {
            Constantes.Estancias c = Constantes.Estancias.values()[estancia];
            int capacidadConsumida = reservas.get(Constantes.Estancias.values()[estancia]).size();
            int capacidad = Constantes.Estancias.values()[estancia].getCapacidad();
            if (capacidadConsumida < capacidad) { // Si hay plazas disponibles
                reservas.get(Constantes.Estancias.values()[estancia]).add(reserva);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            lock.unlock(); // desbloquear
        }
    }

}