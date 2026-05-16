package com.example.autotarget;

import android.graphics.Paint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.CopyOnWriteArrayList;

public class Jogo extends Thread {

    private List<Canhao> canhoesEsquerda;
    private List<Canhao> canhoesDireita;
    private List<Alvo> alvosEsquerda;
    private List<Alvo> alvosDireita;
    private List<Bala> balas;
    private Map<Alvo, List<LeituraSensor>> bufferEsquerda;
    private Map<Alvo, List<LeituraSensor>> bufferDireita;

    private int pontuacao1, pontuacao2;
    private int numAlvos;
    private boolean running = true;
    private GameView gameView;
    private Random random = new Random();
    private int iniEnergiaEsquerda = 100;
    private int iniEnergiaDireita = 100;
    private int energiaEsquerda;
    private int energiaDireita;
    private int tempoRestante;
    private int tempoInicial = 60;
    private long ultimoSegundo;
    private long ultimaColeta;
    private boolean jogoFinalizado = false;
    private boolean partidaIniciada = false;

    private Semaphore semaforoAlvos = new Semaphore(1);
    private Semaphore semaforoBalas = new Semaphore(1);
    private Semaphore semaforoCanhoes = new Semaphore(1);

    public Jogo(GameView gameView) {
        this.gameView = gameView;
        canhoesEsquerda = new CopyOnWriteArrayList<>();
        canhoesDireita = new CopyOnWriteArrayList<>();
        alvosEsquerda = new CopyOnWriteArrayList<>();
        alvosDireita = new CopyOnWriteArrayList<>();
        balas = new CopyOnWriteArrayList<>();
        bufferEsquerda = new HashMap<>();
        bufferDireita = new HashMap<>();
        numAlvos = 5;
        ultimaColeta = 0;
        energiaEsquerda = iniEnergiaEsquerda;
        energiaDireita = iniEnergiaDireita;
    }

    public void atualizar() {
        verificarColisoes();
        removerMortos();
        if (!jogoFinalizado && getAlvos().isEmpty()) {
            criarOnda();
        }
    }


    private void atualizarTempo() {

        if (System.currentTimeMillis() - ultimoSegundo >= 1000) {

            tempoRestante--;

            ultimoSegundo = System.currentTimeMillis();

            if (tempoRestante <= 0) {

                finalizarJogo();
            }
        }
    }
    public synchronized boolean consumirEnergia(Lado lado) {

        if (lado == Lado.ESQUERDO) {
            if (energiaEsquerda <= 0) {
                return false;
            }
            energiaEsquerda--;
            return true;
        }
        if (energiaDireita <= 0) {
            return false;
        }
        energiaDireita--;
        return true;
    }

