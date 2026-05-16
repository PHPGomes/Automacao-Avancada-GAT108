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
    private Jogo jogo;
    private Lado lado;
    private int delayTiros;
    private static final int LIMITE_CANHOES = 5;
    private static final int DELAY_BASE = 1500;

    public Canhao(int x, int y, GameView gameView, Jogo jogo,Lado lado) {
        this.x = x;
        this.y = y;
        this.size = 60;
        this.gameView = gameView;
        this.jogo = jogo;
        delayTiros = DELAY_BASE;
        this.lado = lado;
        numBalas = 5;
        ultimoTiro = 0;

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
        if (!temEnergia()) return;
        delayTiros = calcularDelay();
        if (numBalas <= 0) return;
        if (System.currentTimeMillis() - ultimoTiro > delayTiros) {
            Alvo alvo = escolherAlvo();
            if (alvo == null) return;
            int aX = alvo.getX();
            int aY = alvo.getY();
            if (!jogo.consumirEnergia(lado)) {
                return;
            }
            // Passa 'this' (o próprio canhão) para que a bala saiba quem atirou
            jogo.criarBala(x, y + size, aX, aY, this);
            numBalas--;
            ultimoTiro = System.currentTimeMillis();
        }
    }
    private boolean temEnergia() {

        if (lado == Lado.ESQUERDO) {
            return jogo.getEnergiaEsquerda() > 0;
        }

        return jogo.getEnergiaDireita() > 0;
    }

    public boolean getRunning() {
        return running;
    }

    public void parar() {
        running = false;
    }

    private Alvo escolherAlvo() {
        List<Alvo> alvos = jogo.getAlvosPorLado(lado);
        if (alvos.isEmpty()) return null;

        Alvo alvoEscolhido = null;
        double menorDistancia = Double.MAX_VALUE;

        for (Alvo alvo : alvos) {
            if (!alvo.getRunning()) continue;
            if (alvo.getLado() != lado) continue;

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

    private int calcularDelay() {

        int quantidade;
        if (lado == Lado.ESQUERDO) {
            quantidade = jogo.getCanhoesEsquerda().size();

        } else {
            quantidade = jogo.getCanhoesDireita().size();
        }
        if (quantidade <= LIMITE_CANHOES) {
            return DELAY_BASE;
        }
        int excesso = quantidade - LIMITE_CANHOES;
        double fator = 1 + (excesso * 0.2);
        return (int)(DELAY_BASE * fator);
    }

    public int getX() {
        return x;
    }
    public Lado getLado() {
        return lado;
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