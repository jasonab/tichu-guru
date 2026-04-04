package com.tichuguru.model;

import com.tichuguru.TGApp;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class Game implements Externalizable {
    private static final int REVISION = 1;
    public static final long serialVersionUID = 1;
    private boolean addOnFailure;
    private Date date;
    private long dbId;
    private int gameLimit;
    private boolean gameOver;
    private List<Hand> hands;
    private boolean ignoreStats;
    private boolean mercyRule;
    private List<Player> players;
    private int score1;
    private int score2;

    public Game() {
    }

    public Game(List<Player> players) {
        this.players = players;
        this.hands = new ArrayList();
        this.gameLimit = 1000;
        this.gameOver = false;
        this.mercyRule = true;
        this.ignoreStats = false;
        this.addOnFailure = false;
        this.date = new Date();
    }

    public Game(Game g) {
        this(g.players);
        this.mercyRule = g.mercyRule;
        this.addOnFailure = g.addOnFailure;
    }

    public int lastScore1() {
        if (this.hands.isEmpty()) {
            return 0;
        }
        return this.hands.get(this.hands.size() - 1).getTotalScore1();
    }

    public int lastScore2() {
        if (this.hands.isEmpty()) {
            return 0;
        }
        return this.hands.get(this.hands.size() - 1).getTotalScore2();
    }

    public void endGame() {
        this.gameOver = true;
        if (!this.ignoreStats) {
            for (int i = 0; i < 4; i++) {
                this.players.get(i).recordGame(this, i);
            }
        }
    }

    public void scoreHand(Hand hand) {
        if (!this.gameOver) {
            this.hands.add(hand);
            this.score1 += hand.getTotalScore1();
            this.score2 += hand.getTotalScore2();
            if (this.score1 != this.score2 && (this.score1 >= this.gameLimit || this.score2 >= this.gameLimit || (this.mercyRule && Math.abs(this.score1 - this.score2) >= this.gameLimit))) {
                this.gameOver = true;
            }
            if (!this.ignoreStats) {
                for (int i = 0; i < 4; i++) {
                    this.players.get(i).recordHand(hand, i);
                    if (this.gameOver) {
                        this.players.get(i).recordGame(this, i);
                    }
                }
            }
        }
    }

    public void removeHand(int handNum) {
        Hand hand = this.hands.get(handNum);
        boolean undoGame = this.gameOver;
        this.gameOver = false;
        for (int i = 0; i < 4; i++) {
            Player p = this.players.get(i);
            if (undoGame) {
                p.unrecordGame(this, i);
            }
            p.unrecordHand(hand, i);
        }
        this.score1 -= hand.getTotalScore1();
        this.score2 -= hand.getTotalScore2();
        this.hands.remove(handNum);
    }

    public boolean containsPlayer(Player p) {
        for (Player p2 : this.players) {
            if (p == p2) {
                return true;
            }
        }
        return false;
    }

    @Override // java.io.Externalizable
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int rev = in.readInt();
        if (rev > 1) {
            throw new IOException("Game newer than software");
        }
        this.players = new ArrayList();
        for (int i = 0; i < 4; i++) {
            String name = (String) in.readObject();
            Player foundP = null;
            List<Player> allPlayers = TGApp.getPlayers();
            Iterator<Player> it = allPlayers.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Player p = it.next();
                if (name.equals(p.getName())) {
                    foundP = p;
                    break;
                }
            }
            if (foundP == null) {
                foundP = new Player(name);
                if (!name.equals("New Player")) {
                    throw new RuntimeException("Couldn't find player " + name);
                }
            }
            this.players.add(foundP);
        }
        this.hands = (List) in.readObject();
        this.score1 = in.readInt();
        this.score2 = in.readInt();
        this.gameLimit = in.readInt();
        this.gameOver = in.readBoolean();
        this.date = (Date) in.readObject();
        this.mercyRule = in.readBoolean();
        this.ignoreStats = in.readBoolean();
        this.addOnFailure = in.readBoolean();
    }

    @Override // java.io.Externalizable
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(1);
        for (int i = 0; i < 4; i++) {
            out.writeObject(this.players.get(i).getName());
        }
        out.writeObject(this.hands);
        out.writeInt(this.score1);
        out.writeInt(this.score2);
        out.writeInt(this.gameLimit);
        out.writeBoolean(this.gameOver);
        out.writeObject(this.date);
        out.writeBoolean(this.mercyRule);
        out.writeBoolean(this.ignoreStats);
        out.writeBoolean(this.addOnFailure);
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public void setPlayer(int seat, Player p) {
        this.players.set(seat, p);
    }

    public int getScore1() {
        return this.score1;
    }

    public int getScore2() {
        return this.score2;
    }

    public int getGameLimit() {
        return this.gameLimit;
    }

    public boolean isGameOver() {
        return this.gameOver;
    }

    public Date getDate() {
        return this.date;
    }

    public boolean isMercyRule() {
        return this.mercyRule;
    }

    public boolean isIgnoreStats() {
        return this.ignoreStats;
    }

    public boolean isAddOnFailure() {
        return this.addOnFailure;
    }

    public void setMercyRule(boolean mercyRule) {
        this.mercyRule = mercyRule;
    }

    public void setIgnoreStats(boolean ignoreStats) {
        this.ignoreStats = ignoreStats;
    }

    public void setAddOnFailure(boolean addOnFailure) {
        this.addOnFailure = addOnFailure;
    }

    public List<Hand> getHands() {
        return this.hands;
    }

    public long getDbId() { return dbId; }
    public void setDbId(long dbId) { this.dbId = dbId; }

    public void setGameLimit(int gameLimit) {
        this.gameLimit = gameLimit;
    }

    public void setScore1(int score1) { this.score1 = score1; }
    public void setScore2(int score2) { this.score2 = score2; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    public void setDate(Date date) { this.date = date; }
    public void setHands(List<Hand> hands) { this.hands = hands; }
}
