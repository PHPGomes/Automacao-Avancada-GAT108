package com.example.autotarget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.List;

public class Canhao extends Thread {

    private static final int TAMANHO = 60;
    private static final int RAIO_DESTINO = 20;
    private static final int VELOCIDADE_MOVIMENTO = 4;

    private static final int LIMITE_CANHOES = 5;
    private static final int DELAY_BASE = 1500;
    private static final int NUM_BALAS_INICIAL = 999;

    private int x;
    private int y;

    private int destinoX;
    private int destinoY;

    private int numBalas = NUM_BALAS_INICIAL;

    private final Paint paint;
    private final Path path;

    private volatile boolean running = true;

    private long ultimoTiro = 0;

    private final GameView gameView;
    private final Jogo jogo;
    private final Lado lado;

    public Canhao(int x, int y, GameView gameView, Jogo jogo, Lado lado) {

        this.x = x;
        this.y = y;

        this.destinoX = x;
        this.destinoY = y;

        this.gameView = gameView;
        this.jogo = jogo;
        this.lado = lado;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);

        path = new Path();
    }

    public void draw(Canvas canvas) {

        path.reset();

        path.moveTo(x, y);
        path.lineTo(x - TAMANHO, y + TAMANHO);
        path.lineTo(x + TAMANHO, y + TAMANHO);
        path.close();

        canvas.drawPath(path, paint);
        canvas.drawCircle(destinoX, destinoY, RAIO_DESTINO, paint);
    }

    public void atirar() {

        if (!temEnergia() || numBalas <= 0) {
            return;
        }

        long agora = System.currentTimeMillis();
        int delayAtual = calcularDelay();

        if (agora - ultimoTiro <= delayAtual) {
            return;
        }

        Alvo alvo = escolherAlvo();

        if (alvo == null) {
            return;
        }

        if (!jogo.consumirEnergia(lado)) {
            return;
        }

        jogo.criarBala(
                x,
                y + TAMANHO,
                alvo.getX(),
                alvo.getY(),
                this
        );

        numBalas--;
        ultimoTiro = agora;
    }

    private void mover() {
        x = aproximar(x, destinoX, VELOCIDADE_MOVIMENTO);
        y = aproximar(y, destinoY, VELOCIDADE_MOVIMENTO);
    }

    private int aproximar(int atual, int destino, int velocidade) {

        int erro = destino - atual;

        if (Math.abs(erro) <= velocidade) {
            return destino;
        }

        return atual + (erro > 0 ? velocidade : -velocidade);
    }

    private boolean temEnergia() {

        if (lado == Lado.ESQUERDO) {
            return jogo.getEnergiaEsquerda() > 0;
        }

        return jogo.getEnergiaDireita() > 0;
    }

    public void definirDestino(int x, int y) {
        destinoX = x;
        destinoY = y;
    }

    public boolean getRunning() {
        return running;
    }

    public void parar() {
        running = false;
        interrupt();
    }

    private Alvo escolherAlvo() {

        List<Alvo> alvos = jogo.getAlvosPorLado(lado);

        if (alvos == null || alvos.isEmpty()) {
            return null;
        }

        Alvo alvoEscolhido = null;
        double menorDistancia = Double.MAX_VALUE;

        for (Alvo alvo : alvos) {

            if (alvo == null || !alvo.getRunning()) {
                continue;
            }

            if (alvo.getLado() != lado) {
                continue;
            }

            double distancia = calcularDistancia(alvo);

            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                alvoEscolhido = alvo;
            }
        }

        return alvoEscolhido;
    }

    private double calcularDistancia(Alvo alvo) {

        double dx = alvo.getX() - x;
        double dy = alvo.getY() - y;

        return Math.sqrt(dx * dx + dy * dy);
    }

    private int calcularDelay() {

        int quantidade =
                lado == Lado.ESQUERDO
                        ? jogo.getCanhoesEsquerda().size()
                        : jogo.getCanhoesDireita().size();

        double fator = 1.0;

        if (quantidade > LIMITE_CANHOES) {
            int excesso = quantidade - LIMITE_CANHOES;
            fator += excesso * 0.2;
        }

        if (jogo.isOverheating()) {
            fator *= 1.5;
        }

        return (int) (DELAY_BASE * fator);
    }

    public double getCapacidade() {
        return 1000.0 / calcularDelay();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Lado getLado() {
        return lado;
    }

    @Override
    public void run() {

        setPriority(Thread.NORM_PRIORITY);

        while (running) {

            mover();
            atirar();

            if (numBalas <= 0) {
                running = false;
            }

            try {

                Thread.sleep(50);

            } catch (InterruptedException e) {

                running = false;
                Thread.currentThread().interrupt();
            }
        }
    }
}