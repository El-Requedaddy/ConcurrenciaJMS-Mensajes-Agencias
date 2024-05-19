package es.ujaen.ssccdd.curso2023_24;
import java.util.ArrayList;
import java.util.List;

public class Resultado {
    private List<String> logs = new ArrayList<>();

    public void addLog(String log) {
        logs.add(log);
    }

    public void imprimirResultados() {
        System.out.println("---- Resultados de la Ejecución ----");
        for (String log : logs) {
            System.out.println(log);
        }
        System.out.println("---- Fin de los Resultados ----");
    }
}
