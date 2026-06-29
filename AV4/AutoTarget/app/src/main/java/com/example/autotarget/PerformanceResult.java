package com.example.autotarget;

public class PerformanceResult {

    public int quantidadeAlvos;
    public int quantidadeThreads;
    public long tempoTotalMs;
    public double tempoMedioFrameMs;
    public double fpsMedio;
    public double speedup;
    public double fracaoParalelizavel;

    public PerformanceResult(
            int quantidadeAlvos,
            int quantidadeThreads,
            long tempoTotalMs,
            double tempoMedioFrameMs
    ) {
        this.quantidadeAlvos = quantidadeAlvos;
        this.quantidadeThreads = quantidadeThreads;
        this.tempoTotalMs = tempoTotalMs;
        this.tempoMedioFrameMs = tempoMedioFrameMs;

        if (tempoMedioFrameMs > 0) {
            this.fpsMedio = 1000.0 / tempoMedioFrameMs;
        }
    }
}