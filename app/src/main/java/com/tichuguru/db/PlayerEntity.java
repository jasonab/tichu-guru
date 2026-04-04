package com.tichuguru.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.tichuguru.model.Player;

@Entity(tableName = "players")
public class PlayerEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public int numGames;
    public int numWins;
    public int numHands;
    public int totalPoints;
    public int cardPoints;
    public int numDoubleWins;
    public int numTichuCalled;
    public int numTichuMade;
    public int numGTCalled;
    public int numGTMade;
    public int tichuEfficiencyPoints;
    public int tichuEfficiencyHands;
    public int numTichusStopped;
    public int numTichusCalledByOpps;
    public int numTichusCalledByPartner;
    public int numTichusMadeByPartner;

    public static PlayerEntity from(Player p) {
        PlayerEntity e = new PlayerEntity();
        e.id = p.getDbId();
        e.name = p.getName();
        e.numGames = p.getNumGames();
        e.numWins = p.getNumWins();
        e.numHands = p.getNumHands();
        e.totalPoints = p.getTotalPoints();
        e.cardPoints = p.getCardPoints();
        e.numDoubleWins = p.getNumDoubleWins();
        e.numTichuCalled = p.getNumTichuCalled();
        e.numTichuMade = p.getNumTichuMade();
        e.numGTCalled = p.getNumGTCalled();
        e.numGTMade = p.getNumGTMade();
        e.tichuEfficiencyPoints = p.getTichuEfficiencyPoints();
        e.tichuEfficiencyHands = p.getTichuEfficiencyHands();
        e.numTichusStopped = p.getNumTichusStopped();
        e.numTichusCalledByOpps = p.getNumTichusCalledByOpps();
        e.numTichusCalledByPartner = p.getNumTichusCalledByPartner();
        e.numTichusMadeByPartner = p.getNumTichusMadeByPartner();
        return e;
    }

    public Player toPlayer() {
        Player p = new Player(name);
        p.setDbId(id);
        p.setNumGames(numGames);
        p.setNumWins(numWins);
        p.setNumHands(numHands);
        p.setTotalPoints(totalPoints);
        p.setCardPoints(cardPoints);
        p.setNumDoubleWins(numDoubleWins);
        p.setNumTichuCalled(numTichuCalled);
        p.setNumTichuMade(numTichuMade);
        p.setNumGTCalled(numGTCalled);
        p.setNumGTMade(numGTMade);
        p.setTichuEfficiencyPoints(tichuEfficiencyPoints);
        p.setTichuEfficiencyHands(tichuEfficiencyHands);
        p.setNumTichusStopped(numTichusStopped);
        p.setNumTichusCalledByOpps(numTichusCalledByOpps);
        p.setNumTichusCalledByPartner(numTichusCalledByPartner);
        p.setNumTichusMadeByPartner(numTichusMadeByPartner);
        return p;
    }
}