    private void verificarColisoes() {
        for (Alvo a : alvosEsquerda) {

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

                            if (canhaoAtirador.getLado() == Lado.ESQUERDO) {
                                pontuacao1++;
                            } else {
                                pontuacao2++;
                            }
                        }
                    }
                }
            }
        }

        for (Alvo a : alvosDireita) {

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

                            if (canhaoAtirador.getLado() == Lado.ESQUERDO) {
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
            alvosEsquerda.removeIf(a -> !a.getRunning());
            alvosDireita.removeIf(a -> !a.getRunning());
            semaforoAlvos.release();

            semaforoBalas.acquire();
            balas.removeIf(p -> !p.getRunning());
            semaforoBalas.release();

            semaforoCanhoes.acquire();
            canhoesEsquerda.removeIf(c -> !c.getRunning());
            canhoesDireita.removeIf(c -> !c.getRunning());
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

    public void criarOnda() {
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
                    a = new AlvoComum(gameView.getWidth() / 2, gameView.getHeight() / 2, gameView.getWidth(), gameView.getHeight(), gameView,this);
                } else {
                    a = new AlvoRapido(gameView.getWidth() / 2, gameView.getHeight() / 2, gameView.getWidth(), gameView.getHeight(), gameView,this);
                }
                if (a.getLado() == Lado.ESQUERDO) {
                    alvosEsquerda.add(a);
                } else {
                    alvosDireita.add(a);
                }
                a.start();
            }
            numAlvos += 5;
            semaforoAlvos.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void coletarDadosSensores() {

        if (System.currentTimeMillis() - ultimaColeta < 1000) {
            return;
        }

        ultimaColeta = System.currentTimeMillis();

        coletarLado(alvosEsquerda, bufferEsquerda);

        coletarLado(alvosDireita, bufferDireita);
    }

    private void coletarLado(
            List<Alvo> alvos,
            Map<Alvo, List<LeituraSensor>> buffer
    ) {

        for (Alvo alvo : alvos) {

            LeituraSensor leitura = alvo.gerarLeitura();

            buffer.putIfAbsent(
                    alvo,
                    new ArrayList<>()
            );

            List<LeituraSensor> historico =
                    buffer.get(alvo);

            historico.add(leitura);

            if (historico.size() > 10) {
                historico.remove(0);
            }
        }
    }

    public void iniciarPartida() {

        pararObjetosAtivos();

        energiaEsquerda = iniEnergiaEsquerda;
        energiaDireita = iniEnergiaDireita;

        pontuacao1 = 0;
        pontuacao2 = 0;

        tempoRestante = tempoInicial;

        jogoFinalizado = false;

        partidaIniciada = true;

        ultimoSegundo = System.currentTimeMillis();

        criarOnda();
    }

    private void pararObjetosAtivos() {

        for (Alvo a : getAlvos()) {
            a.parar();
        }

        for (Canhao c : getCanhoes()) {
            c.parar();
        }

        for (Bala b : balas) {
            b.parar();
        }

        alvosEsquerda.clear();
        alvosDireita.clear();

        canhoesEsquerda.clear();
        canhoesDireita.clear();

        balas.clear();
    }

    public void adicionarCanhao(Lado lado) {
        try {
            // Garante que o GameView tenha dimensões válidas antes de adicionar canhão
            if (gameView.getWidth() == 0 || gameView.getHeight() == 0) {
                System.err.println("GameView ainda não tem dimensões válidas. Tente novamente.");
                return;
            }

            int metade = gameView.getWidth() / 2;
            int margem = 80;
            int x;
            if (lado == Lado.ESQUERDO) {

                x = margem + random.nextInt(metade - margem - 80);

            } else {

                x = metade + 80 + random.nextInt(metade - margem - 80);
            }
            if (lado == Lado.ESQUERDO && energiaEsquerda <= 0) {
                return;
            }
            if (lado == Lado.DIREITO && energiaDireita <= 0) {
                return;
            }
            int y = 100 + random.nextInt(gameView.getHeight() - 200);

            if (x < 0 || x > gameView.getWidth() || y < 0 || y > gameView.getHeight()) {
                throw new JogoException("Tentativa de adicionar canhão fora dos limites da tela.");
            }

            semaforoCanhoes.acquire();
            Canhao novoCanhao = new Canhao(x, y, gameView, this,lado);
            if (novoCanhao.getLado() == Lado.ESQUERDO) {
                canhoesEsquerda.add(novoCanhao);
            } else {
                canhoesDireita.add(novoCanhao);
            }
            novoCanhao.start();
            semaforoCanhoes.release();

        } catch (JogoException e) {
            System.err.println("Erro do Jogo: " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<Alvo> getAlvosPorLado(Lado lado) {

        if (lado == Lado.ESQUERDO) {
            return alvosEsquerda;
        }

        return alvosDireita;
    }


    public synchronized void transferirAlvo(Alvo alvo, Lado novoLado) {

        if (novoLado == Lado.ESQUERDO) {

            alvosDireita.remove(alvo);

            if (!alvosEsquerda.contains(alvo)) {
                alvosEsquerda.add(alvo);
            }

        } else {

            alvosEsquerda.remove(alvo);

            if (!alvosDireita.contains(alvo)) {
                alvosDireita.add(alvo);
            }
        }
    }
    private void finalizarJogo() {

        jogoFinalizado = true;

        for (Alvo a : getAlvos()) {
            a.parar();
        }

        for (Canhao c : getCanhoes()) {
            c.parar();
        }

        for (Bala b : balas) {
            b.parar();
        }
    }
    public List<Canhao> getCanhoes() {
        List<Canhao> todos = new ArrayList<>();
        todos.addAll(canhoesEsquerda);
        todos.addAll(canhoesDireita);
        return todos;
    }

    public List<Alvo> getAlvos() {
        List<Alvo> todos = new ArrayList<>();
        todos.addAll(alvosEsquerda);
        todos.addAll(alvosDireita);
        return todos;
    }

    public List<Bala> getBalas() {
        return balas;
    }
    public List<Canhao> getCanhoesEsquerda() { return canhoesEsquerda; }
    public List<Canhao> getCanhoesDireita() { return canhoesDireita; }
    public int getPontuacao1() {
        return pontuacao1;
    }
    public int getTempoRestante() { return tempoRestante; }
    public boolean isJogoFinalizado() { return jogoFinalizado; }
    public int getPontuacao2() {
        return pontuacao2;
    }
    public double getEnergiaEsquerda() { return energiaEsquerda; }
    public double getEnergiaDireita() { return energiaDireita; }

    public void parar() {
        running = false;
        for (Alvo a : alvosEsquerda) a.parar();
        for (Alvo a : alvosDireita) a.parar();
        for (Canhao c : canhoesEsquerda) c.parar();
        for (Canhao c : canhoesDireita) c.parar();
        for (Bala b : balas) b.parar();
    }

    @Override
    public void run() {
        while (running) {
            if (partidaIniciada && !jogoFinalizado) {
                atualizar();
                atualizarTempo();
                coletarDadosSensores();
            }
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}