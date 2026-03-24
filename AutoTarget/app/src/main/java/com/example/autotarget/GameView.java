package com.example.autotarget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class GameView extends View {

    private Canhao canhao;
    private Alvo alvo;
    private Paint line;

    public GameView(Context context) {
        super(context);
        alvo = new Alvo(getContext(),500,500);
        canhao = new Canhao(getContext(), 500, 200);
        line = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        line.setColor(Color.BLACK);

        // Desenha um alvo simples
        canvas.drawLine(0,1200,1080,1200,line);
        canhao.draw(canvas);
        alvo.draw(canvas);
    }
}