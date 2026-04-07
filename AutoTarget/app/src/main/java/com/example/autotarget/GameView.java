package com.example.autotarget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import android.util.AttributeSet;

import java.util.Random;

public class GameView extends View {

    private List<Canhao> canhoes;
    private List<Alvo> alvos;
    private Paint line;

    Random random = new Random();

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        canhoes = new ArrayList<>();
        alvos = new ArrayList<>();

        canhoes.add(new Canhao(500, 200,this));

        line = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.WHITE);

        line.setColor(Color.BLACK);
        line.setStrokeWidth(5);
        canvas.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight(), line);

        // Exibir Canhões
        for (Canhao c : canhoes) {
            if (c != null) {
                c.draw(canvas);
            }
        }

        // Exibir Alvos
        for (Alvo a : alvos) {
            if (a != null) {
                a.draw(canvas);
            }
        }

        // Exibir Balas
        for(Canhao c : canhoes) {
            if(c != null){
                List<Bala> m = c.getMunicoes();
                for(Bala b : m){
                    if(b != null){
                        b.draw(canvas);
                    }
                }
            }
        }
    }

    public void adicionarCanhao() {
        int x = 200 + (int)(Math.random() * 600);
        int y = 100 + (int)(Math.random() * 2000);

        canhoes.add(new Canhao(x, y,this));
        Canhao c = canhoes.get(canhoes.size() - 1);
        c.start();
        c.atirar();
        invalidate();
    }
    public void iniciarJogo() {
        int tipo = random.nextInt(2)+1;
        Alvo a = new Alvo(getWidth()/2, getHeight()/2, getWidth(), getHeight(),tipo,this);
        alvos.add(a);
        a.start();
    }
}