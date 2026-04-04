package com.tichuguru.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/* loaded from: classes.dex */
public class Player implements Comparable<Player>, Externalizable {
    private static final int REVISION = 1;
    public static final long serialVersionUID = 1;
    private int cardPoints;
    private long dbId;
    private String name;
    private int numDoubleWins;
    private int numGTCalled;
    private int numGTMade;
    private int numGames;
    private int numHands;
    private int numTichuCalled;
    private int numTichuMade;
    private int numTichusCalledByOpps;
    private int numTichusCalledByPartner;
    private int numTichusMadeByPartner;
    private int numTichusStopped;
    private int numWins;
    private int tichuEfficiencyHands;
    private int tichuEfficiencyPoints;
    private int totalPoints;

    public Player() {
    }

    public Player(String name) {
        this.name = name;
    }

    public void recordGame(Game game, int seat) {
        this.numGames++;
        if (seat == 0 || seat == 2) {
            if (game.getScore1() > game.getScore2()) {
                this.numWins++;
            }
        } else if (game.getScore1() < game.getScore2()) {
            this.numWins++;
        }
    }

    public void unrecordGame(Game game, int seat) {
        this.numGames--;
        if (seat == 0 || seat == 2) {
            if (game.getScore1() > game.getScore2()) {
                this.numWins--;
            }
        } else if (game.getScore1() < game.getScore2()) {
            this.numWins--;
        }
    }

    public void recordHand(Hand hand, int seat) {
        this.numHands++;
        int numTichusThisHand = 0;
        for (int i = 0; i < 4; i++) {
            if (hand.isTichuFor(i) || hand.isGrandTichuFor(i)) {
                numTichusThisHand++;
            }
        }
        if (seat == 0 || seat == 2) {
            this.totalPoints += hand.getTotalScore1();
            if (hand.getCardScore1() == 200) {
                this.cardPoints += 100;
                this.numDoubleWins++;
            } else {
                this.cardPoints += hand.getCardScore1();
            }
            if (hand.isTichuFor(1) || hand.isGrandTichuFor(1)) {
                this.numTichusCalledByOpps++;
                if (hand.outFirst() != 1) {
                    this.numTichusStopped++;
                }
            }
            if (hand.isTichuFor(3) || hand.isGrandTichuFor(3)) {
                this.numTichusCalledByOpps++;
                if (hand.outFirst() != 3) {
                    this.numTichusStopped++;
                }
            }
        } else {
            this.totalPoints += hand.getTotalScore2();
            if (hand.getCardScore2() == 200) {
                this.cardPoints += 100;
                this.numDoubleWins++;
            } else {
                this.cardPoints += hand.getCardScore2();
            }
            if (hand.isTichuFor(0) || hand.isGrandTichuFor(0)) {
                this.numTichusCalledByOpps++;
                if (hand.outFirst() != 0) {
                    this.numTichusStopped++;
                }
            }
            if (hand.isTichuFor(2) || hand.isGrandTichuFor(2)) {
                this.numTichusCalledByOpps++;
                if (hand.outFirst() != 2) {
                    this.numTichusStopped++;
                }
            }
        }
        if (numTichusThisHand == 0 && hand.outFirst() == seat) {
            this.tichuEfficiencyHands++;
        }
        if (hand.isTichuFor(seat)) {
            this.numTichuCalled++;
            if (hand.outFirst() == seat) {
                this.numTichuMade++;
            }
            this.tichuEfficiencyPoints = (seat == hand.outFirst() ? 100 : -100) + this.tichuEfficiencyPoints;
            this.tichuEfficiencyHands++;
        }
        if (hand.isGrandTichuFor(seat)) {
            this.numGTCalled++;
            if (hand.outFirst() == seat) {
                this.numGTMade++;
            }
        }
        int partner = (seat + 2) % 4;
        if (hand.isTichuFor(partner)) {
            this.numTichusCalledByPartner++;
            if (hand.outFirst() == partner) {
                this.numTichusMadeByPartner++;
            }
        }
    }

