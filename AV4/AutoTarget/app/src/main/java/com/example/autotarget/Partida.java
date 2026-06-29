package com.example.autotarget;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "partidas")
public class Partida {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String usuario;

    public int pontosEsquerda;

    public int pontosDireita;

    public long data;

    public int canhoesEsquerda;

    public int canhoesDireita;

    public int tempo;

}