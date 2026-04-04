package com.tichuguru;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tichuguru.model.Game;
import com.tichuguru.model.Player;
import java.util.List;

public class TGViewModel extends ViewModel {
    private final MutableLiveData<Game> currentGame = new MutableLiveData<>();
    private final MutableLiveData<List<Game>> allGames = new MutableLiveData<>();
    private final MutableLiveData<List<Player>> allPlayers = new MutableLiveData<>();
    private final MutableLiveData<Boolean> clearTichuButtons = new MutableLiveData<>();

    @NonNull
    public LiveData<Game> getCurrentGame() { return currentGame; }

    @NonNull
    public LiveData<List<Game>> getAllGames() { return allGames; }

    @NonNull
    public LiveData<List<Player>> getAllPlayers() { return allPlayers; }

    /** One-shot event: tells CurHandFragment to clear its Tichu radio buttons. */
    @NonNull
    public LiveData<Boolean> getClearTichuButtons() { return clearTichuButtons; }

    /** Signal CurHandFragment to clear its Tichu radio buttons. */
    public void requestClearTichuButtons() { clearTichuButtons.setValue(true); }

    /** Push all current TGApp state into LiveData. Call after any external
     *  mutation (e.g. child Activity finishes and TGApp was updated). */
    public void sync() {
        currentGame.setValue(TGApp.getGame());
        allGames.setValue(TGApp.getGames());
        allPlayers.setValue(TGApp.getPlayers());
    }

    /** Set the active game in both TGApp and LiveData. */
    public void setGame(Game game) {
        TGApp.setGame(game);
        currentGame.setValue(game);
        allGames.setValue(TGApp.getGames());
    }

    /** Notify that the current game's contents changed (hand added/removed). */
    public void notifyGameChanged() {
        currentGame.setValue(TGApp.getGame());
        allGames.setValue(TGApp.getGames());
    }

    /** Notify that the game list changed (game deleted/added). */
    public void notifyGamesChanged() {
        allGames.setValue(TGApp.getGames());
        currentGame.setValue(TGApp.getGame());
    }

    /** Notify that the player list changed. */
    public void notifyPlayersChanged() {
        allPlayers.setValue(TGApp.getPlayers());
    }
}
