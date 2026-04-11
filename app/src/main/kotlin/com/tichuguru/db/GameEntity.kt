package com.tichuguru.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tichuguru.model.Game

@Entity(
    tableName = "games",
    foreignKeys = [
        ForeignKey(entity = PlayerEntity::class, parentColumns = ["id"], childColumns = ["player0"]),
        ForeignKey(entity = PlayerEntity::class, parentColumns = ["id"], childColumns = ["player1"]),
        ForeignKey(entity = PlayerEntity::class, parentColumns = ["id"], childColumns = ["player2"]),
        ForeignKey(entity = PlayerEntity::class, parentColumns = ["id"], childColumns = ["player3"])
    ],
    indices = [Index("player0"), Index("player1"), Index("player2"), Index("player3")]
)
data class GameEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var player0: Long = 0,
    var player1: Long = 0,
    var player2: Long = 0,
    var player3: Long = 0,
    var score1: Int = 0,
    var score2: Int = 0,
    var gameLimit: Int = 0,
    var gameOver: Boolean = false,
    var dateMs: Long = 0,
    var mercyRule: Boolean = false,
    var ignoreStats: Boolean = false,
    var addOnFailure: Boolean = false,
) {
    companion object {
        fun from(g: Game) =
            GameEntity(
                id = g.dbId,
                player0 = g.players[0].dbId,
                player1 = g.players[1].dbId,
                player2 = g.players[2].dbId,
                player3 = g.players[3].dbId,
                score1 = g.score1,
                score2 = g.score2,
                gameLimit = g.gameLimit,
                gameOver = g.gameOver,
                dateMs = g.date.toEpochMilli(),
                mercyRule = g.mercyRule,
                ignoreStats = g.ignoreStats,
                addOnFailure = g.addOnFailure
            )
    }
}
