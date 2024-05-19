package es.ujaen.ssccdd.curso2023_24;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static es.ujaen.ssccdd.curso2023_24.Constantes.*;

public class Main {

    public static void main(String[] args) {

        Resultado resultado = new Resultado();

        try {
            CountDownLatch latch = new CountDownLatch(1);

            Gestor gestor = new Gestor(resultado, latch);
            Thread gestorThread = new Thread(gestor);
            gestorThread.start();

            Agencia agencia = new Agencia();
            Thread agenciaThread = new Thread(agencia);
            agenciaThread.start();

            List<Thread> usuarioThreads = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                Usuario usuario = new Usuario(i);
                Thread usuarioThread = new Thread(usuario);
                usuarioThread.start();
                usuarioThreads.add(usuarioThread);
            }

            Thread.sleep(TIEMPO_ESPERA_FINALIZACION);
            latch.countDown();
            gestorThread.join();

            agenciaThread.interrupt();
            for (Thread usuarioThread : usuarioThreads) {
                usuarioThread.interrupt();
            }

            resultado.imprimirReservasViaje();
            resultado.imprimirReservasEstancia();

        } catch (JMSException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
