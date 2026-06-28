package com.example.autotarget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.util.Random;

public class Alvo extends Thread {

    private int x, y, raio, tamX, tamY;
    protected int vel;
    private long lastMove;
    private GameView gameView;
    private int desX, desY;
    private double vx;
    private double vy;
    private double demanda;
    private int ultimoX;
    private int ultimoY;
    protected Paint paint;
    private boolean running = true;
    private Lado lado;
    private Jogo jogo;

    Random random = new Random();

    public Alvo(int x, int y, int tamX, int tamY, GameView gameView,Jogo jogo, int vel) {
        this.x = x;
        this.y = y;
        if (x < tamX / 2) {
            lado = Lado.ESQUERDO;
        } else {
            lado = Lado.DIREITO;
        }
        this.tamX = tamX;
        this.tamY = tamY;
        this.gameView = gameView;
        this.raio = 30;
        this.vel = vel;
        this.jogo = jogo;
        lastMove = 0;
        demanda = vel/10;

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
        vx = x - ultimoX;
        vy = y - ultimoY;
        ultimoX = x;
        ultimoY = y;

        if (x == desX && y == desY) {
            Atualizadestino();
        }

        if (tamX > 0 && tamY > 0) {
            x = Math.max(raio, Math.min(x, tamX - raio));
            y = Math.max(raio, Math.min(y, tamY - raio));
        }
        verificarMudancaDeLado();
    }
    public synchronized void verificarMudancaDeLado() {

        Lado ladoAnterior = lado;

        if (x < tamX / 2) {
            lado = Lado.ESQUERDO;
        } else {
            lado = Lado.DIREITO;
        }

        if (ladoAnterior != lado) {

            jogo.transferirAlvo(this, lado);

        }
    }

    private double ruidoGaussiano(double valor) {
        double desvio = Math.abs(valor * 0.05);
        return random.nextGaussian() * desvio;
    }

    public synchronized LeituraSensor gerarLeitura() {
        double leituraX = x + ruidoGaussiano(x);
        double leituraY = y + ruidoGaussiano(y);
        double leituraVx = vx + ruidoGaussiano(vx);
        double leituraVy = vy + ruidoGaussiano(vy);
        return new LeituraSensor(leituraX,leituraY,leituraVx,leituraVy);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getTamX() { return tamX; }
    public int getTamY() { return tamY; }
    public Lado getLado() { return lado; }

    public boolean getRunning() { return running; }

    public int getRaio() { return raio; }
    public GameView getGameView() { return gameView; }
    public double getDemanda() { return demanda; }

    public synchronized void draw(Canvas canvas) {
        canvas.drawCircle(x, y, raio, paint);
    }

    public void parar() {
        Log.d("THREADS", "Parou Morreu Acabou ");
        running = false;
    }

    @Override
    public void run() {
        setPriority(Thread.MAX_PRIORITY);
        Log.d("THREADS", "ALVO INICIOU");
        while (running) {

            long inicio = System.nanoTime();
            if (tamX > 0 && tamY > 0 && desX == 0 && desY == 0) {
                Atualizadestino();
            }

            if (tamX > 0 && tamY > 0) {
                mover();
            }
            long fim = System.nanoTime();
            long tempo = (fim - inicio) / 1000000;
            if (gameView != null) {
                gameView.postInvalidate();
            }

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d("THREADS", "ALVO TERMINOU");
    }
}
