package com.example.autotarget;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // usa o layout XML
        setContentView(R.layout.activity_main);

        // pega os elementos da tela
        gameView = findViewById(R.id.gameView);
        Button btnIniciar = findViewById(R.id.btnIniciar);
        Button btnAdicionar = findViewById(R.id.btnAdicionar);

        // botão iniciar (opcional)
        btnIniciar.setOnClickListener(v -> {
            // você pode usar pra resetar o jogo depois
        });

        // botão adicionar canhão 🔥
        btnAdicionar.setOnClickListener(v -> {
            gameView.adicionarCanhao();
        });
    }
}