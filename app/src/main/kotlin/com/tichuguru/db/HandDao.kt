package com.tichuguru.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface HandDao {
    @Query("SELECT * FROM hands WHERE gameId = :gameId ORDER BY handOrder")
    fun getHandsForGame(gameId: Long): List<HandEntity>

    @Upsert
    fun upsertAll(hands: List<HandEntity>): List<Long>

    @Query("DELETE FROM hands WHERE gameId = :gameId")
    fun deleteHandsForGame(gameId: Long)

    fun deleteOrphanHands(
        gameId: Long,
        keepIds: List<Long>,
    ) {
        if (keepIds.isNotEmpty()) deleteOrphanHandsInternal(gameId, keepIds)
    }

    @Query("DELETE FROM hands WHERE gameId = :gameId AND id NOT IN (:keepIds)")
    fun deleteOrphanHandsInternal(
        gameId: Long,
        keepIds: List<Long>,
    )
}
