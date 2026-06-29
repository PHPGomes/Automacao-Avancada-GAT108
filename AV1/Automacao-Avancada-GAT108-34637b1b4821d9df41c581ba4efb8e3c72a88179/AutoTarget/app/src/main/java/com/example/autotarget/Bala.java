package com.example.autotarget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class Bala extends Thread {

    private Paint paint;
    private Path path;
    private boolean running = true;
    private GameView gameView;
    private Canhao canhaoAtirador; // Referência ao canhão que disparou a bala

    private int x, y, size, xAlvo, yAlvo, vel;
    private long lastMove;
    private boolean ativa;
    private double dx, dy;

    public Bala(int xCanhao, int yCanhao, int xAlvo, int yAlvo, GameView gameView, Canhao canhaoAtirador) {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);

        path = new Path();

        x = xCanhao;
        y = yCanhao;

        int deltaX = xAlvo - x;
        int deltaY = yAlvo - y;

        double distancia = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // Prevenção de divisão por zero (CRASH CRÍTICO)
        if (distancia == 0) {
            dx = 0;
            dy = 0;
        } else {
            // normaliza (vetor unitário)
            dx = deltaX / distancia;
            dy = deltaY / distancia;
        }

        this.gameView = gameView;
        this.canhaoAtirador = canhaoAtirador; // Armazena a referência do canhão atirador
        size = 12;
        lastMove = 0;
        vel = 10;
        ativa = false;
    }

    public void draw(Canvas canvas) {
        path.reset();

        // topo esquerda
        path.moveTo(x - size, y);

        // base esquerda
        path.lineTo(x - size, y + size);

        // base direita
        path.lineTo(x + size, y + size);

        // topo direita
        path.lineTo(x + size, y);

        path.close();

        canvas.drawPath(path, paint);
    }

    private synchronized void mover() {
        if (System.currentTimeMillis() - lastMove >= 16) {
            lastMove = System.currentTimeMillis();
            x += dx * vel;
            y += dy * vel;
        }
        // sair da tela → remove bala
        if (gameView != null && (x < 0 || x > gameView.getWidth() || y < 0 || y > gameView.getHeight())) {
            parar();
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setAtiva() {
        ativa = true;
    }

    public void setAlvo(int x, int y) {
        xAlvo = x;
        yAlvo = y;
    }

    public boolean getRunning() {
        return running;
    }

    public boolean getAtividade() {
        return ativa;
    }

    public Canhao getCanhaoAtirador() {
        return canhaoAtirador;
    }

    public void parar() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            mover();
            // evita null
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