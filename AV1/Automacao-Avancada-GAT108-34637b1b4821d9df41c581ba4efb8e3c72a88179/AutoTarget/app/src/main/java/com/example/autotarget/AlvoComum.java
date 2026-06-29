package com.example.autotarget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


public class AlvoComum extends Alvo{


    public AlvoComum(int x, int y, int tamX, int tamY, GameView gameView){
        super(x, y, tamX, tamY, gameView,6);
        paint.setColor(Color.GREEN);
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
