package es.ujaen.ssccdd.curso2023_24;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class TareaFinalizacion implements Runnable {
    private final List<Thread> listaTareas;
    private final CountDownLatch esperaFinalizacion;
    private final Resultado resultado;

    public TareaFinalizacion(List<Thread> listaTareas, CountDownLatch esperaFinalizacion, Resultado resultado) {
        this.listaTareas = listaTareas;
        this.esperaFinalizacion = esperaFinalizacion;
        this.resultado = resultado;
    }

    @Override
    public void run() {
        try {
            // Esperar durante un tiempo antes de finalizar las tareas
            Thread.sleep(10000); // Espera 10 segundos antes de finalizar

            // Recorre la lista de tareas para solicitar su finalización
            for (Thread tarea : listaTareas) {
                tarea.interrupt();
            }

            System.out.println("Ha finalizado la ejecución la Tarea(FINALIZACION)");
            resultado.addLog("Ha finalizado la ejecución la Tarea(FINALIZACION)");

            // El programa principal puede presentar los resultados de las tareas
            esperaFinalizacion.countDown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("TareaFinalizacion interrumpida.");
        }
    }
}
