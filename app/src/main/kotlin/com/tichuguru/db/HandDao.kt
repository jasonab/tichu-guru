package com.tichuguru.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HandDao {
    @Query("SELECT * FROM hands WHERE gameId = :gameId ORDER BY handOrder")
    fun getHandsForGame(gameId: Long): List<HandEntity>

    @Insert
    fun insertAll(hands: List<HandEntity>)

    @Query("DELETE FROM hands WHERE gameId = :gameId")
    fun deleteHandsForGame(gameId: Long)

    @Query("DELETE FROM hands")
    fun deleteAll()
}
