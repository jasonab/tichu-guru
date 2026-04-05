package com.tichuguru.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {PlayerEntity.class, GameEntity.class, HandEntity.class}, version = 1, exportSchema = false)
public abstract class TichuDatabase extends RoomDatabase {
    private static volatile TichuDatabase instance;

    @NonNull public abstract PlayerDao playerDao();
    @NonNull public abstract GameDao gameDao();
    @NonNull public abstract HandDao handDao();

    @NonNull
    public static TichuDatabase getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (TichuDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), TichuDatabase.class, "tichu.db")
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return instance;
    }
}
