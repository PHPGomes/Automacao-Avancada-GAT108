package com.example.autotarget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class Alvo extends View {
    private int x,y,raio;
    private Paint paint;

    public Alvo(Context context, int x, int y){
        super(context);
        this.x = x;
        this.y = y;
        raio = 50;
        paint = new Paint();
        paint.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(x, y, raio, paint);
    }


}
