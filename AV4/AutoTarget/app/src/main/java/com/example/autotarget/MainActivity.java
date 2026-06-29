package com.example.autotarget;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.content.Intent;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inicialização explícita do Firebase (Crucial para evitar tela preta na AV3)
        try {
            FirebaseApp.initializeApp(this);
        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao inicializar Firebase: " + e.getMessage());
        }

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        gameView = findViewById(R.id.gameView);

        Button btnIniciar = findViewById(R.id.btnIniciar);
        Button btnAdicionarEsquerda = findViewById(R.id.btnAdicionarEsquerda);
        Button btnAdicionarDireita = findViewById(R.id.btnAdicionarDireita);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRanking = findViewById(R.id.btnRanking);

        btnIniciar.setOnClickListener(v -> {
            if (currentUser != null) {
                if (gameView != null) gameView.iniciarJogo();
            } else {
                Toast.makeText(MainActivity.this, "Faça login para iniciar o jogo!", Toast.LENGTH_SHORT).show();
            }
        });

        btnAdicionarEsquerda.setOnClickListener(v -> {
            if (currentUser != null) {
                if (gameView != null) gameView.adicionarCanhao(Lado.ESQUERDO);
            } else {
                Toast.makeText(MainActivity.this, "Faça login para adicionar canhões!", Toast.LENGTH_SHORT).show();
            }
        });

        btnAdicionarDireita.setOnClickListener(v -> {
            if (currentUser != null) {
                if (gameView != null) gameView.adicionarCanhao(Lado.DIREITO);
            } else {
                Toast.makeText(MainActivity.this, "Faça login para adicionar canhões!", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnRanking.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RankingActivity.class);
            startActivity(intent);
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Sem usuário logado, vai para login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        currentUser = user;
        if (gameView != null && gameView.getJogo() != null) {
            gameView.getJogo().setCurrentUser(currentUser);
        }
    }
}
