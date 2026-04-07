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

    public Canhao(int x, int y,GameView gameView) {
        this.x = x;
        this.y = y;
        this.size = 60;
        this.gameView = gameView;
        numBalas = 10;
        ultimoTiro = 0;
        delayTiros = 1500;

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);

        path = new Path();

        municao = new ArrayList<>();
        balasAtivas = new ArrayList<>();
        for(int c = 0; c < numBalas; c++){
            municao.add(new Bala(x,y+size,x,3000,gameView));
        }
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

    public List<Bala> getMunicoes(){
        return municao;
    }

    public void atirar(){
        if(!municao.isEmpty()){
            if(System.currentTimeMillis() - ultimoTiro > delayTiros){

                Bala b = municao.get(0);
                municao.remove(0);

                b.setAtiva();
                balasAtivas.add(b);


                b.start();

                ultimoTiro = System.currentTimeMillis();
            }
        }
    }


    public void parar() {
        running = false;
    }

    public List<Bala> getBalasAtivas(){
        return balasAtivas;
    }

    @Override
    public void run() {
        while (running) {
            atirar();
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