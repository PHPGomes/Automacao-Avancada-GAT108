package com.example.autotarget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class GameView extends View {

    private Paint paint1;
    private Paint paint2;
    private Paint line;

    public GameView(Context context) {
        super(context);
        paint1 = new Paint();
        paint2 = new Paint();
        line = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint1.setColor(Color.RED);
        paint2.setColor(Color.BLUE);
        line.setColor(Color.BLACK);

        // Desenha um alvo simples
        canvas.drawCircle(500, 500, 50, paint1);
        canvas.drawCircle(500,100,100,paint2);
        canvas.drawLine(0,1200,1080,1200,line);
    }
}