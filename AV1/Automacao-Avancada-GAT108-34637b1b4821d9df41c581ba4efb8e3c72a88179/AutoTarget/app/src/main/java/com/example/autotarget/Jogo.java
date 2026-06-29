package com.example.autotarget;

import android.graphics.Paint;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.CopyOnWriteArrayList;

public class Jogo extends Thread {

    private List<Canhao> canhoes;
    private List<Alvo> alvos;
    private List<Bala> balas;

    private int pontuacao1, pontuacao2;
    private int numAlvos;
    private boolean running = true;
    private GameView gameView;
    private Random random = new Random();

    private Semaphore semaforoAlvos = new Semaphore(1);
    private Semaphore semaforoBalas = new Semaphore(1);
    private Semaphore semaforoCanhoes = new Semaphore(1);

    public Jogo(GameView gameView) {
        this.gameView = gameView;
        canhoes = new CopyOnWriteArrayList<>();
        alvos = new CopyOnWriteArrayList<>();
        balas = new CopyOnWriteArrayList<>();
        numAlvos = 5;
    }

    public void atualizar() {
        verificarColisoes();
        removerMortos();
    }

    private void verificarColisoes() {
        for (Alvo a : alvos) {
            if (a == null || !a.getRunning()) continue;

            for (Bala b : balas) {
                if (b == null || !b.getRunning()) continue;

                synchronized (a) {
                    if (!a.getRunning() || !b.getRunning()) continue;

                    int dx = b.getX() - a.getX();
                    int dy = b.getY() - a.getY();
                    double distancia = Math.sqrt(dx * dx + dy * dy);

                    if (distancia < a.getRaio()) {
                        a.parar();
                        b.parar();

                        Canhao canhaoAtirador = b.getCanhaoAtirador();
                        if (canhaoAtirador != null) {
                            if (canhaoAtirador.getX() < gameView.getWidth() / 2) {
                                pontuacao1++;
                            } else {
                                pontuacao2++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void removerMortos() {
        try {
            semaforoAlvos.acquire();
            alvos.removeIf(a -> !a.getRunning());
            semaforoAlvos.release();

            semaforoBalas.acquire();
            balas.removeIf(p -> !p.getRunning());
            semaforoBalas.release();

            semaforoCanhoes.acquire();
            canhoes.removeIf(c -> !c.getRunning()); // Remove canhões que pararam (sem balas)
            semaforoCanhoes.release();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void criarBala(int x, int y, int alvoX, int alvoY, Canhao canhaoAtirador) {
        try {
            semaforoBalas.acquire();
            Bala b = new Bala(x, y, alvoX, alvoY, gameView, canhaoAtirador);
            balas.add(b);
            b.start();
            semaforoBalas.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void iniciarJogo() {
        try {
            semaforoAlvos.acquire();
            // Garante que o GameView tenha dimensões válidas antes de criar alvos
            if (gameView.getWidth() == 0 || gameView.getHeight() == 0) {
                System.err.println("GameView ainda não tem dimensões válidas. Tente novamente.");
                semaforoAlvos.release();
                return;
            }

            for (int c = 0; c < numAlvos; c++) {
                Alvo a;
                if (random.nextInt(100) < 70) {
                    a = new AlvoComum(gameView.getWidth() / 2, gameView.getHeight() / 2, gameView.getWidth(), gameView.getHeight(), gameView);
                } else {
                    a = new AlvoRapido(gameView.getWidth() / 2, gameView.getHeight() / 2, gameView.getWidth(), gameView.getHeight(), gameView);
                }
                alvos.add(a);
                a.start();
            }
            numAlvos += 5;
            semaforoAlvos.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void adicionarCanhao() {
        try {
            // Garante que o GameView tenha dimensões válidas antes de adicionar canhão
            if (gameView.getWidth() == 0 || gameView.getHeight() == 0) {
                System.err.println("GameView ainda não tem dimensões válidas. Tente novamente.");
                return;
            }

            int x = 100 + random.nextInt(gameView.getWidth() - 200);
            int y = 100 + random.nextInt(gameView.getHeight() - 200);

            if (x < 0 || x > gameView.getWidth() || y < 0 || y > gameView.getHeight()) {
                throw new JogoException("Tentativa de adicionar canhão fora dos limites da tela.");
            }

            semaforoCanhoes.acquire();
            Canhao novoCanhao = new Canhao(x, y, gameView, this);
            canhoes.add(novoCanhao);
            novoCanhao.start();
            semaforoCanhoes.release();

        } catch (JogoException e) {
            System.err.println("Erro do Jogo: " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    public int getPontuacao1() {
        return pontuacao1;
    }

    public int getPontuacao2() {
        return pontuacao2;
    }

    public void parar() {
        running = false;

        for (Alvo a : alvos) a.parar();
        for (Canhao c : canhoes) c.parar();
        for (Bala b : balas) b.parar();
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