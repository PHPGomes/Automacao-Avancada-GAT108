package com.example.autotarget;

import android.graphics.Color;

public class AlvoRapido extends Alvo {

    public AlvoRapido(int x, int y, int tamX, int tamY, GameView gameView, Jogo jogo) {
        super(x, y, tamX, tamY, gameView, jogo, 8);
        paint.setColor(Color.RED);
    }

    @Override
    public void run() {
        if (super.getTamX() > 0 && super.getTamY() > 0) {
            super.atualizarDestino();
        }

        while (super.getRunning()) {
            if (super.getTamX() > 0 && super.getTamY() > 0) {
                super.mover();
            }

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                super.setRunning(false);
                return;
            }
        }
    }
}
