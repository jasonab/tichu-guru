package com.tichuguru.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY name")
    fun getAll(): List<PlayerEntity>

    @Upsert
    fun upsertAll(players: List<PlayerEntity>): List<Long>
}
