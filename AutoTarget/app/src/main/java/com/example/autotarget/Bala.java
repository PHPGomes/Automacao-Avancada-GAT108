package com.example.autotarget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class Bala extends Thread{

    private Paint paint;
    private Path path;
    private boolean running = true;
    private GameView gameView;

    private int x,y,size,xAlvo,yAlvo,vel;
    private long lastMove;
    private boolean ativa;

    public Bala(int xCanhao, int yCanhao, int xAlvo,int yAlvo,GameView gameView){
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);

        path = new Path();

        x = xCanhao;
        y = yCanhao;
        this.xAlvo = xAlvo;
        this.yAlvo = yAlvo;
        this.gameView = gameView;
        size = 12;
        lastMove = 0;
        vel = 10;
        ativa = false;
    }

    public void draw(Canvas canvas) { //criar img bala

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

            if (xAlvo > x) x = Math.min(x + vel, xAlvo);
            else if (xAlvo < x) x = Math.max(x - vel, xAlvo);

            if (yAlvo > y) y = Math.min(y + vel, yAlvo);
            else if (yAlvo < y) y = Math.max(y - vel, yAlvo);
        }

        // chegou no destino
        if (x == xAlvo && y == yAlvo) {
            parar();
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setAtiva(){
        ativa = true;
    }

    public void setAlvo(int x, int y){
        xAlvo = x;
        yAlvo = y;
    }

    public boolean getAtividade(){
        return ativa;
    }

    public void parar() {
        running = false;
    }

    @Override
    public void run() {
        while (running) { // corrigido
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
