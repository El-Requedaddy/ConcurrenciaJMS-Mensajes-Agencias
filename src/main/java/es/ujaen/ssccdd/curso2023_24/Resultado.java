package es.ujaen.ssccdd.curso2023_24;
import java.util.ArrayList;
import javax.jms.*;
import java.util.*;

public class Resultado {
    private List<String> logs = new ArrayList<>();
    private HashMap<Constantes.Viajes, List<Reserva>> reservasViaje;
    private HashMap<Constantes.Estancias, List<Reserva>> reservasEstancia;

    public void addLog(String log) {
        logs.add(log);
    }

    public void addReservaViaje(HashMap<Constantes.Viajes, List<Reserva>> reservasViaje) {
        this.reservasViaje.putAll(reservasViaje);
    }

    public void addReservaEstancia(HashMap<Constantes.Estancias, List<Reserva>> reservasEstancia) {
        this.reservasEstancia.putAll(reservasEstancia);
    }

    public void imprimirReservasViaje() {
        System.out.println("---- Reservas VIAJES ----");
        for (Map.Entry<Constantes.Viajes, List<Reserva>> entry : reservasViaje.entrySet()) {
            System.out.println("Viaje: " + entry.getKey());
            for (Reserva reserva : entry.getValue()) {
                System.out.println(reserva.toString());
            }
        }
        System.out.println("---- Fin de las Reservas VIAJES ----");
    }

    public void imprimirReservasEstancia() {
        System.out.println("---- Reservas ESTANCIAS ----");
        for (Map.Entry<Constantes.Estancias, List<Reserva>> entry : reservasEstancia.entrySet()) {
            System.out.println("Estancia: " + entry.getKey());
            for (Reserva reserva : entry.getValue()) {
                System.out.println(reserva.toString());
            }
        }
        System.out.println("---- Fin de las Reservas ESTANCIAS ----");
    }

}