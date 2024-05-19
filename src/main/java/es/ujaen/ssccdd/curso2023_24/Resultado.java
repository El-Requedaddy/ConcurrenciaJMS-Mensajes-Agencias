package es.ujaen.ssccdd.curso2023_24;
import java.util.ArrayList;
import javax.jms.*;
import java.util.*;

public class Resultado {
    private List<String> logs = new ArrayList<>();
    private HashMap<Constantes.Viajes, List<Reserva>> reservasViaje;
    private HashMap<Constantes.Estancias, List<Reserva>> reservasEstancia;

    Resultado() {
        reservasViaje = new HashMap<>();
        reservasEstancia = new HashMap<>();
    }

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

        for (Constantes.Viajes viaje : Constantes.Viajes.values()) {
            List<Reserva> reservaListViaje = reservasViaje.get(viaje);
            if (reservaListViaje != null) { // Si hay reservas para este viaje
                for (Iterator<Reserva> iterator = reservaListViaje.iterator(); iterator.hasNext();) {
                    Reserva reserva = iterator.next();
                    System.out.println(reserva.toString());
                }
            }
        }
//        for (Map.Entry<Constantes.Viajes, List<Reserva>> entry : reservasViaje.entrySet()) {
//            System.out.println("Viaje: " + entry.getKey());
//            for (Reserva reserva : entry.getValue()) {
//                System.out.println(reserva.toString());
//            }
//        }
        System.out.println("---- Fin de las Reservas VIAJES ----");
    }

    public void imprimirReservasEstancia() {
        System.out.println("---- Reservas ESTANCIAS ----");

        for (Constantes.Estancias estancia : Constantes.Estancias.values()) {
            List<Reserva> reservaListEstancia = reservasEstancia.get(estancia);
            if (reservaListEstancia != null) { // Si hay reservas para esta estancia
                for (Iterator<Reserva> iterator = reservaListEstancia.iterator(); iterator.hasNext();) {
                    Reserva reserva = iterator.next();
                    System.out.println(reserva.toString());
                }
            }
        }

//        for (Map.Entry<Constantes.Estancias, List<Reserva>> entry : reservasEstancia.entrySet()) {
//            System.out.println("Estancia: " + entry.getKey());
//            for (Reserva reserva : entry.getValue()) {
//                System.out.println(reserva.toString());
//            }
//        }
        System.out.println("---- Fin de las Reservas ESTANCIAS ----");
    }

}