package com.tichuguru.db;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface HandDao {
    @NonNull
    @Query("SELECT * FROM hands WHERE gameId = :gameId ORDER BY handOrder")
    List<HandEntity> getHandsForGame(long gameId);

    @Insert
    void insertAll(@NonNull List<HandEntity> hands);

    @Query("DELETE FROM hands WHERE gameId = :gameId")
    void deleteHandsForGame(long gameId);

    @Query("DELETE FROM hands")
    void deleteAll();
}