    public void unrecordHand(Hand hand, int seat) {
        if (this.numHands != 0) {
            this.numHands--;
            int numTichusThisHand = 0;
            for (int i = 0; i < 4; i++) {
                if (hand.isTichuFor(i) || hand.isGrandTichuFor(i)) {
                    numTichusThisHand++;
                }
            }
            if (seat == 0 || seat == 2) {
                this.totalPoints -= hand.getTotalScore1();
                if (hand.getCardScore1() == 200) {
                    this.cardPoints -= 100;
                    this.numDoubleWins--;
                } else {
                    this.cardPoints -= hand.getCardScore1();
                }
                if (hand.isTichuFor(seat)) {
                    this.tichuEfficiencyPoints -= hand.getTichuScore1();
                    this.tichuEfficiencyHands--;
                }
                if (hand.isTichuFor(1) || hand.isGrandTichuFor(1)) {
                    this.numTichusCalledByOpps--;
                    if (hand.outFirst() != 1) {
                        this.numTichusStopped--;
                    }
                }
                if (hand.isTichuFor(3) || hand.isGrandTichuFor(3)) {
                    this.numTichusCalledByOpps--;
                    if (hand.outFirst() != 3) {
                        this.numTichusStopped--;
                    }
                }
            } else {
                this.totalPoints -= hand.getTotalScore2();
                if (hand.getCardScore2() == 200) {
                    this.cardPoints -= 100;
                    this.numDoubleWins--;
                } else {
                    this.cardPoints -= hand.getCardScore2();
                }
                if (hand.isTichuFor(seat)) {
                    this.tichuEfficiencyPoints -= hand.getTichuScore2();
                    this.tichuEfficiencyHands--;
                }
                if (hand.isTichuFor(0) || hand.isGrandTichuFor(0)) {
                    this.numTichusCalledByOpps--;
                    if (hand.outFirst() != 0) {
                        this.numTichusStopped--;
                    }
                }
                if (hand.isTichuFor(2) || hand.isGrandTichuFor(2)) {
                    this.numTichusCalledByOpps--;
                    if (hand.outFirst() != 2) {
                        this.numTichusStopped--;
                    }
                }
            }
            if (numTichusThisHand == 0 && hand.outFirst() == seat) {
                this.tichuEfficiencyHands--;
            }
            if (hand.isTichuFor(seat)) {
                this.numTichuCalled--;
                if (hand.outFirst() == seat) {
                    this.numTichuMade--;
                }
            }
            if (hand.isGrandTichuFor(seat)) {
                this.numGTCalled--;
                if (hand.outFirst() == seat) {
                    this.numGTMade--;
                }
            }
            int partner = (seat + 2) % 4;
            if (hand.isTichuFor(partner)) {
                this.numTichusCalledByPartner--;
                if (hand.outFirst() == partner) {
                    this.numTichusMadeByPartner--;
                }
            }
        }
    }

    public void clearStats() {
        this.numHands = 0;
        this.numWins = 0;
        this.numGames = 0;
        this.numTichuMade = 0;
        this.numTichuCalled = 0;
        this.numDoubleWins = 0;
        this.cardPoints = 0;
        this.totalPoints = 0;
        this.tichuEfficiencyHands = 0;
        this.tichuEfficiencyPoints = 0;
        this.numGTMade = 0;
        this.numGTCalled = 0;
        this.numTichusCalledByOpps = 0;
        this.numTichusStopped = 0;
        this.numTichusMadeByPartner = 0;
        this.numTichusCalledByPartner = 0;
    }

