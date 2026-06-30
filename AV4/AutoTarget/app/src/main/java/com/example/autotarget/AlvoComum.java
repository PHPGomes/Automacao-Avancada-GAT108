package com.example.autotarget;

import android.graphics.Color;

public class AlvoComum extends Alvo {

    public AlvoComum(
            int x,
            int y,
            int tamX,
            int tamY,
            GameView gameView,
            Jogo jogo
    ) {
        super(x, y, tamX, tamY, gameView, jogo, 4);
        paint.setColor(Color.GREEN);
    }
}