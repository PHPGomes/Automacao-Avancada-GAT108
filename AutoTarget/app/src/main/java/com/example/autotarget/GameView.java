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


    private Jogo jogo;
    private Paint texto1;
    private Paint texto2;
    private Paint line;


    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        jogo = new Jogo(this);
        jogo.start();
        texto1 = new Paint();
        texto2 = new Paint();
        line = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        line.setColor(Color.BLACK);
        line.setStrokeWidth(5);
        Paint ladoEsquerdo = new Paint();
        ladoEsquerdo.setColor(Color.rgb(180, 220, 255));
        Paint ladoDireito = new Paint();
        ladoDireito.setColor(Color.rgb(255, 200, 200));
        canvas.drawRect(0, 0, getWidth()/2, getHeight(), ladoEsquerdo);
        canvas.drawRect(getWidth()/2, 0, getWidth(), getHeight(), ladoDireito);
        canvas.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight(), line);
        texto1.setColor(Color.BLACK);
        texto1.setTextSize(60);
        texto2.setColor(Color.BLACK);
        texto2.setTextSize(60);
        canvas.drawText("Pontos: " + jogo.getPontuacao1(), 50, 80, texto1);
        canvas.drawText("Pontos: " + jogo.getPontuacao2(), 590, 80, texto2);
        canvas.drawText("Energia: " + (int) jogo.getEnergiaEsquerda(),50,150,texto1);
        canvas.drawText("Energia: " + (int) jogo.getEnergiaDireita(),getWidth() - 450,150,texto2);

        for (Canhao c : jogo.getCanhoes()) {
            c.draw(canvas);
        }

        for (Alvo a : jogo.getAlvos()) {
            a.draw(canvas);
        }

        for (Bala b : jogo.getBalas()) {
            b.draw(canvas);
        }
    }

    public void adicionarCanhao() {
        jogo.adicionarCanhao();
        invalidate();
    }
    public void iniciarJogo() {
        jogo.iniciarJogo();
    }
}