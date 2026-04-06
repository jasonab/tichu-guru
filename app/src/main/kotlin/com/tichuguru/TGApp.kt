package com.tichuguru

import android.app.Application
import com.tichuguru.db.TichuDatabase
import com.tichuguru.model.Game
import com.tichuguru.model.Hand
import com.tichuguru.model.Player
import com.tichuguru.repository.TichuRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class TGApp : Application() {

    private var curGame: Game? = null
    private var pendingGame: Game? = null
    private var pendingHand: Hand? = null
    private var games: MutableList<Game> = mutableListOf()
    private var players: MutableList<Player> = mutableListOf()
    private lateinit var repository: TichuRepository
    private val dbScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

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
        runBlocking {
            withContext(Dispatchers.IO) {
                players = repository.loadPlayers()
                games = repository.loadGames(players)
            }
        }
        curGame = games.lastOrNull()
    }

    fun savePlayers() { dbScope.launch { repository.savePlayers(players) } }

    fun saveGames() { dbScope.launch { repository.saveGames(players, games) } }

    fun deleteGame(game: Game) { dbScope.launch { repository.deleteGame(game) } }
}
