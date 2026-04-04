package com.tichuguru.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {PlayerEntity.class, GameEntity.class, HandEntity.class}, version = 3, exportSchema = false)
public abstract class TichuDatabase extends RoomDatabase {
    private static TichuDatabase instance;

    public abstract PlayerDao playerDao();
    public abstract GameDao gameDao();
    public abstract HandDao handDao();

    public static TichuDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), TichuDatabase.class, "tichu.db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
