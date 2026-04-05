package com.tichuguru.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY id")
    fun getAll(): List<GameEntity>

    @Upsert
    fun upsert(game: GameEntity): Long

    @Query("DELETE FROM games WHERE id = :id")
    fun deleteById(id: Long)
}
