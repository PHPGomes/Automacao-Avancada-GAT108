package com.example.autotarget;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // usa o XML com os botões
        setContentView(R.layout.activity_main);

        // agora sim pode pegar os botões
        Button btnIniciar = findViewById(R.id.btnIniciar);
        Button btnAdicionar = findViewById(R.id.btnAdicionar);

        // cria o GameView (mas ainda não mostra)
        gameView = new GameView(this);

        // botão iniciar → troca pra tela do jogo
        btnIniciar.setOnClickListener(v -> {
            setContentView(gameView);
        });

        // botão adicionar → adiciona canhão
        btnAdicionar.setOnClickListener(v -> {
            gameView.adicionarCanhao();
        });
    }
}