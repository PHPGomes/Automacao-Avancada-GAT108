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

    public Canhao(int x, int y,GameView gameView) {
        this.x = x;
        this.y = y;
        this.size = 95;
        this.gameView = gameView;
        numBalas = 10;

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);

        path = new Path();

        municao = new ArrayList<>();
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
            Bala b = municao.get(0);
            b.start();
        }
    }


    public void parar() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
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