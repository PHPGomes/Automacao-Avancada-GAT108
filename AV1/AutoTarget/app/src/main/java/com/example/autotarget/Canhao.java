package com.example.autotarget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;

public class Canhao extends Thread {

    private int x, y, size, numBalas;
    private Paint paint;
    private Path path;
    private boolean running = true;
    private GameView gameView;
    private long ultimoTiro;
    private int delayTiros;
    private Jogo jogo;

    public Canhao(int x, int y, GameView gameView, Jogo jogo) {
        this.x = x;
        this.y = y;
        this.size = 60;
        this.gameView = gameView;
        this.jogo = jogo;
        numBalas = 25;
        ultimoTiro = 0;
        delayTiros = 1500;

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);

        path = new Path();
    }

    public void draw(Canvas canvas) {
        path.reset();
        // topo
        path.moveTo(x, y);
        // base esquerda
        path.lineTo(x - size, y + size);
        // base direita
        path.lineTo(x + size, y + size);
        path.close();
        canvas.drawPath(path, paint);
    }

    public void atirar() {
        if (numBalas <= 0) return;

        if (System.currentTimeMillis() - ultimoTiro > delayTiros) {
            Alvo alvo = escolherAlvo();
            if (alvo == null) return;

            int aX = alvo.getX();
            int aY = alvo.getY();

            // Passa 'this' (o próprio canhão) para que a bala saiba quem atirou
            jogo.criarBala(x, y + size, aX, aY, this);

            numBalas--;
            ultimoTiro = System.currentTimeMillis();
        }
    }

    public boolean getRunning() {
        return running;
    }

    public void parar() {
        running = false;
    }

    private Alvo escolherAlvo() {
        List<Alvo> alvos = jogo.getAlvos();
        if (alvos.isEmpty()) return null;

        Alvo alvoEscolhido = null;
        double menorDistancia = Double.MAX_VALUE;

        for (Alvo alvo : alvos) {
            if (!alvo.getRunning()) continue; // Só atira em alvos ativos

            double dx = alvo.getX() - x;
            double dy = alvo.getY() - y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist < menorDistancia) {
                menorDistancia = dist;
                alvoEscolhido = alvo;
            }
        }
        return alvoEscolhido;
    }

    public int getX() {
        return x;
    }

    @Override
    public void run() {
        while (running) {
            atirar();
            if (numBalas <= 0) {
                running = false;
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