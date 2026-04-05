package com.tichuguru

import android.app.Application
import android.util.Log
import com.tichuguru.db.GameEntity
import com.tichuguru.db.HandEntity
import com.tichuguru.db.PlayerEntity
import com.tichuguru.db.TichuDatabase
import com.tichuguru.model.Game
import com.tichuguru.model.Hand
import com.tichuguru.model.Player
import java.util.Date

class TGApp : Application() {

    private var curGame: Game? = null
    private var pendingGame: Game? = null
    private var pendingHand: Hand? = null
    private var games: MutableList<Game> = mutableListOf()
    private var players: MutableList<Player> = mutableListOf()
    private lateinit var db: TichuDatabase

    companion object {
        const val TAG = "tichuguru"
        private lateinit var instance: TGApp

        @JvmStatic fun getGame(): Game? = instance.curGame
        @JvmStatic fun setGame(game: Game?) { instance.curGame = game }
        @JvmStatic fun getGames(): List<Game> = instance.games
        @JvmStatic fun getPlayers(): List<Player> = instance.players
        @JvmStatic fun getPlayer(name: String): Player? = instance.players.find { it.name == name }
        @JvmStatic fun getPlayerById(id: Long): Player? = instance.players.find { it.dbId == id }
        @JvmStatic fun getPendingGame(): Game? = instance.pendingGame
        @JvmStatic fun setPendingGame(game: Game?) { instance.pendingGame = game }
        @JvmStatic fun getPendingHand(): Hand? = instance.pendingHand
        @JvmStatic fun setPendingHand(hand: Hand?) { instance.pendingHand = hand }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        db = TichuDatabase.getInstance(this)
        loadPlayers()
        loadGames()
    }

    fun loadPlayers() {
        players = db.playerDao().getAll().map { it.toPlayer() }.toMutableList()
    }

    fun savePlayers() {
        val entities = players.map { PlayerEntity.from(it) }
        val ids = db.playerDao().upsertAll(entities)
        players.forEachIndexed { i, p -> p.dbId = ids[i] }
    }

    fun loadGames() {
        games = db.gameDao().getAll().mapNotNull { buildGameFromEntity(it) }.toMutableList()
        curGame = games.lastOrNull()
    }

    private fun buildGameFromEntity(ge: GameEntity): Game? {
        val gamePlayers = listOf(ge.player0, ge.player1, ge.player2, ge.player3).map { pid ->
            getPlayerById(pid) ?: run {
                Log.e(TAG, "Player not found when loading game, id=$pid")
                return null
            }
        }
        return Game(gamePlayers).apply {
            dbId = ge.id
            score1 = ge.score1
            score2 = ge.score2
            gameLimit = ge.gameLimit
            gameOver = ge.gameOver
            date = Date(ge.dateMs)
            mercyRule = ge.mercyRule
            ignoreStats = ge.ignoreStats
            addOnFailure = ge.addOnFailure
            hands = db.handDao().getHandsForGame(ge.id).map { it.toHand() }.toMutableList()
        }
    }

    fun saveGames() {
        db.runInTransaction {
            for (g in games) {
                val gameId = db.gameDao().upsert(GameEntity.from(g))
                g.dbId = gameId
                val hands = g.hands
                if (hands.isEmpty()) {
                    db.handDao().deleteHandsForGame(gameId)
                } else {
                    val entities = hands.mapIndexed { i, h -> HandEntity.from(h, gameId, i) }
                    val handIds = db.handDao().upsertAll(entities)
                    hands.forEachIndexed { i, h -> h.dbId = handIds[i] }
                    db.handDao().deleteOrphanHands(gameId, handIds)
                }
            }
        }
    }

    fun deleteGame(game: Game) {
        db.gameDao().deleteById(game.dbId)
    }
}
