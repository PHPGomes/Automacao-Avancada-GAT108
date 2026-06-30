package com.example.autotarget;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PartidaDao {

    @Insert
    void inserir(Partida partida);

    @Query("SELECT * FROM partidas ORDER BY data DESC")
    List<Partida> listar();

    @Delete
    void excluir(Partida partida);
}