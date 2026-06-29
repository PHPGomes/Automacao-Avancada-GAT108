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
    private double[][] matrizIncidencia;
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
    private long ultimaOtimizacao;
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
        ultimaOtimizacao = 0;
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
                semaforoAlvos.release();
                return;
            }
            for (int c = 0; c < numAlvos; c++) {
                Alvo a;
                if (random.nextInt(100) < 70) {
                    a = new AlvoComum(gameView.getWidth() / 2, gameView.getHeight() / 2, gameView.getWidth(), gameView.getHeight(), gameView, this);
                } else {
                    a = new AlvoRapido(gameView.getWidth() / 2, gameView.getHeight() / 2, gameView.getWidth(), gameView.getHeight(), gameView, this);
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

    private void atualizarMatrizIncidencia() {

        List<Alvo> alvos = getAlvos();

        List<Canhao> canhoes = getCanhoes();

        matrizIncidencia =
                new double[alvos.size()][canhoes.size()];

        for (int i = 0; i < alvos.size(); i++) {

            Alvo alvo = alvos.get(i);

            for (int j = 0; j < canhoes.size(); j++) {

                Canhao canhao = canhoes.get(j);

                double dx =
                        alvo.getX() - canhao.getX();

                double dy =
                        alvo.getY() - canhao.getY();

                double distancia =
                        Math.sqrt(dx * dx + dy * dy);

                matrizIncidencia[i][j] =
                        1.0 / (distancia + 1);
            }
        }
    }



    public void adicionarCanhao(Lado lado) {
        try {
            // Garante que o GameView tenha dimensões válidas antes de adicionar canhão
            if (gameView.getWidth() == 0 || gameView.getHeight() == 0) {
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
            Canhao novoCanhao = new Canhao(x, y, gameView, this, lado);
            if (novoCanhao.getLado() == Lado.ESQUERDO) {
                canhoesEsquerda.add(novoCanhao);
            } else {
                canhoesDireita.add(novoCanhao);
            }
            novoCanhao.start();
            semaforoCanhoes.release();

        } catch (JogoException e) {
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

    private double calcularUtilidade(
            List<Canhao> canhoes,
            List<Alvo> alvos
    ) {

        if (canhoes.isEmpty()) {
            return 0;
        }

        double capacidadeTotal = 0;

        for (Canhao c : canhoes) {

            capacidadeTotal += c.getCapacidade();
        }

        // demanda total dos alvos
        double demandaTotal = 0;

        for (Alvo a : alvos) {

            demandaTotal += a.getDemanda();
        }

        // penalidade por excesso
        double penalidade = 0;

        if (canhoes.size() > 5) {

            penalidade =
                    (canhoes.size() - 5) * 0.5;
        }

        // custo energético
        double custoEnergia =
                canhoes.size() * 0.2;

        return capacidadeTotal
                - custoEnergia
                - penalidade
                - Math.abs(
                demandaTotal - capacidadeTotal
        );
    }


    private double[] reconciliarDistancias(
            Canhao canhao,
            List<Alvo> alvos,
            Map<Alvo, List<LeituraSensor>> buffer
    ) {

        int n = alvos.size();

        if (n == 0) {
            return new double[0];
        }

        double[] y = new double[n];

        double[][] V = new double[n][n];

        double[][] A = new double[1][n];

        for (int i = 0; i < n; i++) {

            Alvo a = alvos.get(i);

            List<LeituraSensor> historico =
                    buffer.get(a);

            if (historico == null
                    || historico.isEmpty()) {

                continue;
            }

            double soma = 0;

            for (LeituraSensor l : historico) {

                double dx =
                        l.getX() - canhao.getX();

                double dy =
                        l.getY() - canhao.getY();

                soma += Math.sqrt(
                        dx * dx + dy * dy
                );
            }

            double media =
                    soma / historico.size();

            y[i] = media;

            // variância simples
            double variancia = 1;

            for (LeituraSensor l : historico) {

                double dx =
                        l.getX() - canhao.getX();

                double dy =
                        l.getY() - canhao.getY();

                double d =
                        Math.sqrt(dx*dx + dy*dy);

                variancia +=
                        Math.pow(d - media, 2);
            }

            variancia /= historico.size();

            V[i][i] = variancia;

            // incidência
            A[0][i] =
                    (media < 300)
                            ? 1
                            : 0;
        }

        return DataReconciliation
                .reconcile(y, V, A);
    }

    private void otimizarLado(
            List<Canhao> canhoes,
            List<Alvo> alvos,
            Map<Alvo, List<LeituraSensor>> buffer,
            Lado lado
    ) {


        if (canhoes.isEmpty() || alvos.isEmpty()) {
            return;
        }

        int numCanhoes = canhoes.size();
        int numAlvos = alvos.size();

        // Vetor de medições
        double[] y = new double[numCanhoes * numAlvos];

        // Matriz de covariância
        double[][] V = new double[y.length][y.length];

        // Matriz de incidência
        double[][] A = new double[y.length][y.length];

        int indice = 0;

        for (Canhao c : canhoes) {

            for (Alvo a : alvos) {

                List<LeituraSensor> leituras =
                        buffer.getOrDefault(a, new ArrayList<>());

                double soma = 0;

                for (LeituraSensor l : leituras) {

                    double dx = l.getX() - c.getX();
                    double dy = l.getY() - c.getY();

                    soma += Math.sqrt(dx * dx + dy * dy);
                }

                double media;

                if (leituras.isEmpty()) {

                    double dx = a.getX() - c.getX();
                    double dy = a.getY() - c.getY();

                    media = Math.sqrt(dx * dx + dy * dy);

                } else {

                    media = soma / leituras.size();
                }

                // Variância
                double variancia = 1;

                if (!leituras.isEmpty()) {

                    double somaVar = 0;

                    for (LeituraSensor l : leituras) {

                        double dx = l.getX() - c.getX();
                        double dy = l.getY() - c.getY();

                        double d = Math.sqrt(dx * dx + dy * dy);

                        somaVar += Math.pow(d - media, 2);
                    }

                    variancia = somaVar / leituras.size();

                    if (variancia < 1) {
                        variancia = 1;
                    }
                }

                y[indice] = media;

                // Matriz V diagonal
                V[indice][indice] = variancia;

                // Matriz A
                if (media < 500) {
                    A[indice][indice] = 1;
                } else {
                    A[indice][indice] = 0;
                }

                indice++;
            }
        }

        // =========================
        // RECONCILIAÇÃO
        // =========================

        double[] yHat;

        try {

            yHat = DataReconciliation.reconcile(y, V, A);

        } catch (Exception e) {

            e.printStackTrace();

            // fallback
            yHat = y;
        }

        // =========================
        // REALOCAÇÃO DOS CANHÕES
        // =========================

        indice = 0;

        for (Canhao c : canhoes) {

            double somaX = 0;
            double somaY = 0;
            int contador = 0;

            for (Alvo a : alvos) {

                double distanciaReconciliada = yHat[indice];

                if (distanciaReconciliada < 500) {

                    somaX += a.getX();
                    somaY += a.getY();

                    contador++;
                }

                indice++;
            }

            if (contador > 0) {

                int alvoX = (int)(somaX / contador);
                int alvoY = (int)(somaY / contador);

                c.definirDestino(alvoX, alvoY);
            }
        }

        // =========================
        // FUNÇÃO DE UTILIDADE
        // =========================

        double taxaDisparoMedia = 0;

        for (Canhao c : canhoes) {
            taxaDisparoMedia += c.getCapacidade();
        }

        taxaDisparoMedia /= canhoes.size();

        double utilidade =
                (numAlvos * taxaDisparoMedia)
                        - (numCanhoes * 2);

        System.out.println(
                "Lado: " + lado +
                        " | Utilidade: " + utilidade +
                        " | Canhões: " + numCanhoes +
                        " | Energia: " +
                        (lado == Lado.ESQUERDO ?
                                energiaEsquerda :
                                energiaDireita)
        );

        // =========================
        // DECISÃO GULOSA
        // =========================

        double energiaAtual =
                (lado == Lado.ESQUERDO)
                        ? energiaEsquerda
                        : energiaDireita;

        // adiciona canhão
        if (energiaAtual > 20 && numCanhoes < numAlvos) {

            adicionarCanhao(lado);

            System.out.println(
                    "NOVO CANHÃO ADICIONADO -> " + lado
            );
        }

        // remove canhão
        else if (energiaAtual < 5 && numCanhoes > 1) {

            Canhao remover =
                    canhoes.get(canhoes.size() - 1);

            remover.parar();

            canhoes.remove(remover);

            System.out.println(
                    "CANHÃO REMOVIDO -> " + lado
            );
        }
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

            public List<Canhao> getCanhoesEsquerda() {
                return canhoesEsquerda;
            }

            public List<Canhao> getCanhoesDireita() {
                return canhoesDireita;
            }

            public int getPontuacao1() {
                return pontuacao1;
            }

            public int getTempoRestante() {
                return tempoRestante;
            }

            public boolean isJogoFinalizado() {
                return jogoFinalizado;
            }

            public int getPontuacao2() {
                return pontuacao2;
            }

            public double getEnergiaEsquerda() {
                return energiaEsquerda;
            }

            public double getEnergiaDireita() {
                return energiaDireita;
            }

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
                setPriority(Thread.MAX_PRIORITY);
                while (running) {
                    long inicio = System.nanoTime();
                    if (partidaIniciada && !jogoFinalizado) {
                        atualizar();
                        atualizarTempo();
                        coletarDadosSensores();
                        atualizarMatrizIncidencia();
                        if (System.currentTimeMillis() - ultimaOtimizacao >= 10000) {

                            otimizarLado(canhoesEsquerda,alvosEsquerda,bufferEsquerda,Lado.ESQUERDO);
                            otimizarLado(canhoesDireita,alvosDireita,bufferDireita,Lado.DIREITO);
                            ultimaOtimizacao = System.currentTimeMillis();
                        }
                    }
                    long fim = System.nanoTime();
                    long tempo = (fim - inicio) / 1000000;
                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
