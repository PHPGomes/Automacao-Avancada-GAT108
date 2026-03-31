package com.example.autotarget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

public class Alvo extends Thread {

    private int x, y, raio, tamX, tamY;
    private long lastMove;
    private GameView gameView;
    private int desX, desY;
    private Paint paint;
    Random random = new Random();

    public Alvo(int x, int y, int tamX, int tamY, GameView gameView) {
        this.x = x;
        this.y = y;
        this.tamX = tamX;
        this.tamY = tamY;
        this.gameView = gameView;
        this.raio = 50;
        lastMove = 0;

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
    }

    private void Atualizadestino() {
        desX = random.nextInt(tamX);
        desY = random.nextInt(tamY);
    }

    private synchronized void mover() {
        if (System.currentTimeMillis() - lastMove >= 16) {
            lastMove = System.currentTimeMillis();

            if (desX > x) x = Math.min(x + 2, desX);
            else if (desX < x) x = Math.max(x - 2, desX);

            if (desY > y) y = Math.min(y + 2, desY);
            else if (desY < y) y = Math.max(y - 2, desY);
        }

        // chegou no destino → novo destino
        if (x == desX && y == desY) {
            Atualizadestino();
        }

        // 🔥 evita sair da tela
        x = Math.max(raio, Math.min(x, tamX - raio));
        y = Math.max(raio, Math.min(y, tamY - raio));
    }

    public synchronized void draw(Canvas canvas) {
        canvas.drawCircle(x, y, raio, paint);
    }

    @Override
    @Override
    public void run() {
        Atualizadestino();

        while (true) {
            mover();

            // 🔥 avisa a UI pra redesenhar
            gameView.postInvalidate();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}