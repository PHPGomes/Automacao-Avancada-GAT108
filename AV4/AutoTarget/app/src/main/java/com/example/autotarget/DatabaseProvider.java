package com.example.autotarget;

import android.content.Context;

import androidx.room.Room;

public class DatabaseProvider {

    private static AppDatabase db;

    public static synchronized AppDatabase get(Context context) {

        if (db == null) {
            db = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "autotarget.db"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return db;
    }
}