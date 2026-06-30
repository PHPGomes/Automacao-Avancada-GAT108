package com.example.autotarget;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private GameView gameView;

    private Button btnIniciar;
    private Button btnAdicionarEsquerda;
    private Button btnAdicionarDireita;
    private Button btnLogin;
    private Button btnRanking;
    private Button btnHistorico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            abrirLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        inicializarViews();
        configurarBotoes();
        updateUI(currentUser);
    }

    @Override
    protected void onResume() {
        super.onResume();

        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            abrirLogin();
        } else {
            updateUI(currentUser);
        }
    }

    private void inicializarViews() {
        gameView = findViewById(R.id.gameView);

        btnIniciar = findViewById(R.id.btnIniciar);
        btnAdicionarEsquerda = findViewById(R.id.btnAdicionarEsquerda);
        btnAdicionarDireita = findViewById(R.id.btnAdicionarDireita);
        btnLogin = findViewById(R.id.btnLogin);
        btnRanking = findViewById(R.id.btnRanking);
        btnHistorico = findViewById(R.id.btnHistorico);
    }

    private void configurarBotoes() {

        btnIniciar.setOnClickListener(v -> iniciarJogo());

        btnAdicionarEsquerda.setOnClickListener(v ->
                adicionarCanhao(Lado.ESQUERDO));

        btnAdicionarDireita.setOnClickListener(v ->
                adicionarCanhao(Lado.DIREITO));

        btnLogin.setOnClickListener(v -> abrirLogin());

        btnRanking.setOnClickListener(v ->
                startActivity(new Intent(this, RankingActivity.class)));

        btnHistorico.setOnClickListener(v ->
                startActivity(new Intent(this, HistoricoActivity.class)));
    }

    private void iniciarJogo() {

        if (!usuarioLogado()) {
            mostrarMensagemLogin();
            return;
        }

        if (gameView != null) {
            gameView.iniciarJogo();
        }
    }

    private void adicionarCanhao(Lado lado) {

        if (!usuarioLogado()) {
            mostrarMensagemLogin();
            return;
        }

        if (gameView != null) {
            gameView.adicionarCanhao(lado);
        }
    }

    private boolean usuarioLogado() {
        currentUser = mAuth.getCurrentUser();
        return currentUser != null;
    }

    private void mostrarMensagemLogin() {
        Toast.makeText(
                this,
                "Faça login para usar o jogo!",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void abrirLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateUI(FirebaseUser user) {

        currentUser = user;

        if (gameView != null && gameView.getJogo() != null) {
            gameView.getJogo().setCurrentUser(user);
        }
    }
}