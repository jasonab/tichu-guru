package com.tichuguru.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tichuguru.model.Hand

@Entity(
    tableName = "hands",
    foreignKeys = [ForeignKey(
        entity = GameEntity::class,
        parentColumns = ["id"],
        childColumns = ["gameId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("gameId")]
)
data class HandEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var gameId: Long = 0,
    var handOrder: Int = 0,
    var addOnFailure: Boolean = false,
    var cardScore1: Int = 0,
    var cardScore2: Int = 0,
    var totalScore1: Int = 0,
    var totalScore2: Int = 0,
    var tichuScore1: Int = 0,
    var tichuScore2: Int = 0,
    var outFirst: Int = 0,
    var tichu0: Boolean = false,
    var tichu1: Boolean = false,
    var tichu2: Boolean = false,
    var tichu3: Boolean = false,
    var grandTichu0: Boolean = false,
    var grandTichu1: Boolean = false,
    var grandTichu2: Boolean = false,
    var grandTichu3: Boolean = false
) {
    companion object {
        fun from(h: Hand, gameId: Long, order: Int) = HandEntity(
            id = h.dbId,
            gameId = gameId,
            handOrder = order,
            addOnFailure = h.isAddOnFailure,
            cardScore1 = h.cardScore1,
            cardScore2 = h.cardScore2,
            totalScore1 = h.totalScore1,
            totalScore2 = h.totalScore2,
            tichuScore1 = h.tichuScore1,
            tichuScore2 = h.tichuScore2,
            outFirst = h.outFirst(),
            tichu0 = h.isTichuFor(0),
            tichu1 = h.isTichuFor(1),
            tichu2 = h.isTichuFor(2),
            tichu3 = h.isTichuFor(3),
            grandTichu0 = h.isGrandTichuFor(0),
            grandTichu1 = h.isGrandTichuFor(1),
            grandTichu2 = h.isGrandTichuFor(2),
            grandTichu3 = h.isGrandTichuFor(3)
        )
    }

    fun toHand(): Hand {
        val h = Hand()
        h.dbId = id
        h.isAddOnFailure = addOnFailure
        h.setCardScore1Direct(cardScore1)
        h.setCardScore2Direct(cardScore2)
        h.totalScore1 = totalScore1
        h.totalScore2 = totalScore2
        h.tichuScore1 = tichuScore1
        h.tichuScore2 = tichuScore2
        h.setOutFirstDirect(outFirst)
        h.setTichuDirect(0, tichu0)
        h.setTichuDirect(1, tichu1)
        h.setTichuDirect(2, tichu2)
        h.setTichuDirect(3, tichu3)
        h.setGrandTichuDirect(0, grandTichu0)
        h.setGrandTichuDirect(1, grandTichu1)
        h.setGrandTichuDirect(2, grandTichu2)
        h.setGrandTichuDirect(3, grandTichu3)
        return h
    }
}