    @Override // java.io.Externalizable
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int rev = in.readInt();
        if (rev > 1) {
            throw new IOException("Game newer than software");
        }
        this.name = (String) in.readObject();
        this.numGames = in.readInt();
        this.numWins = in.readInt();
        this.numHands = in.readInt();
        this.totalPoints = in.readInt();
        this.cardPoints = in.readInt();
        this.numDoubleWins = in.readInt();
        this.numTichuCalled = in.readInt();
        this.numTichuMade = in.readInt();
        this.numGTCalled = in.readInt();
        this.numGTMade = in.readInt();
        this.tichuEfficiencyPoints = in.readInt();
        this.tichuEfficiencyHands = in.readInt();
        this.numTichusStopped = in.readInt();
        this.numTichusCalledByOpps = in.readInt();
        this.numTichusCalledByPartner = in.readInt();
        this.numTichusMadeByPartner = in.readInt();
    }

    @Override // java.io.Externalizable
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(1);
        out.writeObject(this.name);
        out.writeInt(this.numGames);
        out.writeInt(this.numWins);
        out.writeInt(this.numHands);
        out.writeInt(this.totalPoints);
        out.writeInt(this.cardPoints);
        out.writeInt(this.numDoubleWins);
        out.writeInt(this.numTichuCalled);
        out.writeInt(this.numTichuMade);
        out.writeInt(this.numGTCalled);
        out.writeInt(this.numGTMade);
        out.writeInt(this.tichuEfficiencyPoints);
        out.writeInt(this.tichuEfficiencyHands);
        out.writeInt(this.numTichusStopped);
        out.writeInt(this.numTichusCalledByOpps);
        out.writeInt(this.numTichusCalledByPartner);
        out.writeInt(this.numTichusMadeByPartner);
    }

    public static String getCSVHeader() {
        return "Name,NumGames,NumWins,NumHands,TotalPts,CardPts,NumDW,NumTCalled,NumTMade,NumGTCalled,NumGTMade,TEffPts,TEffHands,NumTStopped,NumTCalledOpps,NumTCalledPartner,NumTMadePartner";
    }

    public String toCSVString() {
        return String.valueOf(this.name) + "," + this.numGames + "," + this.numWins + "," + this.numHands + "," + this.totalPoints + "," + this.cardPoints + "," + this.numDoubleWins + "," + this.numTichuCalled + "," + this.numTichuMade + "," + this.numGTCalled + "," + this.numGTMade + "," + this.tichuEfficiencyPoints + "," + this.tichuEfficiencyHands + "," + this.numTichusStopped + "," + this.numTichusCalledByOpps + "," + this.numTichusCalledByPartner + "," + this.numTichusMadeByPartner;
    }

    public double getWinPct() {
        if (this.numGames == 0) {
            return 0.0d;
        }
        return (100.0d * this.numWins) / this.numGames;
    }

    public double getPtsPerHand() {
        if (this.numHands == 0) {
            return 0.0d;
        }
        return (double) this.totalPoints / this.numHands;
    }

    public double getCardPtsPerHand() {
        if (this.numHands == 0) {
            return 0.0d;
        }
        return (double) this.cardPoints / this.numHands;
    }

    public double getTichuPct() {
        if (this.numTichuCalled == 0) {
            return 0.0d;
        }
        return (100.0d * this.numTichuMade) / this.numTichuCalled;
    }

    public double getGTPct() {
        if (this.numGTCalled == 0) {
            return 0.0d;
        }
        return (100.0d * this.numGTMade) / this.numGTCalled;
    }

    public double getTichuEfficiency() {
        if (this.tichuEfficiencyHands == 0) {
            return 0.0d;
        }
        return (double) this.tichuEfficiencyPoints / this.tichuEfficiencyHands;
    }

    public double getHandsPerDW() {
        if (this.numDoubleWins == 0) {
            return 1000.0d;
        }
        return (double) this.numHands / this.numDoubleWins;
    }

    public int nonCalls() {
        return this.tichuEfficiencyHands - this.numTichuCalled;
    }

    public double getTichuStopPct() {
        if (this.numTichusCalledByOpps == 0) {
            return 0.0d;
        }
        return (100.0d * this.numTichusStopped) / this.numTichusCalledByOpps;
    }

    public double getPartnerTichuPct() {
        if (this.numTichusCalledByPartner == 0) {
            return 0.0d;
        }
        return (100.0d * this.numTichusMadeByPartner) / this.numTichusCalledByPartner;
    }

    public String getName() {
        return this.name;
    }

    public boolean equals(Object o) {
        Player p = (Player) o;
        return this.name.equals(p.name);
    }

    @Override // java.lang.Comparable
    public int compareTo(Player another) {
        return this.name.compareTo(another.name);
    }

    public int getNumGames() {
        return this.numGames;
    }

    public int getNumHands() {
        return this.numHands;
    }

    public int getNumDoubleWins() {
        return this.numDoubleWins;
    }

    public int getNumTichuCalled() {
        return this.numTichuCalled;
    }

    public int getNumTichuMade() {
        return this.numTichuMade;
    }

    public int getNumGTCalled() {
        return this.numGTCalled;
    }

    public int getNumGTMade() {
        return this.numGTMade;
    }

    public int getNumTichusStopped() {
        return this.numTichusStopped;
    }

    public void setNumTichusStopped(int numTichusStopped) {
        this.numTichusStopped = numTichusStopped;
    }

    public int getNumWins() {
        return this.numWins;
    }

    public int getTichuEfficiencyHands() {
        return this.tichuEfficiencyHands;
    }

    public int getNumTichusCalledByOpps() {
        return this.numTichusCalledByOpps;
    }

    public int getNumTichusCalledByPartner() {
        return this.numTichusCalledByPartner;
    }

    public int getNumTichusMadeByPartner() {
        return this.numTichusMadeByPartner;
    }

    public int getTotalPoints() { return this.totalPoints; }
    public int getCardPoints() { return this.cardPoints; }
    public int getTichuEfficiencyPoints() { return this.tichuEfficiencyPoints; }

    public void setNumGames(int v) { this.numGames = v; }
    public void setNumWins(int v) { this.numWins = v; }
    public void setNumHands(int v) { this.numHands = v; }
    public void setTotalPoints(int v) { this.totalPoints = v; }
    public void setCardPoints(int v) { this.cardPoints = v; }
    public void setNumDoubleWins(int v) { this.numDoubleWins = v; }
    public void setNumTichuCalled(int v) { this.numTichuCalled = v; }
    public void setNumTichuMade(int v) { this.numTichuMade = v; }
    public void setNumGTCalled(int v) { this.numGTCalled = v; }
    public void setNumGTMade(int v) { this.numGTMade = v; }
    public void setTichuEfficiencyPoints(int v) { this.tichuEfficiencyPoints = v; }
    public void setTichuEfficiencyHands(int v) { this.tichuEfficiencyHands = v; }
    public void setNumTichusCalledByOpps(int v) { this.numTichusCalledByOpps = v; }
    public void setNumTichusCalledByPartner(int v) { this.numTichusCalledByPartner = v; }
    public void setNumTichusMadeByPartner(int v) { this.numTichusMadeByPartner = v; }
    public long getDbId() { return dbId; }
    public void setDbId(long dbId) { this.dbId = dbId; }
}
