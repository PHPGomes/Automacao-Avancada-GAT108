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

    private int pontuacao1,pontuacao2;
    private Paint texto1;
    private Paint texto2;


    Random random = new Random();

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        canhoes = new ArrayList<>();
        alvos = new ArrayList<>();
        texto1 = new Paint();
        texto2 = new Paint();
        line = new Paint();

        pontuacao1 = 0;
        pontuacao2 = 0;
    }

    private void verificarColisoes() {

        for (Alvo a : alvos) {
            if (a == null) continue;

            for (Canhao c : canhoes) {
                if (c == null) continue;

                for (Bala b : c.getBalasAtivas()) {
                    if (b == null) continue;

                    int dx = b.getX() - a.getX();
                    int dy = b.getY() - a.getY();

                    double distancia = Math.sqrt(dx * dx + dy * dy);

                    if (distancia < a.getRaio()) {

                        // ACERTOU
                        a.parar();
                        b.parar();
                        if(c.getX()<540){pontuacao1 = pontuacao1 + 1;}
                        else{pontuacao2 = pontuacao2 + 1;}
                        alvos.remove(a);
                        c.getBalasAtivas().remove(b);

                        return; // evita crash
                    }
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.DKGRAY);

        line.setColor(Color.BLACK);
        line.setStrokeWidth(5);
        canvas.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight(), line);
        texto1.setColor(Color.BLACK);
        texto1.setTextSize(60);
        texto2.setColor(Color.BLACK);
        texto2.setTextSize(60);

        canvas.drawText("Pontos: " + pontuacao1, 50, 80, texto1);
        canvas.drawText("Pontos: " + pontuacao2, 590, 80, texto2);

        verificarColisoes();

        // Exibir Canhões
        for (Canhao c : canhoes) {
            if (c != null) {
                if(c.numBalas()>0) {
                    c.draw(canvas);
                }
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
                List<Bala> m = c.getBalasAtivas();
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
        int tipo;
        if(random.nextInt(100)>70){tipo = 2;}
        else{tipo = 1;}
        Alvo a = new Alvo(getWidth()/2, getHeight()/2, getWidth(), getHeight(),tipo,this);
        alvos.add(a);
        a.start();
    }
}