package com.tichuguru.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tichuguru.model.Player

@Entity(tableName = "players")
data class PlayerEntity(
    @JvmField @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @JvmField var name: String = "",
    @JvmField var numGames: Int = 0,
    @JvmField var numWins: Int = 0,
    @JvmField var numHands: Int = 0,
    @JvmField var totalPoints: Int = 0,
    @JvmField var cardPoints: Int = 0,
    @JvmField var numDoubleWins: Int = 0,
    @JvmField var numTichuCalled: Int = 0,
    @JvmField var numTichuMade: Int = 0,
    @JvmField var numGTCalled: Int = 0,
    @JvmField var numGTMade: Int = 0,
    @JvmField var tichuEfficiencyPoints: Int = 0,
    @JvmField var tichuEfficiencyHands: Int = 0,
    @JvmField var numTichusStopped: Int = 0,
    @JvmField var numTichusCalledByOpps: Int = 0,
    @JvmField var numTichusCalledByPartner: Int = 0,
    @JvmField var numTichusMadeByPartner: Int = 0
) {
    companion object {
        @JvmStatic
        fun from(p: Player) = PlayerEntity(
            id = p.dbId,
            name = p.name,
            numGames = p.numGames,
            numWins = p.numWins,
            numHands = p.numHands,
            totalPoints = p.totalPoints,
            cardPoints = p.cardPoints,
            numDoubleWins = p.numDoubleWins,
            numTichuCalled = p.numTichuCalled,
            numTichuMade = p.numTichuMade,
            numGTCalled = p.numGTCalled,
            numGTMade = p.numGTMade,
            tichuEfficiencyPoints = p.tichuEfficiencyPoints,
            tichuEfficiencyHands = p.tichuEfficiencyHands,
            numTichusStopped = p.numTichusStopped,
            numTichusCalledByOpps = p.numTichusCalledByOpps,
            numTichusCalledByPartner = p.numTichusCalledByPartner,
            numTichusMadeByPartner = p.numTichusMadeByPartner
        )
    }

    fun toPlayer(): Player {
        val p = Player(name)
        p.dbId = id
        p.numGames = numGames
        p.numWins = numWins
        p.numHands = numHands
        p.totalPoints = totalPoints
        p.cardPoints = cardPoints
        p.numDoubleWins = numDoubleWins
        p.numTichuCalled = numTichuCalled
        p.numTichuMade = numTichuMade
        p.numGTCalled = numGTCalled
        p.numGTMade = numGTMade
        p.tichuEfficiencyPoints = tichuEfficiencyPoints
        p.tichuEfficiencyHands = tichuEfficiencyHands
        p.numTichusStopped = numTichusStopped
        p.numTichusCalledByOpps = numTichusCalledByOpps
        p.numTichusCalledByPartner = numTichusCalledByPartner
        p.numTichusMadeByPartner = numTichusMadeByPartner
        return p
    }
}
