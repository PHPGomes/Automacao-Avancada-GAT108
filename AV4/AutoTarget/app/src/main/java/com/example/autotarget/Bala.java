package com.example.autotarget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class Bala extends Thread {

    private static final int TAMANHO = 12;
    private static final int VELOCIDADE = 10;
    private static final int INTERVALO_MOVIMENTO_MS = 16;

    private final Paint paint;
    private final Path path;

    private volatile boolean running = true;

    private final GameView gameView;
    private final Canhao canhaoAtirador;
    private final Lado lado;

    private int x;
    private int y;

    private final double dx;
    private final double dy;

    public Bala(
            int xCanhao,
            int yCanhao,
            int xAlvo,
            int yAlvo,
            GameView gameView,
            Canhao canhaoAtirador
    ) {

        this.x = xCanhao;
        this.y = yCanhao;
        this.gameView = gameView;
        this.canhaoAtirador = canhaoAtirador;
        this.lado = canhaoAtirador.getLado();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);

        path = new Path();

        int deltaX = xAlvo - x;
        int deltaY = yAlvo - y;

        double distancia = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distancia == 0) {
            dx = 0;
            dy = 0;
        } else {
            dx = deltaX / distancia;
            dy = deltaY / distancia;
        }
    }

    public void draw(Canvas canvas) {

        path.reset();

        path.moveTo(x - TAMANHO, y);
        path.lineTo(x - TAMANHO, y + TAMANHO);
        path.lineTo(x + TAMANHO, y + TAMANHO);
        path.lineTo(x + TAMANHO, y);
        path.close();

        canvas.drawPath(path, paint);
    }

    private void mover() {

        x += dx * VELOCIDADE;
        y += dy * VELOCIDADE;

        verificarLimites();
    }

    private void verificarLimites() {

        if (gameView == null || gameView.getWidth() == 0 || gameView.getHeight() == 0) {
            parar();
            return;
        }

        int metade = gameView.getWidth() / 2;

        if (lado == Lado.ESQUERDO && x > metade) {
            parar();
            return;
        }

        if (lado == Lado.DIREITO && x < metade) {
            parar();
            return;
        }

        if (x < 0 || x > gameView.getWidth() || y < 0 || y > gameView.getHeight()) {
            parar();
        }
    }

    public void parar() {
        running = false;
        interrupt();
    }

    public boolean getRunning() {
        return running;
    }

    public Canhao getCanhaoAtirador() {
        return canhaoAtirador;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public void run() {

        setPriority(Thread.MIN_PRIORITY);

        while (running) {

            mover();

            try {

                Thread.sleep(INTERVALO_MOVIMENTO_MS);

            } catch (InterruptedException e) {

                running = false;
                Thread.currentThread().interrupt();
            }
        }
    }
}