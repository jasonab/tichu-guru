package com.tichuguru.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface GameDao {
    @Query("SELECT * FROM games ORDER BY id")
    List<GameEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(GameEntity game);

    @Query("DELETE FROM games")
    void deleteAll();
}
