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


public class Jogo extends Thread{

    private List<Canhao> canhoes;
    private List<Alvo> alvos;
    private List<Bala> balas;
    private Paint line;

    private int pontuacao1,pontuacao2;
    private Paint texto1;
    private Paint texto2;
    private int numAlvos;
    private List<Canhao> remover;
    private boolean running = true;
    GameView gameView;


    Random random = new Random();

    public Jogo(GameView gameView){
        this.gameView = gameView;
        canhoes = new ArrayList<>();
        alvos = new ArrayList<>();
        texto1 = new Paint();
        texto2 = new Paint();
        line = new Paint();
        remover = new ArrayList<>();
        balas = new ArrayList<>();

        numAlvos = 5;
    }

    public void atualizar() {
        verificarColisoes();
        removerMortos();
    }

    private void verificarColisoes() {

        for (Alvo a : alvos) {
            if (a == null) continue;

            for (Canhao c : canhoes) {
                if (c == null) continue;

                for (Bala b : balas) {
                    if (b == null) continue;

                    int dx = b.getX() - a.getX();
                    int dy = b.getY() - a.getY();

                    double distancia = Math.sqrt(dx * dx + dy * dy);

                    if (distancia < a.getRaio() && b.getRunning()) {

                        // ACERTOU
                        a.parar();
                        b.parar();
                        if(c.getX()<540){pontuacao1 = pontuacao1 + 1;}
                        else{pontuacao2 = pontuacao2 + 1;}
                        alvos.remove(a);
                        balas.remove(b);

                        return; // evita crash
                    }
                }
            }
        }
    }

    private void removerMortos() {
        alvos.removeIf(a -> !a.getRuning());
        balas.removeIf(p -> !p.getRunning());
    }

    public void criarBala(int x, int y, int alvoX, int alvoY) {
        Bala b = new Bala(x, y, alvoX, alvoY, gameView);
        balas.add(b);
        b.start();
    }


    public void iniciarJogo() {
        for(int c = 0; c < numAlvos; c++){
            Alvo a;
            if(random.nextInt(100)>70){a = new AlvoComum(gameView.getWidth()/2, gameView.getHeight()/2, gameView.getWidth(), gameView.getHeight(),gameView);}
            else{a = new AlvoRapido(gameView.getWidth()/2, gameView.getHeight()/2, gameView.getWidth(), gameView.getHeight(),gameView);}
            alvos.add(a);
            a.start();
        }
        numAlvos = numAlvos + 5;
    }

    public void adicionarCanhao() {
        int x = 200 + (int)(Math.random() * 600);
        int y = 100 + (int)(Math.random() * 2000);

        canhoes.add(new Canhao(x, y,gameView,this));
        Canhao c = canhoes.get(canhoes.size() - 1);
        c.start();

    }




    public List<Canhao> getCanhoes() {
        return canhoes;
    }
    public List<Alvo> getAlvos() {
        return alvos;
    }
    public List<Bala> getBalas() {
        return balas;
    }
    public int getPontuacao1(){
        return pontuacao1;
    }
    public int getPontuacao2() {
        return pontuacao2;
    }

    public void parar() {
        running = false;
    }


    @Override
    public void run() {
        while (running) {
            atualizar();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
