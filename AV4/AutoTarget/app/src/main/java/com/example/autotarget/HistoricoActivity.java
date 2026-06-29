package com.example.autotarget;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoricoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PartidaAdapter adapter;
    private List<Partida> partidas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        recyclerView = findViewById(R.id.recyclerHistorico);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        partidas = new ArrayList<>();

        // ====== Dados de teste ======

        Partida p1 = new Partida();
        p1.usuario = "Pedro";
        p1.pontosEsquerda = 18;
        p1.pontosDireita = 12;
        p1.tempo = 60;
        p1.data = System.currentTimeMillis();

        Partida p2 = new Partida();
        p2.usuario = "Maria";
        p2.pontosEsquerda = 22;
        p2.pontosDireita = 19;
        p2.tempo = 60;
        p2.data = System.currentTimeMillis();

        Partida p3 = new Partida();
        p3.usuario = "João";
        p3.pontosEsquerda = 15;
        p3.pontosDireita = 14;
        p3.tempo = 60;
        p3.data = System.currentTimeMillis();

        partidas.add(p1);
        partidas.add(p2);
        partidas.add(p3);

        // ============================

        adapter = new PartidaAdapter(partidas);

        recyclerView.setAdapter(adapter);
    }
}