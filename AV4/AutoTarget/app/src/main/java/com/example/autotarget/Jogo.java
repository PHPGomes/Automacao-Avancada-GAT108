package com.example.autotarget;

import android.graphics.Paint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.concurrent.CopyOnWriteArrayList;

public class Jogo extends Thread {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirestoreRepository firestoreRepository;
    private ScheduledExecutorService telemetryScheduler;
    private volatile boolean desenhando = false;

    private PerformanceMonitor performanceMonitor;

    private final List<Canhao> canhoesDesenho = new ArrayList<>();
    private final List<Alvo> alvosDesenho = new ArrayList<>();
    private final List<Bala> balasDesenho = new ArrayList<>();

    private boolean otimizando = false;

    private Handler handler = new Handler(Looper.getMainLooper());

    private double currentTemperature = 25.0; // Temperatura inicial simulada
    private static final double TEMP_THRESHOLD = 40.0;
    private static final long TELEMETRY_INTERVAL_SECONDS = 10;
    private boolean isOverheating = false;

    private static final double ENERGY_ADD_THRESHOLD = 25.0; // Limiar para adicionar canhão
    private static final double ENERGY_REMOVE_THRESHOLD = 10.0; // Limiar para remover canhão

    private List<Canhao> canhoesEsquerda =
            new CopyOnWriteArrayList<>();

    private List<Canhao> canhoesDireita =
            new CopyOnWriteArrayList<>();

    private List<Alvo> alvosEsquerda =
            new CopyOnWriteArrayList<>();
    private List<Alvo> alvosDireita =
            new CopyOnWriteArrayList<>();

    private List<Bala> balas =
            new CopyOnWriteArrayList<>();

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
    private boolean criandoOnda = false;


    private Semaphore semaforoAlvos = new Semaphore(1);
    private Semaphore semaforoBalas = new Semaphore(1);
    private Semaphore semaforoCanhoes = new Semaphore(1);

    public Jogo(GameView gameView) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestoreRepository = new FirestoreRepository();
        telemetryScheduler = Executors.newSingleThreadScheduledExecutor();
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
        performanceMonitor = new PerformanceMonitor();

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

        if (System.currentTimeMillis() - ultimoSegundo >= 1000) {

            tempoRestante--;

            ultimoSegundo = System.currentTimeMillis();

            if (tempoRestante <= 0) {

                finalizarJogo();
                saveGameResult(); // Salvar resultado da partida ao finalizar
            }
        }
    }

    public void liberarDesenho(){
        desenhando = false;
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

            e.printStackTrace();

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
        if (currentTemperature < 20) currentTemperature = 20;
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
        if (currentTemperature > TEMP_THRESHOLD) {
            isOverheating = true;
            // Aumentar o delay de tiro dos canhões (implementado na classe Canhao)
        } else {
            isOverheating = false;
        }
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
            try {
                alvosEsquerda.removeIf(a -> {
                    if(!a.getRunning() && !a.isAlive()) {
                        a.parar();   // IMPORTANTE
                        return true;
                    }
                    return false;
                });
                alvosDireita.removeIf(a -> {
                    if(!a.getRunning() && !a.isAlive()) {
                        a.parar();
                        return true;
                    }
                    return false;
                });
            } finally {
                semaforoAlvos.release();
            }
            semaforoBalas.acquire();
            try {
                balas.removeIf(p -> {
                    if(!p.getRunning() && !p.isAlive()) {
                        p.parar();
                        return true;
                    }
                    return false;
                });
            } finally {
                semaforoBalas.release();
            }
            semaforoCanhoes.acquire();
            try {
                canhoesEsquerda.removeIf(c -> {
                    if(!c.getRunning() && !c.isAlive()) {
                        c.parar();
                        return true;
                    }
                    return false;
                });
                canhoesDireita.removeIf(c -> {
                    if(!c.getRunning() && !c.isAlive()) {
                        c.parar();
                        return true;
                    }
                    return false;
                });
            } finally {
                semaforoCanhoes.release();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void criarBala(int x, int y, int alvoX, int alvoY, Canhao canhaoAtirador) {
        try {
            semaforoBalas.acquire();
            try {
                Bala b = new Bala(x, y, alvoX, alvoY, gameView, canhaoAtirador);
                balas.add(b);
                b.start();
            } finally {
                semaforoBalas.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void criarOnda() {
        Log.d("ONDA",
                "criando onda. threads=" + Thread.activeCount()
                        + " alvos E=" + alvosEsquerda.size()
                        + " alvos D=" + alvosDireita.size()
                        + " balas=" + balas.size()
                        + " canhoes E=" + canhoesEsquerda.size()
                        + " canhoes D=" + canhoesDireita.size());
        if (criandoOnda) {
            return;
        }
        //Log.d("ONDA", "COMECOU CRIAR ONDA");
        criandoOnda = true;
        Log.d("ONDA","INICIO criação");
        try {
            semaforoAlvos.acquire();
            try {
                // Garante que o GameView tenha dimensões válidas antes de criar alvos
                if (gameView.getWidth() == 0 || gameView.getHeight() == 0) {
                    criandoOnda = false;
                    return;
                }
                for (int c = 0; c < numAlvos; c++) {
                   // Log.d("ONDA", "CRIANDO ALVO " + c);
                    Alvo a;
                    if (random.nextInt(100) < 70) {
                        a = new AlvoComum(gameView.getWidth() / 2, gameView.getHeight() / 2, gameView.getWidth(), gameView.getHeight(), gameView, this);
                    } else {
                        a = new AlvoRapido(gameView.getWidth() / 2, gameView.getHeight() / 2, gameView.getWidth(), gameView.getHeight(), gameView, this);
                    }
                   // Log.d("ONDA", "ALVO CRIADO");
                    if (a.getLado() == Lado.ESQUERDO) {
                        alvosEsquerda.add(a);
                    } else {
                        alvosDireita.add(a);
                    }
                    a.start();
                    Log.d("THREAD", "alvo criado id="+a.getId());
                   // Log.d("ONDA", "DEPOIS START");
                }
                //numAlvos += 5;
            } finally {
                criandoOnda = false;
                semaforoAlvos.release();
                Log.d("ONDA","FIM criação");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            //Log.e("ONDA", "ERRO NA ONDA", e);
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
        if(canhoes.size() * alvos.size() == 0){
            return;
        }


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
            if(yHat.length != y.length) {
                yHat = y;
            }

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

                double distanciaReconciliada = 0;

                if(indice < yHat.length){
                    distanciaReconciliada = yHat[indice];
                }

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

        // adiciona canhão com histerese
        if (energiaAtual > ENERGY_ADD_THRESHOLD && numCanhoes < numAlvos) {

            adicionarCanhao(lado);

            System.out.println(
                    "NOVO CANHÃO ADICIONADO -> " + lado
            );
        }

        // remove canhão com histerese
        else if (energiaAtual < ENERGY_REMOVE_THRESHOLD && numCanhoes > 1) {

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
