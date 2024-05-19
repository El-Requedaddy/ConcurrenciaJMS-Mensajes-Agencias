package es.ujaen.ssccdd.curso2023_24;

import javax.jms.JMSException;
import static es.ujaen.ssccdd.curso2023_24.Constantes.*;

public class Main {

    public static void main(String[] args) {
        try {
            // Crear e iniciar el proceso Gestor
            Gestor gestor = new Gestor();
            Thread gestorThread = new Thread(gestor);
            gestorThread.start();

            // Crear e iniciar el proceso Agencia
            Agencia agencia = new Agencia();
            Thread agenciaThread = new Thread(agencia);
            agenciaThread.start();

            /*
            // Crear e iniciar varios procesos de Usuario
            for (int i = 1; i <= 5; i++) {
                Usuario usuario = new Usuario(i);
                Thread usuarioThread = new Thread(usuario);
                usuarioThread.start();
            }*/
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}