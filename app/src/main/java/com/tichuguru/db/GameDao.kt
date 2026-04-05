package com.tichuguru.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY id")
    fun getAll(): List<GameEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(game: GameEntity): Long
}
