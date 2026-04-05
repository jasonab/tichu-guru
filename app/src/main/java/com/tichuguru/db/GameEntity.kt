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
    @JvmField @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @JvmField var player0: Long = 0,
    @JvmField var player1: Long = 0,
    @JvmField var player2: Long = 0,
    @JvmField var player3: Long = 0,
    @JvmField var score1: Int = 0,
    @JvmField var score2: Int = 0,
    @JvmField var gameLimit: Int = 0,
    @JvmField var gameOver: Boolean = false,
    @JvmField var dateMs: Long = 0,
    @JvmField var mercyRule: Boolean = false,
    @JvmField var ignoreStats: Boolean = false,
    @JvmField var addOnFailure: Boolean = false
) {
    companion object {
        @JvmStatic
        fun from(g: Game) = GameEntity(
            id = g.dbId,
            player0 = g.players[0].dbId,
            player1 = g.players[1].dbId,
            player2 = g.players[2].dbId,
            player3 = g.players[3].dbId,
            score1 = g.score1,
            score2 = g.score2,
            gameLimit = g.gameLimit,
            gameOver = g.isGameOver,
            dateMs = g.date.time,
            mercyRule = g.isMercyRule,
            ignoreStats = g.isIgnoreStats,
            addOnFailure = g.isAddOnFailure
        )
    }
}
