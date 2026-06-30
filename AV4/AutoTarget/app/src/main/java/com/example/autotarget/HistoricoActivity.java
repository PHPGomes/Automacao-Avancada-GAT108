package com.example.autotarget;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoricoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView txtResumoHistorico;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        recyclerView = findViewById(R.id.recyclerHistorico);
        txtResumoHistorico = findViewById(R.id.txtResumoHistorico);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        carregarHistorico();
    }

    private void carregarHistorico() {

        executor.execute(() -> {

            List<Partida> partidas = DatabaseProvider
                    .get(getApplicationContext())
                    .partidaDao()
                    .listar();

            runOnUiThread(() -> atualizarTela(partidas));
        });
    }

    private void atualizarTela(List<Partida> partidas) {

        PartidaAdapter adapter = new PartidaAdapter(partidas);
        recyclerView.setAdapter(adapter);

        txtResumoHistorico.setText(
                "Histórico de partidas (" + partidas.size() + ")"
        );
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}