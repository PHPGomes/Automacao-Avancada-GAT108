package com.example.autotarget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class Canhao {

    private int x, y, size;
    private Paint paint;
    private Path path;

    public Canhao(int x, int y) {
        this.x = x;
        this.y = y;
        this.size = 150;

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
}