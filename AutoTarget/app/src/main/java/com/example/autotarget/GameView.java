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
    private List<Alvo> alvos;
    private Paint line;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        canhoes = new ArrayList<>();
        alvos = new ArrayList<>();

        canhoes.add(new Canhao(500, 200));

        line = new Paint();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w > 0 && h > 0 && alvos.isEmpty()) {
            Alvo a = new Alvo(w/2, h/2, w, h, this);
            alvos.add(a);
            a.start(); // ✅ depois de adicionar (mais seguro)
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.WHITE);

        line.setColor(Color.BLACK);
        line.setStrokeWidth(5);
        canvas.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight(), line);

        for (Canhao c : canhoes) {
            if (c != null) {
                c.draw(canvas);
            }
        }

        for (Alvo a : alvos) {
            if (a != null) {
                a.draw(canvas);
            }
        }

        // 🔥 garante atualização contínua (extra segurança)
        invalidate();
    }

    public void adicionarCanhao() {
        int x = 200 + (int)(Math.random() * 600);
        int y = 200;

        canhoes.add(new Canhao(x, y));
        invalidate();
    }
    public void iniciarJogo() {
        if (alvos.isEmpty()) {
            Alvo a = new Alvo(getWidth()/2, getHeight()/2, getWidth(), getHeight(), this);
            alvos.add(a);
            a.start();
        }
    }
}