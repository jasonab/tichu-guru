package com.tichuguru

import android.app.Application
import com.tichuguru.db.TichuDatabase
import com.tichuguru.model.Game
import com.tichuguru.model.Hand
import com.tichuguru.model.Player
import com.tichuguru.repository.TichuRepository

class TGApp : Application() {

    private var curGame: Game? = null
    private var pendingGame: Game? = null
    private var pendingHand: Hand? = null
    private var games: MutableList<Game> = mutableListOf()
    private var players: MutableList<Player> = mutableListOf()
    private lateinit var repository: TichuRepository

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
        repository = TichuRepository(TichuDatabase.getInstance(this))
        players = repository.loadPlayers()
        games = repository.loadGames(players)
        curGame = games.lastOrNull()
    }

    fun savePlayers() = repository.savePlayers(players)

    fun saveGames() = repository.saveGames(players, games)

    fun deleteGame(game: Game) = repository.deleteGame(game)
}
