package com.tichuguru.repository

import android.util.Log
import com.tichuguru.TGApp
import com.tichuguru.db.GameEntity
import com.tichuguru.db.HandEntity
import com.tichuguru.db.PlayerEntity
import com.tichuguru.db.TichuDatabase
import com.tichuguru.model.Game
import com.tichuguru.model.Player
import java.util.Date

class TichuRepository(private val db: TichuDatabase) {

    fun loadPlayers(): MutableList<Player> =
        db.playerDao().getAll().map { it.toPlayer() }.toMutableList()

    fun savePlayers(players: List<Player>) {
        val entities = players.map { PlayerEntity.from(it) }
        val ids = db.playerDao().upsertAll(entities)
        players.forEachIndexed { i, p -> if (ids[i] > 0) p.dbId = ids[i] }
    }

    fun loadGames(players: List<Player>): MutableList<Game> =
        db.gameDao().getAll().mapNotNull { buildGameFromEntity(it, players) }.toMutableList()

    fun saveGames(players: List<Player>, games: List<Game>) {
        db.runInTransaction {
            val entities = players.map { PlayerEntity.from(it) }
            val ids = db.playerDao().upsertAll(entities)
            players.forEachIndexed { i, p -> if (ids[i] > 0) p.dbId = ids[i] }
            for (g in games) {
                val upsertedId = db.gameDao().upsert(GameEntity.from(g))
                if (upsertedId > 0) g.dbId = upsertedId
                val gid = g.dbId
                val hands = g.hands
                if (hands.isEmpty()) {
                    db.handDao().deleteHandsForGame(gid)
                } else {
                    val handEntities = hands.mapIndexed { i, h -> HandEntity.from(h, gid, i) }
                    val handIds = db.handDao().upsertAll(handEntities)
                    hands.forEachIndexed { i, h -> if (handIds[i] > 0) h.dbId = handIds[i] }
                    db.handDao().deleteOrphanHands(gid, hands.map { it.dbId })
                }
            }
        }
    }

    fun deleteGame(game: Game) {
        db.gameDao().deleteById(game.dbId)
    }

    private fun buildGameFromEntity(ge: GameEntity, players: List<Player>): Game? {
        fun findPlayer(id: Long): Player? {
            val p = players.find { it.dbId == id }
            if (p == null) Log.e(TGApp.TAG, "Player not found when loading game, id=$id")
            return p
        }
        val gamePlayers = listOf(ge.player0, ge.player1, ge.player2, ge.player3)
            .map { findPlayer(it) ?: return null }
        return Game(gamePlayers).apply {
            dbId        = ge.id
            score1      = ge.score1
            score2      = ge.score2
            gameLimit   = ge.gameLimit
            gameOver    = ge.gameOver
            date        = Date(ge.dateMs)
            mercyRule   = ge.mercyRule
            ignoreStats = ge.ignoreStats
            addOnFailure = ge.addOnFailure
            hands = db.handDao().getHandsForGame(ge.id).map { it.toHand() }.toMutableList()
        }
    }
}
