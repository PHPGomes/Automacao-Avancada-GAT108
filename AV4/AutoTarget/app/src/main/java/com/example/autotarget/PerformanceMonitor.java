package com.example.autotarget;

import android.os.Debug;
import android.util.Log;

public class PerformanceMonitor {

    private static final String TAG = "AMDAHL";
    private static final long INTERVALO_LOG_MS = 5000;

    private long inicioExecucao;
    private long ultimoLog;

    private long somaFrame;
    private long quantidadeFrames;

    private int quantidadeAlvos;

    public PerformanceMonitor(int quantidadeAlvos) {
        this.quantidadeAlvos = quantidadeAlvos;
    }

    public void iniciar() {

        inicioExecucao = System.currentTimeMillis();
        ultimoLog = inicioExecucao;

        somaFrame = 0;
        quantidadeFrames = 0;
    }

    public void registrarFrame(long tempoFrame) {

        somaFrame += tempoFrame;
        quantidadeFrames++;

        long agora = System.currentTimeMillis();

        if (agora - ultimoLog >= INTERVALO_LOG_MS) {

            imprimirEstatisticas();

            ultimoLog = agora;
        }
    }

    private void imprimirEstatisticas() {

        double tempoMedio = calcularTempoMedioFrame();
        double fps = calcularFPS();

        long heap =
                Debug.getNativeHeapAllocatedSize() / 1024;

        Log.d(TAG, "========================================");
        Log.d(TAG, "Alvos: " + quantidadeAlvos);
        Log.d(TAG, "Threads: " + Thread.activeCount());
        Log.d(TAG, String.format("Tempo médio/frame: %.3f ms", tempoMedio));
        Log.d(TAG, String.format("FPS médio: %.2f", fps));
        Log.d(TAG, "Heap: " + heap + " KB");
        Log.d(TAG, "========================================");
    }

    public void finalizar() {

        long tempoTotal =
                System.currentTimeMillis() - inicioExecucao;

        Log.d(TAG, "******** RESULTADO FINAL ********");
        Log.d(TAG, "Tempo total: " + tempoTotal + " ms");
        Log.d(TAG, "Frames: " + quantidadeFrames);
        Log.d(TAG,
                String.format("Tempo médio/frame: %.3f ms",
                        calcularTempoMedioFrame()));
        Log.d(TAG,
                String.format("FPS médio: %.2f",
                        calcularFPS()));
        Log.d(TAG, "Threads finais: " + Thread.activeCount());
        Log.d(TAG, "********************************");
    }

    private double calcularTempoMedioFrame() {

        if (quantidadeFrames == 0) {
            return 0;
        }

        return somaFrame / (double) quantidadeFrames;
    }

    private double calcularFPS() {

        double tempoMedio = calcularTempoMedioFrame();

        if (tempoMedio <= 0) {
            return 0;
        }

        return 1000.0 / tempoMedio;
    }

    public void setQuantidadeAlvos(int quantidadeAlvos) {
        this.quantidadeAlvos = quantidadeAlvos;
    }

    public int getQuantidadeAlvos() {
        return quantidadeAlvos;
    }
}