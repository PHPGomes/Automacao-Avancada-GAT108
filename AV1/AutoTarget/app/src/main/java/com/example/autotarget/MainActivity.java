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
        Button btnAdicionar = findViewById(R.id.btnAdicionar);

        btnIniciar.setOnClickListener(v -> {
            gameView.iniciarJogo(); //
        });

        btnAdicionar.setOnClickListener(v -> {
            gameView.adicionarCanhao();
        });
    }
}
