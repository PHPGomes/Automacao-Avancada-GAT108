package com.example.autotarget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import android.util.AttributeSet;

public class GameView extends View {

    private List<Canhao> canhoes;
    private Alvo alvo;
    private Paint line;



    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        canhoes = new ArrayList<>();
        alvo = new Alvo(getContext(), 500, 500);
        canhoes.add(new Canhao(getContext(), 500, 200));
        line = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        line.setColor(Color.BLACK);

        canvas.drawLine(0, 1200, 1080, 1200, line);

        for (Canhao c : canhoes) {
            c.draw(canvas);
        }

        alvo.draw(canvas);
    }

    public void adicionarCanhao() {
        int x = 200 + (int)(Math.random() * 600);
        int y = 200;

        canhoes.add(new Canhao(getContext(), x, y));
        invalidate();
    }
}