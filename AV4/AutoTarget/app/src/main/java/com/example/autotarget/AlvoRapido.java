package com.example.autotarget;

import android.graphics.Color;

public class AlvoRapido extends Alvo {

    public AlvoRapido(
            int x,
            int y,
            int tamX,
            int tamY,
            GameView gameView,
            Jogo jogo
    ) {
        super(x, y, tamX, tamY, gameView, jogo, 8);
        paint.setColor(Color.RED);
    }
}