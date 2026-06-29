package com.example.autotarget;

import android.os.Debug;
import android.util.Log;

public class PerformanceMonitor {

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

        if (agora - ultimoLog >= 5000) {

            imprimirEstatisticas();

            ultimoLog = agora;
        }
    }

    private void imprimirEstatisticas() {

        double tempoMedio = somaFrame / (double) quantidadeFrames;

        double fps;

        if (tempoMedio > 0) {
            fps = 1000.0 / tempoMedio;
        } else {
            fps = 0;
        }

        long heap =
                Debug.getNativeHeapAllocatedSize() / 1024;

        Log.d(
                "AMDAHL",
                "========================================"
        );

        Log.d(
                "AMDAHL",
                "Alvos: " + quantidadeAlvos
        );

        Log.d(
                "AMDAHL",
                "Threads: " + Thread.activeCount()
        );

        Log.d(
                "AMDAHL",
                String.format(
                        "Tempo médio/frame: %.3f ms",
                        tempoMedio
                )
        );

        Log.d(
                "AMDAHL",
                String.format(
                        "FPS médio: %.2f",
                        fps
                )
        );

        Log.d(
                "AMDAHL",
                "Heap: " + heap + " KB"
        );

        Log.d(
                "AMDAHL",
                "========================================"
        );
    }

    public void finalizar() {

        long tempoTotal =
                System.currentTimeMillis() - inicioExecucao;

        double tempoMedio =
                somaFrame / (double) quantidadeFrames;

        double fps;

        if (tempoMedio > 0) {
            fps = 1000.0 / tempoMedio;
        } else {
            fps = 0;
        }

        Log.d(
                "AMDAHL",
                "******** RESULTADO FINAL ********"
        );

        Log.d(
                "AMDAHL",
                "Tempo total: " + tempoTotal + " ms"
        );

        Log.d(
                "AMDAHL",
                "Frames: " + quantidadeFrames
        );

        Log.d(
                "AMDAHL",
                String.format(
                        "Tempo médio/frame: %.3f ms",
                        tempoMedio
                )
        );

        Log.d(
                "AMDAHL",
                String.format(
                        "FPS médio: %.2f",
                        fps
                )
        );

        Log.d(
                "AMDAHL",
                "Threads finais: " + Thread.activeCount()
        );

        Log.d(
                "AMDAHL",
                "********************************"
        );
    }

    public void setQuantidadeAlvos(int quantidadeAlvos) {
        this.quantidadeAlvos = quantidadeAlvos;
    }

    public int getQuantidadeAlvos() {
        return quantidadeAlvos;
    }
}