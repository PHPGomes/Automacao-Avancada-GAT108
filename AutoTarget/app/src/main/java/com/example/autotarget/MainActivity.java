package com.example.autotarget;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.gameView);
        Button btnIniciar = findViewById(R.id.btnIniciar);
        Button btnAdicionarEsquerda =
                findViewById(R.id.btnAdicionarEsquerda);

        Button btnAdicionarDireita =
                findViewById(R.id.btnAdicionarDireita);

        btnIniciar.setOnClickListener(v -> {
            gameView.iniciarJogo(); //
        });

        btnAdicionarEsquerda.setOnClickListener(v -> {
            gameView.adicionarCanhao(Lado.ESQUERDO);
        });

        btnAdicionarDireita.setOnClickListener(v -> {
            gameView.adicionarCanhao(Lado.DIREITO);
        });
    }
}
