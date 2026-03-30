package com.tichuguru;

import android.app.Application;
import android.util.Log;
import com.tichuguru.model.Game;
import com.tichuguru.model.Player;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class TGApp extends Application {
    public static final String CSV_FILE = "TichuGuru.csv";
    private static final int GAMEREV = 0;
    public static final String GAME_FILE = "Games.dat";
    private static final int PLAYERREV = 0;
    public static final String PLAYER_FILE = "Players.dat";
    public static final String TAG = "tichuguru";
    private static Game curGame;
    private static List<Game> games;
    private static List<Player> players;

    @Override // android.app.Application
    public void onCreate() {
        loadPlayers(null);
        loadGames(null);
        super.onCreate();
    }

    public void loadPlayers(File file) {
        ObjectInputStream in;
        players = new ArrayList();
        try {
            if (file == null) {
                in = new ObjectInputStream(new BufferedInputStream(openFileInput(PLAYER_FILE)));
            } else {
                in = new ObjectInputStream(new FileInputStream(file));
            }
            try {
                int rev = in.readInt();
                if (rev > 0) {
                    throw new IOException("Player file is newer than software");
                }
                int numPlayers = in.readInt();
                for (int i = 0; i < numPlayers; i++) {
                    players.add((Player) in.readObject());
                }
            } finally {
                in.close();
            }
        } catch (FileNotFoundException e) {
        } catch (Exception e2) {
            Log.e(TAG, "Error while loading players", e2);
        }
    }

    public void savePlayers(File file) {
        ObjectOutputStream out;
        try {
            if (file == null) {
                out = new ObjectOutputStream(new BufferedOutputStream(openFileOutput(PLAYER_FILE, 0)));
            } else {
                out = new ObjectOutputStream(Files.newOutputStream(file.toPath()));
            }
            try {
                out.writeInt(0);
                out.writeInt(players.size());
                for (Player p : players) {
                    out.writeObject(p);
                }
            } finally {
                out.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error while saving players", e);
        }
    }

    public void saveCSV(File file) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(file));
            try {
                out.println(Player.getCSVHeader());
                for (Player p : players) {
                    out.println(p.toCSVString());
                }
            } finally {
                out.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error while saving players", e);
        }
    }

    public void loadGames(File file) {
        ObjectInputStream in;
        int rev;
        games = new ArrayList();
        try {
            if (file == null) {
                in = new ObjectInputStream(new BufferedInputStream(openFileInput(GAME_FILE)));
            } else {
                in = new ObjectInputStream(new FileInputStream(file));
            }
            try {
                rev = in.readInt();
                if (rev > 0) {
                    throw new IOException("Game file is newer than software");
                }
                int numGames = in.readInt();
                for (int i = 0; i < numGames; i++) {
                    games.add((Game) in.readObject());
                }
                if (!games.isEmpty()) {
                    curGame = games.get(games.size() - 1);
                }
            } finally {
                in.close();
            }
        } catch (FileNotFoundException e) {
        } catch (Exception e2) {
            Log.e(TAG, "Error while loading games", e2);
        }

    }

    public void saveGames(File file) {
        ObjectOutputStream out;
        try {
            if (file == null) {
                out = new ObjectOutputStream(new BufferedOutputStream(openFileOutput(GAME_FILE, 0)));
            } else {
                out = new ObjectOutputStream(new FileOutputStream(file));
            }
            try {
                out.writeInt(0);
                out.writeInt(games.size());
                for (Game g : games) {
                    out.writeObject(g);
                }
            } finally {
                out.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error while saving games", e);
        }
    }

    public static List<Game> getGames() {
        return games;
    }

    public static List<Player> getPlayers() {
        return players;
    }

    public static Player getPlayer(String name) {
        for (Player p : players) {
            if (name.equals(p.getName())) {
                return p;
            }
        }
        return null;
    }

    public static Game getGame() {
        return curGame;
    }

    public static void setGame(Game game) {
        curGame = game;
    }
}
