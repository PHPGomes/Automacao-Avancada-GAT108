package com.example.autotarget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


public class AlvoRapido extends Alvo{



    public AlvoRapido(int x, int y, int tamX, int tamY, GameView gameView,Jogo jogo){
        super(x, y, tamX, tamY, gameView,jogo,8);
        paint.setColor(Color.RED);
    }

    @Override
    public void run() {

        // evita rodar antes da tela existir
        if (super.getTamX() > 0 && super.getTamY() > 0) {
            super.Atualizadestino();
        }

        while (super.getRuning()) { // corrigido

            if (super.getTamX() > 0 && super.getTamY() > 0) {
                super.mover();
            }

            // evita null
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
