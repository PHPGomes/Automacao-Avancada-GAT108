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

    public Bala(int xCanhao, int yCanhao, int xAlvo,int yAlvo){
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);

        path = new Path();

        x = xCanhao;
        y = yCanhao;
        this.xAlvo = xAlvo;
        this.yAlvo = yAlvo;
        size = 45;
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

    private void mover(){ // definir como bala vai se mover

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
