package com.example.autotarget;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {Partida.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PartidaDao partidaDao();
}