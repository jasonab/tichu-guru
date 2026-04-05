package com.tichuguru.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY name")
    fun getAll(): List<PlayerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(players: List<PlayerEntity>): List<Long>

    @Query("DELETE FROM players")
    fun deleteAll()
}
