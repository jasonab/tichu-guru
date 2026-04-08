package com.tichuguru.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tichuguru.model.Player

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String = "",
    var numGames: Int = 0,
    var numWins: Int = 0,
    var numHands: Int = 0,
    var totalPoints: Int = 0,
    var cardPoints: Int = 0,
    var numDoubleWins: Int = 0,
    var numTichuCalled: Int = 0,
    var numTichuMade: Int = 0,
    var numGTCalled: Int = 0,
    var numGTMade: Int = 0,
    var tichuEfficiencyPoints: Int = 0,
    var tichuEfficiencyHands: Int = 0,
    var numTichusStopped: Int = 0,
    var numTichusCalledByOpps: Int = 0,
    var numTichusCalledByPartner: Int = 0,
    var numTichusMadeByPartner: Int = 0
) {
    companion object {
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
