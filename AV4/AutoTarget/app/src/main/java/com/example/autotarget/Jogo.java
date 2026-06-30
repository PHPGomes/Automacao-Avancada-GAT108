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

    private static final String TAG = "JOGO";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirestoreRepository firestoreRepository;
    private ScheduledExecutorService telemetryScheduler;
    private PerformanceMonitor performanceMonitor;

    private GameView gameView;
    private Random random = new Random();

    private List<Canhao> canhoesEsquerda = new CopyOnWriteArrayList<>();
    private List<Canhao> canhoesDireita = new CopyOnWriteArrayList<>();

    private List<Alvo> alvosEsquerda = new CopyOnWriteArrayList<>();
    private List<Alvo> alvosDireita = new CopyOnWriteArrayList<>();

    private List<Bala> balas = new CopyOnWriteArrayList<>();

    private Map<Alvo, List<LeituraSensor>> bufferEsquerda = new HashMap<>();
    private Map<Alvo, List<LeituraSensor>> bufferDireita = new HashMap<>();

    private Semaphore semaforoAlvos = new Semaphore(1);
    private Semaphore semaforoBalas = new Semaphore(1);
    private Semaphore semaforoCanhoes = new Semaphore(1);

    private int pontuacao1 = 0;
    private int pontuacao2 = 0;

    private int numAlvos = 5;

    private int iniEnergiaEsquerda = 100;
    private int iniEnergiaDireita = 100;
    private int energiaEsquerda = iniEnergiaEsquerda;
    private int energiaDireita = iniEnergiaDireita;

    private int tempoInicial = 60;
    private int tempoRestante = tempoInicial;

    private long ultimoSegundo = 0;
    private long ultimaColeta = 0;
    private long ultimaOtimizacao = 0;

    private double[][] matrizIncidencia;

    private boolean running = true;
    private boolean jogoFinalizado = false;
    private boolean partidaIniciada = false;
    private boolean criandoOnda = false;
    private boolean otimizando = false;

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

            if (alvo == null || !alvo.getRunning()) {
                continue;
            }

            LeituraSensor leitura = alvo.gerarLeitura();

            buffer.putIfAbsent(alvo, new ArrayList<>());

            List<LeituraSensor> historico = buffer.get(alvo);

            if (historico == null) {
                continue;
            }

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

        ultimaColeta = 0;
        ultimaOtimizacao = 0;
        ultimoSegundo = System.currentTimeMillis();

        bufferEsquerda.clear();
        bufferDireita.clear();

        if (performanceMonitor != null) {
            performanceMonitor.setQuantidadeAlvos(numAlvos);
            performanceMonitor.iniciar();
        }

        criarOnda();
    }

    private void pararObjetosAtivos() {

        for (Alvo alvo : getAlvos()) {
            alvo.parar();
        }

        for (Canhao canhao : getCanhoes()) {
            canhao.parar();
        }

        for (Bala bala : balas) {
            bala.parar();
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

        matrizIncidencia = new double[alvos.size()][canhoes.size()];

        for (int i = 0; i < alvos.size(); i++) {

            Alvo alvo = alvos.get(i);

            for (int j = 0; j < canhoes.size(); j++) {

                Canhao canhao = canhoes.get(j);

                double distancia = calcularDistancia(
                        alvo.getX(),
                        alvo.getY(),
                        canhao.getX(),
                        canhao.getY()
                );

                matrizIncidencia[i][j] = 1.0 / (distancia + 1);
            }
        }
    }

    public void adicionarCanhao(Lado lado) {

        if (gameView == null || gameView.getWidth() == 0 || gameView.getHeight() == 0) {
            return;
        }

        if (lado == Lado.ESQUERDO && energiaEsquerda <= 0) {
            return;
        }

        if (lado == Lado.DIREITO && energiaDireita <= 0) {
            return;
        }

        try {

            int metade = gameView.getWidth() / 2;
            int margem = 80;

            int larguraDisponivel = metade - margem - 80;

            if (larguraDisponivel <= 0 || gameView.getHeight() <= 200) {
                return;
            }

            int x;

            if (lado == Lado.ESQUERDO) {
                x = margem + random.nextInt(larguraDisponivel);
            } else {
                x = metade + 80 + random.nextInt(larguraDisponivel);
            }

            int y = 100 + random.nextInt(gameView.getHeight() - 200);

            if (x < 0 || x > gameView.getWidth() || y < 0 || y > gameView.getHeight()) {
                throw new JogoException("Tentativa de adicionar canhão fora dos limites da tela.");
            }

            semaforoCanhoes.acquire();

            try {

                Canhao novoCanhao = new Canhao(
                        x,
                        y,
                        gameView,
                        this,
                        lado
                );

                if (lado == Lado.ESQUERDO) {
                    canhoesEsquerda.add(novoCanhao);
                } else {
                    canhoesDireita.add(novoCanhao);
                }

                novoCanhao.start();

            } finally {

                semaforoCanhoes.release();
            }

        } catch (JogoException e) {

            Log.e("JOGO", "Erro ao adicionar canhão", e);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            Log.e("JOGO", "Thread interrompida ao adicionar canhão", e);
        }
    }

    public List<Alvo> getAlvosPorLado(Lado lado) {

        if (lado == Lado.ESQUERDO) {
            return alvosEsquerda;
        }

        return alvosDireita;
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

        preencherMatrizesReconcilicao(canhoes, alvos, buffer, y, V, A);

        double[] yHat = aplicarReconcilicao(y, V, A);

        reposicionarCanhoes(canhoes, alvos, yHat);

        avaliarCustoBeneficio(canhoes, alvos, lado);
    }

    private void preencherMatrizesReconcilicao(
            List<Canhao> canhoes,
            List<Alvo> alvos,
            Map<Alvo, List<LeituraSensor>> buffer,
            double[] y,
            double[][] V,
            double[][] A
    ) {

        int indice = 0;

        for (Canhao canhao : canhoes) {

            for (Alvo alvo : alvos) {

                List<LeituraSensor> leituras =
                        buffer.getOrDefault(alvo, new ArrayList<>());

                double media = calcularDistanciaMedia(canhao, alvo, leituras);
                double variancia = calcularVarianciaDistancia(canhao, leituras, media);

                y[indice] = media;
                V[indice][indice] = variancia;
                A[indice][indice] = media < 500 ? 1 : 0;

                indice++;
            }
        }
    }

    private double calcularDistanciaMedia(
            Canhao canhao,
            Alvo alvo,
            List<LeituraSensor> leituras
    ) {

        if (leituras == null || leituras.isEmpty()) {
            return calcularDistancia(
                    alvo.getX(),
                    alvo.getY(),
                    canhao.getX(),
                    canhao.getY()
            );
        }

        double soma = 0;

        for (LeituraSensor leitura : leituras) {

            soma += calcularDistancia(
                    leitura.getX(),
                    leitura.getY(),
                    canhao.getX(),
                    canhao.getY()
            );
        }

        return soma / leituras.size();
    }

    private double calcularVarianciaDistancia(
            Canhao canhao,
            List<LeituraSensor> leituras,
            double media
    ) {

        if (leituras == null || leituras.isEmpty()) {
            return 1;
        }

        double somaVariancia = 0;

        for (LeituraSensor leitura : leituras) {

            double distancia = calcularDistancia(
                    leitura.getX(),
                    leitura.getY(),
                    canhao.getX(),
                    canhao.getY()
            );

            somaVariancia += Math.pow(distancia - media, 2);
        }

        double variancia = somaVariancia / leituras.size();

        return Math.max(variancia, 1);
    }

    private double[] aplicarReconcilicao(
            double[] y,
            double[][] V,
            double[][] A
    ) {

        try {

            double[] yHat = DataReconciliation.reconcile(y, V, A);

            if (yHat == null || yHat.length != y.length) {
                return y;
            }

            return yHat;

        } catch (Exception e) {

            Log.e("RECON",
                    "Falha na reconciliação; usando dados medidos",
                    e);

            return y;
        }
    }

    private void reposicionarCanhoes(
            List<Canhao> canhoes,
            List<Alvo> alvos,
            double[] yHat
    ) {

        int indice = 0;

        for (Canhao canhao : canhoes) {

            double somaX = 0;
            double somaY = 0;
            int contador = 0;

            for (Alvo alvo : alvos) {

                if (indice < yHat.length && yHat[indice] < 500) {

                    somaX += alvo.getX();
                    somaY += alvo.getY();

                    contador++;
                }

                indice++;
            }

            if (contador > 0) {

                int destinoX = (int) (somaX / contador);
                int destinoY = (int) (somaY / contador);

                canhao.definirDestino(destinoX, destinoY);
            }
        }
    }

    private void avaliarCustoBeneficio(
            List<Canhao> canhoes,
            List<Alvo> alvos,
            Lado lado
    ) {

        int numCanhoes = canhoes.size();
        int numAlvos = alvos.size();

        if (numCanhoes == 0) {
            return;
        }

        double taxaDisparoMedia = 0;

        for (Canhao canhao : canhoes) {
            taxaDisparoMedia += canhao.getCapacidade();
        }

        taxaDisparoMedia /= numCanhoes;

        double utilidade =
                (numAlvos * taxaDisparoMedia)
                        - (numCanhoes * 2);

        double energiaAtual =
                lado == Lado.ESQUERDO
                        ? energiaEsquerda
                        : energiaDireita;

        Log.d("OTIMIZACAO",
                "Lado=" + lado +
                        " | Utilidade=" + utilidade +
                        " | Canhões=" + numCanhoes +
                        " | Alvos=" + numAlvos +
                        " | Energia=" + energiaAtual);

        if (energiaAtual > ENERGY_ADD_THRESHOLD && numCanhoes < numAlvos) {

            adicionarCanhao(lado);

            Log.d("OTIMIZACAO",
                    "Canhão adicionado -> " + lado);

        } else if (energiaAtual < ENERGY_REMOVE_THRESHOLD && numCanhoes > 1) {

            Canhao remover = canhoes.get(canhoes.size() - 1);

            remover.parar();
            canhoes.remove(remover);

            Log.d("OTIMIZACAO",
                    "Canhão removido -> " + lado);
        }
    }

    public synchronized void transferirAlvo(Alvo alvo, Lado novoLado) {

        if (alvo == null) {
            return;
        }

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

        if (performanceMonitor != null) {
            performanceMonitor.finalizar();
        }

        for (Alvo alvo : getAlvos()) {
            alvo.parar();
        }

        for (Canhao canhao : getCanhoes()) {
            canhao.parar();
        }

        for (Bala bala : balas) {
            bala.parar();
        }

        salvarPartidaLocal();
    }

    private void salvarPartidaLocal() {

        if (gameView == null) {
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {

            try {

                Partida partida = new Partida();

                if (currentUser != null && currentUser.getEmail() != null) {
                    partida.usuario = currentUser.getEmail();
                } else {
                    partida.usuario = "Jogador";
                }

                partida.pontosEsquerda = pontuacao1;
                partida.pontosDireita = pontuacao2;
                partida.canhoesEsquerda = canhoesEsquerda.size();
                partida.canhoesDireita = canhoesDireita.size();
                partida.tempo = tempoInicial - tempoRestante;
                partida.data = System.currentTimeMillis();

                DatabaseProvider
                        .get(gameView.getContext())
                        .partidaDao()
                        .inserir(partida);

            } catch (Exception e) {

                Log.e("ROOM",
                        "Erro ao salvar partida local",
                        e);
            }
        });
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

    public int getPontuacao2() {
        return pontuacao2;
    }

    public int getTempoRestante() {
        return tempoRestante;
    }

    public boolean isJogoFinalizado() {
        return jogoFinalizado;
    }

    public double getEnergiaEsquerda() {
        return energiaEsquerda;
    }

    public double getEnergiaDireita() {
        return energiaDireita;
    }

    public void parar() {

        running = false;

        for (Alvo alvo : alvosEsquerda) {
            alvo.parar();
        }

        for (Alvo alvo : alvosDireita) {
            alvo.parar();
        }

        for (Canhao canhao : canhoesEsquerda) {
            canhao.parar();
        }

        for (Canhao canhao : canhoesDireita) {
            canhao.parar();
        }

        for (Bala bala : balas) {
            bala.parar();
        }

        if (telemetryScheduler != null) {
            telemetryScheduler.shutdownNow();
        }
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

        Log.d("JOGO",
                "Thread do jogo iniciada. id=" + Thread.currentThread().getId());

        setPriority(Thread.MAX_PRIORITY);

        iniciarTelemetria();

        while (running) {

            long inicio = System.nanoTime();

            try {

                processarCicloJogo();

            } catch (Exception e) {

                Log.e("JOGO",
                        "Erro no ciclo principal",
                        e);
            }

            dormirLoop();

            long fim = System.nanoTime();

            if (performanceMonitor != null) {
                performanceMonitor.registrarFrame((fim - inicio) / 1000000);
            }

        }

        if (telemetryScheduler != null) {
            telemetryScheduler.shutdownNow();
        }
    }

    private void iniciarTelemetria() {

        if (telemetryScheduler == null || telemetryScheduler.isShutdown()) {
            telemetryScheduler = Executors.newSingleThreadScheduledExecutor();
        }

        telemetryScheduler.scheduleWithFixedDelay(() -> {

                    try {

                        currentUser = mAuth.getCurrentUser();

                        if (currentUser != null && partidaIniciada) {

                            simulateTemperature();
                            saveTelemetryData();
                            applyTemperatureFeedback();
                        }

                    } catch (Exception e) {

                        Log.e("TELEMETRIA",
                                "Erro na telemetria",
                                e);
                    }

                }, TELEMETRY_INTERVAL_SECONDS,
                TELEMETRY_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
    }

    private void processarCicloJogo() {

        if (!partidaIniciada || jogoFinalizado) {
            return;
        }

        atualizar();

        atualizarTempo();

        coletarDadosSensores();

        atualizarMatrizIncidencia();

        currentUser = mAuth.getCurrentUser();

        executarOtimizacaoPeriodica();
    }

    private void executarOtimizacaoPeriodica() {

        if (System.currentTimeMillis() - ultimaOtimizacao < 10000) {
            return;
        }

        otimizando = true;

        try {

            otimizarLado(
                    canhoesEsquerda,
                    alvosEsquerda,
                    bufferEsquerda,
                    Lado.ESQUERDO
            );

            otimizarLado(
                    canhoesDireita,
                    alvosDireita,
                    bufferDireita,
                    Lado.DIREITO
            );

            ultimaOtimizacao = System.currentTimeMillis();

        } finally {

            otimizando = false;
        }
    }

    private void dormirLoop() {

        try {

            Thread.sleep(16);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            Log.e("JOGO",
                    "Loop interrompido",
                    e);
        }
    }
}
