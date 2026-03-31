package com.example.autotarget;

// [ Import ]

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import android.util.AttributeSet;

public class GameView extends View {

    // [ Atributos ]
    private List<Canhao> canhoes;
    private List<Alvo> alvos;
    private Paint line;

    private GameView gameView;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        canhoes = new ArrayList<>();
        alvos = new ArrayList<>();

        // cria só o canhão aqui (ok)
        canhoes.add(new Canhao(500, 200));

        line = new Paint();
    }

    // Criar alvos
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (alvos.isEmpty()) {
            Alvo a = new Alvo(w/2, h/2, w, h, this);
            a.start();
            alvos.add(a);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // fundo branco (evita tela preta)
        canvas.drawColor(Color.WHITE);

        // linha do meio
        line.setColor(Color.BLACK);
        line.setStrokeWidth(5);
        canvas.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight(), line);

        // desenha canhões
        for (Canhao c : canhoes) {
            c.draw(canvas);
        }

        // desenha alvos
        for (Alvo a : alvos) {
            a.draw(canvas);
        }

        postInvalidate(); // 🔥 redesenho contínuo
    }

    public void adicionarCanhao() {
        int x = 200 + (int)(Math.random() * 600);
        int y = 200;

        canhoes.add(new Canhao(x, y));
        invalidate();
    }
}