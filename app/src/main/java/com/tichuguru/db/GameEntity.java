package com.tichuguru.db;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.tichuguru.model.Game;

@Entity(
    tableName = "games",
    foreignKeys = {
        @ForeignKey(entity = PlayerEntity.class, parentColumns = "id", childColumns = "player0"),
        @ForeignKey(entity = PlayerEntity.class, parentColumns = "id", childColumns = "player1"),
        @ForeignKey(entity = PlayerEntity.class, parentColumns = "id", childColumns = "player2"),
        @ForeignKey(entity = PlayerEntity.class, parentColumns = "id", childColumns = "player3")
    },
    indices = {
        @Index("player0"), @Index("player1"), @Index("player2"), @Index("player3")
    }
)
public class GameEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long player0;
    public long player1;
    public long player2;
    public long player3;
    public int score1;
    public int score2;
    public int gameLimit;
    public boolean gameOver;
    public long dateMs;
    public boolean mercyRule;
    public boolean ignoreStats;
    public boolean addOnFailure;

    public static GameEntity from(Game g) {
        GameEntity e = new GameEntity();
        e.id = g.getDbId();
        e.player0 = g.getPlayers().get(0).getDbId();
        e.player1 = g.getPlayers().get(1).getDbId();
        e.player2 = g.getPlayers().get(2).getDbId();
        e.player3 = g.getPlayers().get(3).getDbId();
        e.score1 = g.getScore1();
        e.score2 = g.getScore2();
        e.gameLimit = g.getGameLimit();
        e.gameOver = g.isGameOver();
        e.dateMs = g.getDate().getTime();
        e.mercyRule = g.isMercyRule();
        e.ignoreStats = g.isIgnoreStats();
        e.addOnFailure = g.isAddOnFailure();
        return e;
    }
}
