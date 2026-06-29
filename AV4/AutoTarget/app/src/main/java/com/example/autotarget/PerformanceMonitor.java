package com.example.autotarget;

import android.os.Debug;
import android.util.Log;

public class PerformanceMonitor {

    private long inicioExecucao;
    private long ultimoLog;

    private long somaFrame = 0;
    private long quantidadeFrames = 0;

    public void iniciar() {
        inicioExecucao = System.currentTimeMillis();
        ultimoLog = inicioExecucao;
    }

    public void registrarFrame(long tempoFrame) {

        somaFrame += tempoFrame;
        quantidadeFrames++;

        long agora = System.currentTimeMillis();

        if (agora - ultimoLog >= 5000) {

            double media = somaFrame / (double) quantidadeFrames;

            Log.d("PERFORMANCE",
                    "Tempo médio frame = "
                            + media
                            + " ms");

            Log.d("PERFORMANCE",
                    "Threads = "
                            + Thread.activeCount());

            Log.d("PERFORMANCE",
                    "Heap = "
                            + (Debug.getNativeHeapAllocatedSize()/1024)
                            + " KB");

            ultimoLog = agora;
        }
    }

    public void finalizar() {

        long tempo = System.currentTimeMillis() - inicioExecucao;

        Log.d("PERFORMANCE",
                "Tempo total = "
                        + tempo
                        + " ms");

        Log.d("PERFORMANCE",
                "Frames = "
                        + quantidadeFrames);

        Log.d("PERFORMANCE",
                "Tempo médio = "
                        + (somaFrame/(double)quantidadeFrames)
                        + " ms");
    }

}