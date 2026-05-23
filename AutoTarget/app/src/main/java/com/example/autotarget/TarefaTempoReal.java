package com.example.autotarget;

public class TarefaTempoReal {

    private String nome;
    private int prioridade;
    private int periodo;
    private int deadline;
    private int jitter;
    private int tempoExecucao;

    public TarefaTempoReal(
            String nome,
            int prioridade,
            int periodo,
            int deadline,
            int jitter,
            int tempoExecucao
    ) {

        this.nome = nome;
        this.prioridade = prioridade;
        this.periodo = periodo;
        this.deadline = deadline;
        this.jitter = jitter;
        this.tempoExecucao = tempoExecucao;
    }

    public String getNome() {
        return nome;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public int getPeriodo() {
        return periodo;
    }

    public int getDeadline() {
        return deadline;
    }

    public int getJitter() {
        return jitter;
    }

    public int getTempoExecucao() {
        return tempoExecucao;
    }
}