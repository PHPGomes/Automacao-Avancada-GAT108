package com.example.autotarget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PartidaAdapter extends RecyclerView.Adapter<PartidaAdapter.ViewHolder> {

    private final List<Partida> partidas;

    public PartidaAdapter(List<Partida> partidas) {
        this.partidas = partidas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_partida, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        Partida partida = partidas.get(position);

        holder.usuario.setText("Usuário: " + partida.usuario);

        holder.pontos.setText(
                "Pontuação: "
                        + partida.pontosEsquerda
                        + " x "
                        + partida.pontosDireita
        );

        holder.canhões.setText(
                "Canhões: "
                        + partida.canhoesEsquerda
                        + " x "
                        + partida.canhoesDireita
        );

        holder.tempo.setText("Tempo: " + partida.tempo + " s");

        String dataFormatada = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
        ).format(partida.data);

        holder.data.setText(dataFormatada);
    }

    @Override
    public int getItemCount() {
        return partidas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView usuario;
        TextView pontos;
        TextView canhões;
        TextView tempo;
        TextView data;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            usuario = itemView.findViewById(R.id.txtUsuario);
            pontos = itemView.findViewById(R.id.txtPontos);
            canhões = itemView.findViewById(R.id.txtCanhoes);
            tempo = itemView.findViewById(R.id.txtTempo);
            data = itemView.findViewById(R.id.txtData);
        }
    }
}