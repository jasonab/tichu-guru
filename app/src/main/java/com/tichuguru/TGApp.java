package com.tichuguru;

import android.app.Application;
import android.util.Log;
import com.tichuguru.db.GameEntity;
import com.tichuguru.db.HandEntity;
import com.tichuguru.db.PlayerEntity;
import com.tichuguru.db.TichuDatabase;
import com.tichuguru.model.Game;
import com.tichuguru.model.Hand;
import com.tichuguru.model.Player;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TGApp extends Application {
    public static final String TAG = "tichuguru";

    private static Game curGame;
    private static List<Game> games;
    private static List<Player> players;
    private TichuDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = TichuDatabase.getInstance(this);
        loadPlayers();
        loadGames();
    }

    public void loadPlayers() {
        players = new ArrayList<>();
        for (PlayerEntity e : db.playerDao().getAll()) {
            players.add(e.toPlayer());
        }
    }

    public void savePlayers() {
        List<PlayerEntity> entities = new ArrayList<>(players.size());
        for (Player p : players) {
            entities.add(PlayerEntity.from(p));
        }
        List<Long> ids = db.playerDao().insertAll(entities);
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setDbId(ids.get(i));
        }
    }

    public void loadGames() {
        games = new ArrayList<>();
        for (GameEntity ge : db.gameDao().getAll()) {
            Game g = buildGameFromEntity(ge);
            if (g != null) games.add(g);
        }
        if (!games.isEmpty()) curGame = games.get(games.size() - 1);
    }

    private Game buildGameFromEntity(GameEntity ge) {
        List<Player> gamePlayers = new ArrayList<>(4);
        long[] playerIds = {ge.player0, ge.player1, ge.player2, ge.player3};
        for (long pid : playerIds) {
            Player p = getPlayerById(pid);
            if (p == null) {
                Log.e(TAG, "Player not found when loading game, id=" + pid);
                return null;
            }
            gamePlayers.add(p);
        }
        Game g = new Game(gamePlayers);
        g.setDbId(ge.id);
        g.setScore1(ge.score1);
        g.setScore2(ge.score2);
        g.setGameLimit(ge.gameLimit);
        g.setGameOver(ge.gameOver);
        g.setDate(new Date(ge.dateMs));
        g.setMercyRule(ge.mercyRule);
        g.setIgnoreStats(ge.ignoreStats);
        g.setAddOnFailure(ge.addOnFailure);
        List<HandEntity> handEntities = db.handDao().getHandsForGame(ge.id);
        List<Hand> hands = new ArrayList<>(handEntities.size());
        for (HandEntity he : handEntities) {
            hands.add(he.toHand());
        }
        g.setHands(hands);
        return g;
    }

    public void saveGames() {
        db.runInTransaction(() -> {
            for (Game g : games) {
                long gameId = db.gameDao().insert(GameEntity.from(g));
                g.setDbId(gameId);
                db.handDao().deleteHandsForGame(gameId);
                List<Hand> hands = g.getHands();
                if (!hands.isEmpty()) {
                    List<HandEntity> handEntities = new ArrayList<>(hands.size());
                    for (int i = 0; i < hands.size(); i++) {
                        handEntities.add(HandEntity.from(hands.get(i), gameId, i));
                    }
                    db.handDao().insertAll(handEntities);
                }
            }
        });
    }

    public void deleteGame(Game game) {
        db.gameDao().deleteById(game.getDbId());
    }

    public static List<Game> getGames() { return games; }
    public static List<Player> getPlayers() { return players; }

    public static Player getPlayer(String name) {
        for (Player p : players) {
            if (name.equals(p.getName())) return p;
        }
        return null;
    }

    public static Player getPlayerById(long id) {
        for (Player p : players) {
            if (p.getDbId() == id) return p;
        }
        return null;
    }

    public static Game getGame() { return curGame; }
    public static void setGame(Game game) { curGame = game; }
}
