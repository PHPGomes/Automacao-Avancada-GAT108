package com.example.autotarget;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Jogo extends Thread {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private FirestoreRepository firestoreRepository;

    private ScheduledExecutorService telemetryScheduler;

    private PerformanceMonitor performanceMonitor;

    private boolean otimizando = false;
    private boolean jogoFinalizado = false;
    private boolean partidaIniciada = false;
    private boolean criandoOnda = false;

    private double currentTemperature = 25.0;

    private boolean isOverheating = false;

    private static final double TEMP_THRESHOLD = 40.0;

    private static final long TELEMETRY_INTERVAL_SECONDS = 10;

    private static final double ENERGY_ADD_THRESHOLD = 25.0;

    private static final double ENERGY_REMOVE_THRESHOLD = 10.0;

    public Jogo(GameView gameView) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestoreRepository = new FirestoreRepository();
        telemetryScheduler = Executors.newSingleThreadScheduledExecutor();
        this.gameView = gameView;
        performanceMonitor = new PerformanceMonitor(numAlvos);
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
        performanceMonitor = new PerformanceMonitor(numAlvos);

    }

    public void atualizar() {
        verificarColisoes();
        removerMortos();
        if (!jogoFinalizado
                && getAlvos().isEmpty()
                && !criandoOnda
                && !otimizando) {
            criarOnda();
        }
        if (gameView != null) {
            gameView.postInvalidate();
        }
    }

    public boolean temAlvos() {
        return !alvosEsquerda.isEmpty() ||
                !alvosDireita.isEmpty();
    }

    private void atualizarTempo() {
        if (System.currentTimeMillis() - ultimoSegundo < 1000) {
            return;
        }
        ultimoSegundo = System.currentTimeMillis();
        tempoRestante--;
        if (tempoRestante <= 0) {
            finalizarJogo();
            saveGameResult();
        }
    }
    private void saveGameResult() {
        if (currentUser == null) {
            return; // Não salva se não houver usuário logado
        }
        Map<String, Object> gameData = new HashMap<>();
        gameData.put("timestamp", System.currentTimeMillis());
        gameData.put("pontuacaoEsquerda", pontuacao1);
        gameData.put("pontuacaoDireita", pontuacao2);
        gameData.put("alvosAbatidos", numAlvos - 5); // Exemplo, ajustar conforme a lógica real
        List<Map<String, Object>> configuracoesCanhoes = new ArrayList<>();
        for (Canhao canhao : getCanhoes()) {
            Map<String, Object> dadosCanhao = new HashMap<>();
            dadosCanhao.put("x", canhao.getX());
            dadosCanhao.put("y", canhao.getY());
            dadosCanhao.put("lado", canhao.getLado().toString());
            dadosCanhao.put("capacidade", canhao.getCapacidade());
            configuracoesCanhoes.add(dadosCanhao);
        }
        gameData.put("configuracaoCanhoes", configuracoesCanhoes);
        // TODO: Adicionar configurações dos canhões (posição, tipo, etc.)
        // Criptografar dados sensíveis
        try {
            int score = Math.max(pontuacao1, pontuacao2);
            String nomeJogador = currentUser.getEmail();
            if (nomeJogador == null) {
                nomeJogador = "Jogador";
            }
            String encryptedPlayerName = Cryptography.encrypt(nomeJogador);
            String encryptedScore = Cryptography.encrypt(String.valueOf(score));
            gameData.put("playerName", encryptedPlayerName);
            // usado para ordenar o ranking
            gameData.put("finalScore", score);
            // usado para demonstrar criptografia
            gameData.put("finalScoreEncrypted", encryptedScore);
        } catch (Exception e) {
            Log.e("SAVE_GAME",
                    "Erro ao criptografar",
                    e);
            gameData.put("playerName", "Jogador");

            gameData.put("finalScore", Math.max(pontuacao1, pontuacao2));

            gameData.put("finalScoreEncrypted", String.valueOf(Math.max(pontuacao1, pontuacao2)));
        }


        firestoreRepository.saveGameResult(currentUser.getUid(), gameData, new FirestoreRepository.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Log ou feedback de sucesso
            }
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void simulateTemperature() {
        // Simula variação de temperatura
        currentTemperature += (random.nextDouble() * 2) - 1; // Varia entre -1 e +1
        currentTemperature =
                Math.max(20,
                        Math.min(50,currentTemperature));
        if (currentTemperature > 50) currentTemperature = 50;
    }

    private void saveTelemetryData() {
        Map<String, Object> telemetry = new HashMap<>();
        telemetry.put("timestamp", System.currentTimeMillis());
        telemetry.put("temperature", currentTemperature);
        telemetry.put("isOverheating", isOverheating);

        firestoreRepository.saveTelemetryData(currentUser.getUid(), telemetry, new FirestoreRepository.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Log ou feedback de sucesso
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void applyTemperatureFeedback() {
        isOverheating =  currentTemperature > TEMP_THRESHOLD;
    }
    public boolean isOverheating() {
        return isOverheating;
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
        verificarColisoes(alvosEsquerda);
        verificarColisoes(alvosDireita);
    }

    private void verificarColisoes(List<Alvo> alvos) {

        for (Alvo alvo : alvos) {

            if (alvo == null || !alvo.getRunning()) {
                continue;
            }

            for (Bala bala : balas) {

                if (bala == null || !bala.getRunning()) {
                    continue;
                }

                synchronized (alvo) {

                    if (!alvo.getRunning() || !bala.getRunning()) {
                        continue;
                    }

                    double distancia = calcularDistancia(
                            bala.getX(),
                            bala.getY(),
                            alvo.getX(),
                            alvo.getY()
                    );

                    if (distancia < alvo.getRaio()) {

                        alvo.parar();
                        bala.parar();

                        registrarPontuacao(bala);
                    }
                }
            }
        }
    }

    private double calcularDistancia(double x1, double y1, double x2, double y2) {

        double dx = x1 - x2;
        double dy = y1 - y2;

        return Math.sqrt(dx * dx + dy * dy);
    }

    private void registrarPontuacao(Bala bala) {

        Canhao canhaoAtirador = bala.getCanhaoAtirador();

        if (canhaoAtirador == null) {
            return;
        }

        if (canhaoAtirador.getLado() == Lado.ESQUERDO) {
            pontuacao1++;
        } else {
            pontuacao2++;
        }
    }

    private void removerMortos() {
        try {

            limparAlvosMortos();
            limparBalasMortas();
            limparCanhoesMortos();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            Log.e("JOGO",
                    "Thread interrompida ao remover objetos mortos",
                    e);
        }
    }

    private void limparAlvosMortos() throws InterruptedException {

        semaforoAlvos.acquire();

        try {

            alvosEsquerda.removeIf(this::objetoThreadMorto);
            alvosDireita.removeIf(this::objetoThreadMorto);

        } finally {

            semaforoAlvos.release();
        }
    }

    private void limparBalasMortas() throws InterruptedException {

        semaforoBalas.acquire();

        try {

            balas.removeIf(this::objetoThreadMorto);

        } finally {

            semaforoBalas.release();
        }
    }

    private void limparCanhoesMortos() throws InterruptedException {

        semaforoCanhoes.acquire();

        try {

            canhoesEsquerda.removeIf(this::objetoThreadMorto);
            canhoesDireita.removeIf(this::objetoThreadMorto);

        } finally {

            semaforoCanhoes.release();
        }
    }

    private boolean objetoThreadMorto(Thread objeto) {

        if (objeto == null) {
            return true;
        }

        boolean parado = false;

        if (objeto instanceof Alvo) {
            parado = !((Alvo) objeto).getRunning();
        } else if (objeto instanceof Bala) {
            parado = !((Bala) objeto).getRunning();
        } else if (objeto instanceof Canhao) {
            parado = !((Canhao) objeto).getRunning();
        }

        return parado && !objeto.isAlive();
    }

    public void criarBala(int x, int y, int alvoX, int alvoY, Canhao canhaoAtirador) {

        try {

            semaforoBalas.acquire();

            try {

                Bala bala = new Bala(
                        x,
                        y,
                        alvoX,
                        alvoY,
                        gameView,
                        canhaoAtirador
                );

                balas.add(bala);
                bala.start();

            } finally {

                semaforoBalas.release();
            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            Log.e("JOGO",
                    "Erro ao criar bala",
                    e);
        }
    }

    public void criarOnda() {

        if (criandoOnda) {
            return;
        }

        if (gameView == null || gameView.getWidth() == 0 || gameView.getHeight() == 0) {
            return;
        }

        criandoOnda = true;

        try {

            semaforoAlvos.acquire();

            try {

                for (int i = 0; i < numAlvos; i++) {

                    Alvo alvo = criarAlvoAleatorio();

                    if (alvo.getLado() == Lado.ESQUERDO) {
                        alvosEsquerda.add(alvo);
                    } else {
                        alvosDireita.add(alvo);
                    }

                    alvo.start();
                }

                Log.d("ONDA",
                        "Nova onda criada | Alvos=" + numAlvos +
                                " | Threads=" + Thread.activeCount());

            } finally {

                semaforoAlvos.release();
            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            Log.e("ONDA",
                    "Erro ao criar onda",
                    e);

        } finally {

            criandoOnda = false;
        }
    }

    private Alvo criarAlvoAleatorio() {

        int largura = gameView.getWidth();
        int altura = gameView.getHeight();

        int x = largura / 2;
        int y = altura / 2;

        if (random.nextInt(100) < 70) {

            return new AlvoComum(
                    x,
                    y,
                    largura,
                    altura,
                    gameView,
                    this
            );
        }

        return new AlvoRapido(
                x,
                y,
                largura,
                altura,
                gameView,
                this
        );
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

        performanceMonitor.iniciar();

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
            try {
                Canhao novoCanhao = new Canhao(x, y, gameView, this, lado);
                if (novoCanhao.getLado() == Lado.ESQUERDO) {
                    canhoesEsquerda.add(novoCanhao);
                } else {
                    canhoesDireita.add(novoCanhao);
                }
                novoCanhao.start();
            } finally {
                semaforoCanhoes.release();
            }

        } catch (JogoException e) {
            e.printStackTrace(); // Logar a exceção para depuração
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

        if (canhoes == null || alvos == null || buffer == null) {
            return;
        }

        if (canhoes.isEmpty() || alvos.isEmpty()) {
            return;
        }

        int numCanhoes = canhoes.size();
        int numAlvos = alvos.size();

        double[] y = new double[numCanhoes * numAlvos];
        double[][] V = new double[y.length][y.length];
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
                V[indice][indice] = variancia;

                if (media < 500) {
                    A[indice][indice] = 1;
                } else {
                    A[indice][indice] = 0;
                }

                indice++;
            }
        }

        double[] yHat;

        try {

            yHat = DataReconciliation.reconcile(y, V, A);

            if (yHat == null || yHat.length != y.length) {
                yHat = y;
            }

        } catch (Exception e) {

            e.printStackTrace();
            yHat = y;
        }

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

                int alvoX = (int) (somaX / contador);
                int alvoY = (int) (somaY / contador);

                c.definirDestino(alvoX, alvoY);
            }
        }

        double taxaDisparoMedia = 0;

        for (Canhao c : canhoes) {
            taxaDisparoMedia += c.getCapacidade();
        }

        taxaDisparoMedia /= canhoes.size();

        double utilidade =
                (numAlvos * taxaDisparoMedia)
                        - (numCanhoes * 2);

        Log.d("Jogo",
                "Lado: " + lado +
                        " | Utilidade: " + utilidade +
                        " | Canhões: " + numCanhoes +
                        " | Energia: " +
                        (lado == Lado.ESQUERDO ? energiaEsquerda : energiaDireita)
        );

        double energiaAtual =
                (lado == Lado.ESQUERDO)
                        ? energiaEsquerda
                        : energiaDireita;

        if (energiaAtual > ENERGY_ADD_THRESHOLD && numCanhoes < numAlvos) {

            adicionarCanhao(lado);

            Log.d("Jogo",
                    "NOVO CANHÃO ADICIONADO -> " + lado
            );

        } else if (energiaAtual < ENERGY_REMOVE_THRESHOLD && numCanhoes > 1) {

            Canhao remover = canhoes.get(canhoes.size() - 1);

            remover.parar();

            canhoes.remove(remover);

            Log.d("Jogo",
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

        performanceMonitor.finalizar();

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

        Executors.newSingleThreadExecutor().execute(() -> {

            Partida p = new Partida();

            p.usuario = currentUser.getEmail();

            p.pontosEsquerda = pontuacao1;

            p.pontosDireita = pontuacao2;

            p.canhoesEsquerda = canhoesEsquerda.size();

            p.canhoesDireita = canhoesDireita.size();

            p.tempo = tempoInicial - tempoRestante;

            p.data = System.currentTimeMillis();

            DatabaseProvider
                    .get(gameView.getContext())
                    .partidaDao()
                    .inserir(p);

        });


    }

    public List<Canhao> getCanhoes(){

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

    public void setCurrentUser(FirebaseUser user) {
        this.currentUser = user;
    }

    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    public FirestoreRepository getFirestoreRepository() {
        return firestoreRepository;
    }

    public FirebaseAuth getFirebaseAuth() {
        return mAuth;
    }

    public double getCurrentTemperature() {
        return currentTemperature;
    }

    @Override
    public void run() {
        Log.d("JOGO", "Thread do jogo iniciada. id=" + Thread.currentThread().getId());
        setPriority(Thread.NORM_PRIORITY);
        Log.d("TELEMETRIA", "Scheduler iniciado");

        telemetryScheduler.scheduleAtFixedRate(() -> {
            currentUser = mAuth.getCurrentUser();
            if (currentUser != null && partidaIniciada) {
                simulateTemperature();
                saveTelemetryData();
                applyTemperatureFeedback();
            }
        }, TELEMETRY_INTERVAL_SECONDS, TELEMETRY_INTERVAL_SECONDS, TimeUnit.SECONDS);



        // delay inicial igual ao intervalo, evita o 0 depreciado
        setPriority(Thread.MAX_PRIORITY);

        while (running) {
            long inicio = System.nanoTime();
            Log.d("THREADS", "Ativas = " + Thread.activeCount());
            //Log.d("LOOP_JOGO", "rodando");
            if (partidaIniciada && !jogoFinalizado) {
                atualizar();
                atualizarTempo();
                coletarDadosSensores();
                atualizarMatrizIncidencia();
                // Atualizar o currentUser caso o login tenha sido feito após a inicialização do Jogo
                currentUser = mAuth.getCurrentUser();


                if (System.currentTimeMillis() - ultimaOtimizacao >= 10000) {
                    otimizando=true;
                    otimizarLado(canhoesEsquerda,alvosEsquerda,bufferEsquerda,Lado.ESQUERDO);
                    otimizarLado(canhoesDireita,alvosDireita,bufferDireita,Lado.DIREITO);
                    ultimaOtimizacao = System.currentTimeMillis();
                    otimizando=false;
                }


            }
            else {
                // ADICIONE ISSO AQUI: Mantém a tela ativa/visível antes do jogo começar
                //if (gameView != null) {
                    //new Handler(Looper.getMainLooper()).post(() -> gameView.invalidate());
                //}
            }
            long fim = System.nanoTime();
            performanceMonitor.registrarFrame((fim - inicio)/1000000);
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Log.e("JOGO", "ERRO NO LOOP", e);
            }
        }
        // Ao parar o jogo, desligar o scheduler de telemetria
        telemetryScheduler.shutdownNow();
    }



}
