package com.example.autotarget;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.autotarget.Partida;
import com.example.autotarget.PartidaDao;

@Database(
        entities = {Partida.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PartidaDao partidaDao();

}