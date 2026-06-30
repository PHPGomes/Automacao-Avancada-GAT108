package com.example.autotarget;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Random;

public class Alvo extends Thread {

    private static final int RAIO_PADRAO = 30;
    private static final int MARGEM_DESTINO = 100;
    private static final int INTERVALO_MOVIMENTO_MS = 16;
    private static final double PERCENTUAL_RUIDO = 0.05;

    private int x;
    private int y;

    private final int tamX;
    private final int tamY;
    private final int raio;

    protected int vel;

    private int destinoX;
    private int destinoY;

    private int ultimoX;
    private int ultimoY;

    private double vx;
    private double vy;
    private final double demanda;

    protected final Paint paint;

    private volatile boolean running = true;

    private Lado lado;

    private final Jogo jogo;
    private final Random random = new Random();

    public Alvo(
            int x,
            int y,
            int tamX,
            int tamY,
            GameView gameView,
            Jogo jogo,
            int vel
    ) {

        this.x = x;
        this.y = y;
        this.tamX = tamX;
        this.tamY = tamY;
        this.raio = RAIO_PADRAO;
        this.vel = vel;
        this.jogo = jogo;

        this.ultimoX = x;
        this.ultimoY = y;

        this.lado = calcularLadoAtual();

        this.demanda = vel / 10.0;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        atualizarDestino();
    }

    public void atualizarDestino() {

        if (!dimensoesValidas()) {
            destinoX = x;
            destinoY = y;
            return;
        }

        int larguraUtil = Math.max(1, tamX - 2 * MARGEM_DESTINO);
        int alturaUtil = Math.max(1, tamY - 2 * MARGEM_DESTINO);

        destinoX = random.nextInt(larguraUtil) + MARGEM_DESTINO;
        destinoY = random.nextInt(alturaUtil) + MARGEM_DESTINO;
    }

    public void mover() {

        x = aproximar(x, destinoX, vel);
        y = aproximar(y, destinoY, vel);

        atualizarVelocidade();

        if (x == destinoX && y == destinoY) {
            atualizarDestino();
        }

        limitarDentroDaTela();

        verificarMudancaDeLado();
    }

    private int aproximar(int atual, int destino, int velocidade) {

        int erro = destino - atual;

        if (Math.abs(erro) <= velocidade) {
            return destino;
        }

        return atual + (erro > 0 ? velocidade : -velocidade);
    }

    private void atualizarVelocidade() {

        vx = x - ultimoX;
        vy = y - ultimoY;

        ultimoX = x;
        ultimoY = y;
    }

    private void limitarDentroDaTela() {

        if (!dimensoesValidas()) {
            return;
        }

        x = Math.max(raio, Math.min(x, tamX - raio));
        y = Math.max(raio, Math.min(y, tamY - raio));
    }

    private void verificarMudancaDeLado() {

        Lado ladoAnterior = lado;
        lado = calcularLadoAtual();

        if (ladoAnterior != lado && jogo != null) {
            jogo.transferirAlvo(this, lado);
        }
    }

    private Lado calcularLadoAtual() {

        if (x < tamX / 2) {
            return Lado.ESQUERDO;
        }

        return Lado.DIREITO;
    }

    private boolean dimensoesValidas() {
        return tamX > 0 && tamY > 0;
    }

    private double ruidoGaussiano(double valor) {

        double desvio = Math.abs(valor * PERCENTUAL_RUIDO);

        if (desvio == 0) {
            desvio = PERCENTUAL_RUIDO;
        }

        return random.nextGaussian() * desvio;
    }

    public synchronized LeituraSensor gerarLeitura() {

        double leituraX = x + ruidoGaussiano(x);
        double leituraY = y + ruidoGaussiano(y);
        double leituraVx = vx + ruidoGaussiano(vx);
        double leituraVy = vy + ruidoGaussiano(vy);

        return new LeituraSensor(
                leituraX,
                leituraY,
                leituraVx,
                leituraVy
        );
    }

    public synchronized void draw(Canvas canvas) {
        canvas.drawCircle(x, y, raio, paint);
    }

    public void parar() {
        running = false;
        interrupt();
    }

    public boolean getRunning() {
        return running;
    }

    public void setRunning(Boolean set) {
        running = Boolean.TRUE.equals(set);
    }

    public void Atualizadestino() {
        atualizarDestino();
    }

    public GameView getGameView() {
        return null;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getTamX() {
        return tamX;
    }

    public int getTamY() {
        return tamY;
    }

    public Lado getLado() {
        return lado;
    }

    public int getRaio() {
        return raio;
    }

    public double getDemanda() {
        return demanda;
    }

    @Override
    public void run() {

        setPriority(Thread.MIN_PRIORITY);

        while (running) {

            if (dimensoesValidas()) {
                mover();
            }

            try {

                Thread.sleep(INTERVALO_MOVIMENTO_MS);

            } catch (InterruptedException e) {

                running = false;
                Thread.currentThread().interrupt();
            }
        }
    }
}