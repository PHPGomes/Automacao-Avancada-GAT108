package com.example.autotarget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;

public class Canhao extends Thread {

    private int x, y, size,numBalas;
    private Paint paint;
    private Path path;
    private boolean running = true;
    private GameView gameView;
    private List<Bala> municao;
    private List<Bala> balasAtivas;
    private long ultimoTiro;
    private int delayTiros;
    private List<Alvo> a;

    public Canhao(int x, int y,GameView gameView,List<Alvo> a) {
        this.x = x;
        this.y = y;
        this.size = 60;
        this.gameView = gameView;
        this.a = a;
        numBalas = 10;
        ultimoTiro = 0;
        delayTiros = 1500;

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);

        path = new Path();

        municao = new ArrayList<>();
        balasAtivas = new ArrayList<>();
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

            Bala b = new Bala(x, y + size, aX, aY, gameView);

            b.setAtiva();
            balasAtivas.add(b);

            b.start();

            numBalas--;
            ultimoTiro = System.currentTimeMillis();
        }
    }


    public void parar() {
        running = false;
    }

    public List<Bala> getBalasAtivas(){
        return balasAtivas;
    }

    private void verificaMunicao(){
        if(numBalas <= 0 && balasAtivas.isEmpty()){
            running = false;
        }
    }
    private Alvo escolherAlvo() {
        if (a.isEmpty()) return null;

        Alvo alvoEscolhido = null;
        double menorDistancia = Double.MAX_VALUE;

        for (Alvo alvo : a) {
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
    public int numBalas(){
        return municao.size();
    }

    public int getX(){
        return x;
    }

    @Override
    public void run() {
        while (running) {
            atirar();
            verificaMunicao();

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