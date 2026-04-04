package com.tichuguru.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY name")
    List<PlayerEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<PlayerEntity> players);

    @Query("DELETE FROM players")
    void deleteAll();
}
