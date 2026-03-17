package com.example.autotarget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class GameView extends View {

    private Paint paint;

    public GameView(Context context) {
        super(context);
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.RED);

        // Desenha um alvo simples
        canvas.drawCircle(500, 500, 50, paint);
    }
}