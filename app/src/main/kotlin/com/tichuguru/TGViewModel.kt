package com.tichuguru

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tichuguru.model.Game
import com.tichuguru.model.Player

class TGViewModel : ViewModel() {
    private val _currentGame = MutableLiveData<Game>()
    private val _allGames = MutableLiveData<List<Game>>()
    private val _allPlayers = MutableLiveData<List<Player>>()
    private val _clearTichuButtons = MutableLiveData<Boolean>()

    fun getCurrentGame(): LiveData<Game> = _currentGame
    fun getAllGames(): LiveData<List<Game>> = _allGames
    fun getAllPlayers(): LiveData<List<Player>> = _allPlayers

    /** One-shot event: tells CurHandFragment to clear its Tichu radio buttons. */
    fun getClearTichuButtons(): LiveData<Boolean> = _clearTichuButtons

    /** Signal CurHandFragment to clear its Tichu radio buttons. */
    fun requestClearTichuButtons() { _clearTichuButtons.value = true }

    /** Push all current TGApp state into LiveData. Call after any external
     *  mutation (e.g. child Activity finishes and TGApp was updated). */
    fun sync() {
        _currentGame.value = TGApp.getGame()
        _allGames.value = TGApp.getGames()
        _allPlayers.value = TGApp.getPlayers()
    }

    /** Set the active game in both TGApp and LiveData. */
    fun setGame(game: Game) {
        TGApp.setGame(game)
        _currentGame.value = game
        _allGames.value = TGApp.getGames()
    }

    /** Notify that the current game's contents changed (hand added/removed). */
    fun notifyGameChanged() {
        _currentGame.value = TGApp.getGame()
        _allGames.value = TGApp.getGames()
    }

    /** Notify that the game list changed (game deleted/added). */
    fun notifyGamesChanged() {
        _allGames.value = TGApp.getGames()
        _currentGame.value = TGApp.getGame()
    }

    /** Notify that the player list changed. */
    fun notifyPlayersChanged() {
        _allPlayers.value = TGApp.getPlayers()
    }
}
