package com.example.autotarget;

import java.util.List;

public class EscalonadorRM {

    public static double calcularUtilizacao(
            List<TarefaTempoReal> tarefas
    ) {

        double U = 0;

        for (TarefaTempoReal t : tarefas) {

            U += (double)
                    t.getTempoExecucao()
                    / t.getPeriodo();
        }

        return U;
    }

    public static boolean verificarEscalonabilidade(
            List<TarefaTempoReal> tarefas
    ) {

        int n = tarefas.size();

        double limite =
                n * (Math.pow(2, 1.0 / n) - 1);

        double utilizacao =
                calcularUtilizacao(tarefas);

        System.out.println(
                "Utilização RM: " + utilizacao
        );

        System.out.println(
                "Limite RM: " + limite
        );

        return utilizacao <= limite;
    }
}