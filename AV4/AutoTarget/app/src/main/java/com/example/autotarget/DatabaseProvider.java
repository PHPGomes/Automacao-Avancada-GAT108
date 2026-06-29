package com.example.autotarget;

import android.content.Context;

import androidx.room.Room;

import com.example.autotarget.AppDatabase;

public class DatabaseProvider {

    private static AppDatabase db;

    public static AppDatabase get(Context context){

        if(db == null){

            db = Room.databaseBuilder(
                            context,
                            AppDatabase.class,
                            "autotarget.db")
                    .fallbackToDestructiveMigration()
                    .build();

        }

        return db;

    }

}