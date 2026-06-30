package com.example.autotarget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class GameView extends View {

    private Jogo jogo;

    private final Paint textoPrincipal;
    private final Paint linhaCentral;
    private final Paint campoEsquerdo;
    private final Paint campoDireito;
    private final Paint textoVencedor;
    private boolean renderizando = false;
    private final Runnable renderLoop = new Runnable() {
        @Override
        public void run() {
            if (!renderizando) {
                return;
            }

            invalidate();

            postDelayed(this, 16);
        }
    };

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        textoPrincipal = new Paint(Paint.ANTI_ALIAS_FLAG);
        textoPrincipal.setColor(Color.BLACK);
        textoPrincipal.setTextSize(60);

        linhaCentral = new Paint(Paint.ANTI_ALIAS_FLAG);
        linhaCentral.setColor(Color.BLACK);
        linhaCentral.setStrokeWidth(5);

        campoEsquerdo = new Paint();
        campoEsquerdo.setColor(Color.rgb(180, 220, 255));

        campoDireito = new Paint();
        campoDireito.setColor(Color.rgb(255, 200, 200));

        textoVencedor = new Paint(Paint.ANTI_ALIAS_FLAG);
        textoVencedor.setColor(Color.WHITE);
        textoVencedor.setTextSize(100);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (jogo == null) {
            jogo = new Jogo(this);
        }

        renderizando = true;
        post(renderLoop);
    }

    @Override
    protected void onDetachedFromWindow() {
        renderizando = false;
        removeCallbacks(renderLoop);

        if (jogo != null) {
            jogo.parar();
        }

        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int largura, int altura, int oldw, int oldh) {
        super.onSizeChanged(largura, altura, oldw, oldh);

        if (jogo != null && !jogo.isAlive()) {
            jogo.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (jogo == null || getWidth() == 0 || getHeight() == 0) {
            return;
        }

        desenharCampo(canvas);
        desenharInterface(canvas);
        desenharObjetos(canvas);

        if (jogo.isJogoFinalizado()) {
            desenharResultado(canvas);
        }
    }

    private void desenharCampo(Canvas canvas) {
        int largura = getWidth();
        int altura = getHeight();
        int metade = largura / 2;

        canvas.drawColor(Color.BLACK);

        canvas.drawRect(0, 0, metade, altura, campoEsquerdo);
        canvas.drawRect(metade, 0, largura, altura, campoDireito);
        canvas.drawLine(metade, 0, metade, altura, linhaCentral);
    }

    private void desenharInterface(Canvas canvas) {
        int largura = getWidth();
        int altura = getHeight();
        int metade = largura / 2;

        canvas.drawText(
                "Pontos: " + jogo.getPontuacao1(),
                50,
                80,
                textoPrincipal
        );

        canvas.drawText(
                "Pontos: " + jogo.getPontuacao2(),
                metade + 50,
                80,
                textoPrincipal
        );

        canvas.drawText(
                "Energia: " + (int) jogo.getEnergiaEsquerda(),
                50,
                150,
                textoPrincipal
        );

        canvas.drawText(
                "Energia: " + (int) jogo.getEnergiaDireita(),
                metade + 50,
                150,
                textoPrincipal
        );

        canvas.drawText(
                "Tempo: " + jogo.getTempoRestante(),
                metade - 180,
                altura - 80,
                textoPrincipal
        );
    }

    private void desenharObjetos(Canvas canvas) {

        for (Canhao canhao : jogo.getSnapshotCanhoes()) {
            if (canhao != null && canhao.getRunning()) {
                canhao.draw(canvas);
            }
        }

        for (Alvo alvo : jogo.getSnapshotAlvos()) {
            if (alvo != null && alvo.getRunning()) {
                alvo.draw(canvas);
            }
        }

        for (Bala bala : jogo.getSnapshotBalas()) {
            if (bala != null && bala.getRunning()) {
                bala.draw(canvas);
            }
        }
    }

    private void desenharResultado(Canvas canvas) {
        String resultado;

        if (jogo.getPontuacao1() > jogo.getPontuacao2()) {
            resultado = "ESQUERDA VENCEU";
        } else if (jogo.getPontuacao2() > jogo.getPontuacao1()) {
            resultado = "DIREITA VENCEU";
        } else {
            resultado = "EMPATE";
        }

        float larguraTexto = textoVencedor.measureText(resultado);

        canvas.drawText(
                resultado,
                (getWidth() - larguraTexto) / 2f,
                getHeight() / 2f,
                textoVencedor
        );
    }


    public void adicionarCanhao(Lado lado) {
        if (jogo != null) {
            jogo.adicionarCanhao(lado);
            invalidate();
        }
    }

    public void iniciarJogo() {
        if (jogo != null) {
            jogo.iniciarPartida();
        }
    }

    public Jogo getJogo() {
        return jogo;
    }
}