package com.example.autotarget;

import android.graphics.Color;

public class AlvoComum extends Alvo {

    public AlvoComum(int x, int y, int tamX, int tamY, GameView gameView, Jogo jogo) {
        super(x, y, tamX, tamY, gameView, jogo, 4);
        paint.setColor(Color.GREEN);
    }

    @Override
    public void run() {
        if (super.getTamX() > 0 && super.getTamY() > 0) {
            super.Atualizadestino();
        }

        while (super.getRunning()) {
            if (super.getTamX() > 0 && super.getTamY() > 0) {
                super.mover();
            }

            if (super.getGameView() != null) {
                super.getGameView().postInvalidate();
            }

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
