package com.example.autotarget;

import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.Random;

public class Alvo extends Thread {

    private int x, y, raio, tamX, tamY;
    protected int vel;
    private long lastMove;
    private GameView gameView;
    private int desX, desY;
    protected Paint paint;
    private boolean running = true;

    Random random = new Random();

    public Alvo(int x, int y, int tamX, int tamY, GameView gameView, int vel) {
        this.x = x;
        this.y = y;
        this.tamX = tamX;
        this.tamY = tamY;
        this.gameView = gameView;
        this.raio = 30;
        this.vel = vel;
        lastMove = 0;

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        // Inicializa o destino imediatamente se as dimensões forem válidas
        if (tamX > 0 && tamY > 0) {
            Atualizadestino();
        }
    }

    public void Atualizadestino() {
        if (tamX > 0 && tamY > 0) {
            desX = random.nextInt(tamX - 200) + 100;
            desY = random.nextInt(tamY - 200) + 100;
        }
    }

    public void mover() {
        if (System.currentTimeMillis() - lastMove >= 16) {
            lastMove = System.currentTimeMillis();

            if (desX > x) x = Math.min(x + vel, desX);
            else if (desX < x) x = Math.max(x - vel, desX);

            if (desY > y) y = Math.min(y + vel, desY);
            else if (desY < y) y = Math.max(y - vel, desY);
        }

        if (x == desX && y == desY) {
            Atualizadestino();
        }

        if (tamX > 0 && tamY > 0) {
            x = Math.max(raio, Math.min(x, tamX - raio));
            y = Math.max(raio, Math.min(y, tamY - raio));
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getTamX() { return tamX; }
    public int getTamY() { return tamY; }

    public boolean getRunning() { return running; }
    public boolean getRuning() { return running; }

    public int getRaio() { return raio; }
    public GameView getGameView() { return gameView; }

    public synchronized void draw(Canvas canvas) {
        canvas.drawCircle(x, y, raio, paint);
    }

    public void parar() {
        running = false;
    }

    @Override
    public void run() {
        // A chamada de Atualizadestino() já foi feita no construtor se as dimensões eram válidas.
        // Se não eram, será feita na primeira vez que tamX/tamY forem > 0 dentro do loop.

        while (running) {
            // Garante que o destino seja atualizado se as dimensões se tornarem válidas após a criação
            if (tamX > 0 && tamY > 0 && desX == 0 && desY == 0) {
                Atualizadestino();
            }

            if (tamX > 0 && tamY > 0) {
                mover();
            }

            if (gameView != null) {
                gameView.postInvalidate();
            }

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}