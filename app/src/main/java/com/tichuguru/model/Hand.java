package com.tichuguru.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/* loaded from: classes.dex */
public class Hand implements Externalizable {
    private static final int REVISION = 1;
    public static final long serialVersionUID = 1;
    private boolean addOnFailure;
    private int cardScore1;
    private int cardScore2;
    private boolean[] grandTichu;
    private int outFirst;
    private boolean[] tichu;
    private int tichuScore1;
    private int tichuScore2;
    private int totalScore1;
    private int totalScore2;

    public Hand() {
    }

    public Hand(Game game) {
        this.addOnFailure = game.isAddOnFailure();
        this.tichu = new boolean[4];
        this.grandTichu = new boolean[4];
    }

    public void setCardScore1(int score) {
        this.cardScore1 = score;
        this.totalScore1 = this.cardScore1 + this.tichuScore1;
    }

    public void setCardScore2(int score) {
        this.cardScore2 = score;
        this.totalScore2 = this.cardScore2 + this.tichuScore2;
    }

    public void setTichuFor(int player) {
        this.tichu[player] = true;
    }

    public void setGrandTichuFor(int player) {
        this.grandTichu[player] = true;
    }

    public boolean isTichuFor(int player) {
        return this.tichu[player];
    }

    public boolean isGrandTichuFor(int player) {
        return this.grandTichu[player];
    }

    public int outFirst() {
        return this.outFirst;
    }

    public void setOutFirst(int player) {
        this.outFirst = player;
        this.tichuScore2 = 0;
        this.tichuScore1 = 0;
        if (this.addOnFailure) {
            if (this.tichu[0]) {
                if (this.outFirst == 0) {
                    this.tichuScore1 += 100;
                } else {
                    this.tichuScore2 += 100;
                }
            }
            if (this.tichu[2]) {
                if (this.outFirst == 2) {
                    this.tichuScore1 += 100;
                } else {
                    this.tichuScore2 += 100;
                }
            }
            if (this.tichu[1]) {
                if (this.outFirst == 1) {
                    this.tichuScore2 += 100;
                } else {
                    this.tichuScore1 += 100;
                }
            }
            if (this.tichu[3]) {
                if (this.outFirst == 3) {
                    this.tichuScore2 += 100;
                } else {
                    this.tichuScore1 += 100;
                }
            }
            if (this.grandTichu[0]) {
                if (this.outFirst == 0) {
                    this.tichuScore1 += 200;
                } else {
                    this.tichuScore2 += 200;
                }
            }
            if (this.grandTichu[2]) {
                if (this.outFirst == 2) {
                    this.tichuScore1 += 200;
                } else {
                    this.tichuScore2 += 200;
                }
            }
            if (this.grandTichu[1]) {
                if (this.outFirst == 1) {
                    this.tichuScore2 += 200;
                } else {
                    this.tichuScore1 += 200;
                }
            }
            if (this.grandTichu[3]) {
                if (this.outFirst == 3) {
                    this.tichuScore2 += 200;
                } else {
                    this.tichuScore1 += 200;
                }
            }
        } else {
            if (this.tichu[0]) {
                this.tichuScore1 = (this.outFirst == 0 ? 100 : -100) + this.tichuScore1;
            }
            if (this.tichu[2]) {
                this.tichuScore1 = (this.outFirst == 2 ? 100 : -100) + this.tichuScore1;
            }
            if (this.tichu[1]) {
                this.tichuScore2 = (this.outFirst == 1 ? 100 : -100) + this.tichuScore2;
            }
            if (this.tichu[3]) {
                this.tichuScore2 = (this.outFirst == 3 ? 100 : -100) + this.tichuScore2;
            }
            if (this.grandTichu[0]) {
                this.tichuScore1 = (this.outFirst == 0 ? 200 : -200) + this.tichuScore1;
            }
            if (this.grandTichu[2]) {
                this.tichuScore1 = (this.outFirst == 2 ? 200 : -200) + this.tichuScore1;
            }
            if (this.grandTichu[1]) {
                this.tichuScore2 = (this.outFirst == 1 ? 200 : -200) + this.tichuScore2;
            }
            if (this.grandTichu[3]) {
                this.tichuScore2 += this.outFirst == 3 ? 200 : -200;
            }
        }
        this.totalScore1 = this.cardScore1 + this.tichuScore1;
        this.totalScore2 = this.cardScore2 + this.tichuScore2;
    }

    @Override // java.io.Externalizable
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int rev = in.readInt();
        if (rev > 1) {
            throw new IOException("Game newer than software");
        }
        this.addOnFailure = in.readBoolean();
        this.totalScore1 = in.readInt();
        this.totalScore2 = in.readInt();
        this.tichuScore1 = in.readInt();
        this.tichuScore2 = in.readInt();
        this.cardScore1 = in.readInt();
        this.cardScore2 = in.readInt();
        this.outFirst = in.readInt();
        this.tichu = new boolean[4];
        this.grandTichu = new boolean[4];
        for (int i = 0; i < 4; i++) {
            this.tichu[i] = in.readBoolean();
            this.grandTichu[i] = in.readBoolean();
        }
    }

    @Override // java.io.Externalizable
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(1);
        out.writeBoolean(this.addOnFailure);
        out.writeInt(this.totalScore1);
        out.writeInt(this.totalScore2);
        out.writeInt(this.tichuScore1);
        out.writeInt(this.tichuScore2);
        out.writeInt(this.cardScore1);
        out.writeInt(this.cardScore2);
        out.writeInt(this.outFirst);
        for (int i = 0; i < 4; i++) {
            out.writeBoolean(this.tichu[i]);
            out.writeBoolean(this.grandTichu[i]);
        }
    }

    public int getTotalScore1() {
        return this.totalScore1;
    }

    public void setTotalScore1(int totalScore1) {
        this.totalScore1 = totalScore1;
    }

    public int getTotalScore2() {
        return this.totalScore2;
    }

    public void setTotalScore2(int totalScore2) {
        this.totalScore2 = totalScore2;
    }

    public int getTichuScore1() {
        return this.tichuScore1;
    }

    public void setTichuScore1(int tichuScore1) {
        this.tichuScore1 = tichuScore1;
    }

    public int getTichuScore2() {
        return this.tichuScore2;
    }

    public void setTichuScore2(int tichuScore2) {
        this.tichuScore2 = tichuScore2;
    }

    public int getCardScore1() {
        return this.cardScore1;
    }

    public int getCardScore2() {
        return this.cardScore2;
    }

    public boolean isAddOnFailure() { return this.addOnFailure; }
    public void setAddOnFailure(boolean v) { this.addOnFailure = v; }

    /** Set cardScore1 without recalculating totals (use when loading persisted data). */
    public void setCardScore1Direct(int v) { this.cardScore1 = v; }
    /** Set cardScore2 without recalculating totals (use when loading persisted data). */
    public void setCardScore2Direct(int v) { this.cardScore2 = v; }
    /** Set outFirst without recalculating tichu scores (use when loading persisted data). */
    public void setOutFirstDirect(int v) { this.outFirst = v; }
    /** Set tichu flag directly without recalculating scores (use when loading persisted data). */
    public void setTichuDirect(int player, boolean v) {
        if (this.tichu == null) this.tichu = new boolean[4];
        this.tichu[player] = v;
    }
    /** Set grand tichu flag directly without recalculating scores (use when loading persisted data). */
    public void setGrandTichuDirect(int player, boolean v) {
        if (this.grandTichu == null) this.grandTichu = new boolean[4];
        this.grandTichu[player] = v;
    }
}
