package com.example.autotarget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class Canhao extends View {

    private int x, y, size;
    private Paint paint;
    private Path path;

    public Canhao(Context context, int x, int y) {
        super(context);
        this.x = x;
        this.y = y;
        size = 150;
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);

        path = new Path();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        path.reset();

        // Topo
        path.moveTo(x, y);

        // Base esquerda
        path.lineTo(x - size, y + size);

        // Base direita
        path.lineTo(x + size, y + size);

        path.close();

        canvas.drawPath(path, paint);
    }


}