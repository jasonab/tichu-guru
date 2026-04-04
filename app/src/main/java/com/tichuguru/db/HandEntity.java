package com.tichuguru.db;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.tichuguru.model.Hand;

@Entity(
    tableName = "hands",
    foreignKeys = @ForeignKey(
        entity = GameEntity.class,
        parentColumns = "id",
        childColumns = "gameId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("gameId")
)
public class HandEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long gameId;
    public int handOrder;
    public boolean addOnFailure;
    public int cardScore1;
    public int cardScore2;
    public int totalScore1;
    public int totalScore2;
    public int tichuScore1;
    public int tichuScore2;
    public int outFirst;
    public boolean tichu0;
    public boolean tichu1;
    public boolean tichu2;
    public boolean tichu3;
    public boolean grandTichu0;
    public boolean grandTichu1;
    public boolean grandTichu2;
    public boolean grandTichu3;

    public static HandEntity from(Hand h, long gameId, int order) {
        HandEntity e = new HandEntity();
        e.gameId = gameId;
        e.handOrder = order;
        e.addOnFailure = h.isAddOnFailure();
        e.cardScore1 = h.getCardScore1();
        e.cardScore2 = h.getCardScore2();
        e.totalScore1 = h.getTotalScore1();
        e.totalScore2 = h.getTotalScore2();
        e.tichuScore1 = h.getTichuScore1();
        e.tichuScore2 = h.getTichuScore2();
        e.outFirst = h.outFirst();
        e.tichu0 = h.isTichuFor(0);
        e.tichu1 = h.isTichuFor(1);
        e.tichu2 = h.isTichuFor(2);
        e.tichu3 = h.isTichuFor(3);
        e.grandTichu0 = h.isGrandTichuFor(0);
        e.grandTichu1 = h.isGrandTichuFor(1);
        e.grandTichu2 = h.isGrandTichuFor(2);
        e.grandTichu3 = h.isGrandTichuFor(3);
        return e;
    }

    public Hand toHand() {
        Hand h = new Hand();
        h.setAddOnFailure(addOnFailure);
        h.setCardScore1Direct(cardScore1);
        h.setCardScore2Direct(cardScore2);
        h.setTotalScore1(totalScore1);
        h.setTotalScore2(totalScore2);
        h.setTichuScore1(tichuScore1);
        h.setTichuScore2(tichuScore2);
        h.setOutFirstDirect(outFirst);
        h.setTichuDirect(0, tichu0);
        h.setTichuDirect(1, tichu1);
        h.setTichuDirect(2, tichu2);
        h.setTichuDirect(3, tichu3);
        h.setGrandTichuDirect(0, grandTichu0);
        h.setGrandTichuDirect(1, grandTichu1);
        h.setGrandTichuDirect(2, grandTichu2);
        h.setGrandTichuDirect(3, grandTichu3);
        return h;
    }
}
