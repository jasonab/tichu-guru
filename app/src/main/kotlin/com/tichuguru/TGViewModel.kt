package com.tichuguru

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tichuguru.db.TichuDatabase
import com.tichuguru.model.Game
import com.tichuguru.model.Hand
import com.tichuguru.model.Player
import com.tichuguru.repository.TichuRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TGViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TichuRepository(TichuDatabase.getInstance(application))
    private val dbScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    private val players: MutableList<Player> = mutableListOf()
    private val games: MutableList<Game> = mutableListOf()
    private var curGame: Game? = null

    private val _currentGame = MutableLiveData<Game?>()
    private val _allGames = MutableLiveData<List<Game>>()
    private val _allPlayers = MutableLiveData<List<Player>>()
    private val _clearTichuButtons = MutableLiveData<Boolean>()
    private val _initialized = MutableLiveData<Boolean>()

    var isInitialized = false
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            players.addAll(repository.loadPlayers())
            games.addAll(repository.loadGames(players))
            curGame = games.lastOrNull()
            withContext(Dispatchers.Main) {
                isInitialized = true
                _currentGame.value = curGame
                _allGames.value = games
                _allPlayers.value = players
                _initialized.value = true
            }
        }
    }

    fun getCurrentGame(): LiveData<Game?> = _currentGame
    fun getAllGames(): LiveData<List<Game>> = _allGames
    fun getAllPlayers(): LiveData<List<Player>> = _allPlayers
    fun getClearTichuButtons(): LiveData<Boolean> = _clearTichuButtons
    fun getInitialized(): LiveData<Boolean> = _initialized
    fun requestClearTichuButtons() { _clearTichuButtons.value = true }
    fun getPlayer(name: String): Player? = players.find { it.name == name }

    fun setGame(game: Game) {
        curGame = game
        _currentGame.value = game
        _allGames.value = games
    }

    fun endGame() {
        curGame?.endGame()
        saveGames()
        savePlayers()
        _currentGame.value = curGame
        _allGames.value = games
    }

    fun deleteLastHand() {
        val game = curGame ?: return
        game.removeHand(game.hands.size - 1)
        saveGames()
        savePlayers()
        _currentGame.value = curGame
        _allGames.value = games
    }

    fun scoreHand(hand: Hand) {
        curGame?.scoreHand(hand)
        saveGames()
        savePlayers()
        _currentGame.value = curGame
        _allGames.value = games
    }

    fun addGame(game: Game) {
        games.add(game)
        curGame = game
        saveGames()
        _allGames.value = games
        _currentGame.value = curGame
    }

    fun deleteGame(game: Game) {
        games.remove(game)
        dbScope.launch { repository.deleteGame(game) }
        if (curGame === game) {
            curGame = if (games.isEmpty()) null else games.last()
        }
        _allGames.value = games
        _currentGame.value = curGame
    }

    fun addPlayer(player: Player) {
        players.add(player)
        players.sort()
        savePlayers()
        _allPlayers.value = players
    }

    fun clearPlayerStats(player: Player) {
        player.clearStats()
        savePlayers()
        _allPlayers.value = players
    }

    fun clearAllPlayerStats() {
        for (p in players) p.clearStats()
        savePlayers()
        _allPlayers.value = players
    }

    fun deletePlayer(player: Player) {
        for (i in games.indices.reversed()) {
            val game = games[i]
            if (game.containsPlayer(player)) {
                games.removeAt(i)
                if (game === curGame) {
                    curGame = if (games.isEmpty()) null else games.last()
                }
            }
        }
        players.remove(player)
        saveGames()
        savePlayers()
        _allGames.value = games
        _currentGame.value = curGame
        _allPlayers.value = players
    }

    private fun savePlayers() { dbScope.launch { repository.savePlayers(players) } }
    private fun saveGames() { dbScope.launch { repository.saveGames(players, games) } }
}
