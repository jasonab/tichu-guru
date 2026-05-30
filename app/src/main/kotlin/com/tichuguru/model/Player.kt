package com.tichuguru.model

internal fun isTeamOne(seat: Int) = seat % 2 == 0

class Player(var name: String = "") : Comparable<Player> {
    var dbId: Long = 0
    var numGames: Int = 0
    var numWins: Int = 0
    var numHands: Int = 0
    var totalPoints: Int = 0
    var cardPoints: Int = 0
    var numDoubleWins: Int = 0
    var numTichuCalled: Int = 0
    var numTichuMade: Int = 0
    var numGTCalled: Int = 0
    var numGTMade: Int = 0
    var tichuEfficiencyPoints: Int = 0
    var tichuEfficiencyHands: Int = 0
    var numTichusStopped: Int = 0
    var numTichusCalledByOpps: Int = 0
    var numTichusCalledByPartner: Int = 0
    var numTichusMadeByPartner: Int = 0

    fun recordGame(
        game: Game,
        seat: Int,
    ) {
        numGames++
        if (isTeamOne(seat)) {
            if (game.teamOneTotal > game.teamTwoTotal) numWins++
        } else if (game.teamOneTotal < game.teamTwoTotal) {
            numWins++
        }
    }

    fun unrecordGame(
        game: Game,
        seat: Int,
    ) {
        numGames--
        if (isTeamOne(seat)) {
            if (game.teamOneTotal > game.teamTwoTotal) numWins--
        } else if (game.teamOneTotal < game.teamTwoTotal) {
            numWins--
        }
    }

    fun recordHand(
        hand: Hand,
        seat: Int,
        addOnFailure: Boolean = false,
    ) {
        numHands++
        adjustHandStats(hand, seat, addOnFailure, +1)
    }

    fun unrecordHand(
        hand: Hand,
        seat: Int,
        addOnFailure: Boolean = false,
    ) {
        if (numHands == 0) return
        numHands--
        adjustHandStats(hand, seat, addOnFailure, -1)
    }

    private fun adjustHandStats(
        hand: Hand,
        seat: Int,
        addOnFailure: Boolean,
        sign: Int,
    ) {
        val numTichusThisHand = (0..3).count { hand.isTichuFor(it) || hand.isGrandTichuFor(it) }
        if (isTeamOne(seat)) {
            totalPoints += sign * hand.totalScoreTeamOne(addOnFailure)
            if (hand.cardScoreTeamOne == 200) {
                cardPoints += sign * 100
                numDoubleWins += sign
            } else {
                cardPoints += sign * hand.cardScoreTeamOne
            }
            if (hand.isTichuFor(1) || hand.isGrandTichuFor(1)) {
                numTichusCalledByOpps += sign
                if (hand.playerOutFirst != 1) numTichusStopped += sign
            }
            if (hand.isTichuFor(3) || hand.isGrandTichuFor(3)) {
                numTichusCalledByOpps += sign
                if (hand.playerOutFirst != 3) numTichusStopped += sign
            }
        } else {
            totalPoints += sign * hand.totalScoreTeamTwo(addOnFailure)
            if (hand.cardScoreTeamTwo == 200) {
                cardPoints += sign * 100
                numDoubleWins += sign
            } else {
                cardPoints += sign * hand.cardScoreTeamTwo
            }
            if (hand.isTichuFor(0) || hand.isGrandTichuFor(0)) {
                numTichusCalledByOpps += sign
                if (hand.playerOutFirst != 0) numTichusStopped += sign
            }
            if (hand.isTichuFor(2) || hand.isGrandTichuFor(2)) {
                numTichusCalledByOpps += sign
                if (hand.playerOutFirst != 2) numTichusStopped += sign
            }
        }
        if (numTichusThisHand == 0 && hand.playerOutFirst == seat) tichuEfficiencyHands += sign
        if (hand.isTichuFor(seat)) {
            numTichuCalled += sign
            if (hand.playerOutFirst == seat) numTichuMade += sign
            tichuEfficiencyPoints += sign * if (seat == hand.playerOutFirst) 100 else -100
            tichuEfficiencyHands += sign
        }
        if (hand.isGrandTichuFor(seat)) {
            numGTCalled += sign
            if (hand.playerOutFirst == seat) numGTMade += sign
        }
        val partner = (seat + 2) % 4
        if (hand.isTichuFor(partner)) {
            numTichusCalledByPartner += sign
            if (hand.playerOutFirst == partner) numTichusMadeByPartner += sign
        }
    }

    fun clearStats() {
        numHands = 0
        numWins = 0
        numGames = 0
        numTichuMade = 0
        numTichuCalled = 0
        numDoubleWins = 0
        cardPoints = 0
        totalPoints = 0
        tichuEfficiencyHands = 0
        tichuEfficiencyPoints = 0
        numGTMade = 0
        numGTCalled = 0
        numTichusCalledByOpps = 0
        numTichusStopped = 0
        numTichusMadeByPartner = 0
        numTichusCalledByPartner = 0
    }

    fun getWinPct() = if (numGames == 0) 0.0 else 100.0 * numWins / numGames

    fun getPtsPerHand() = if (numHands == 0) 0.0 else totalPoints.toDouble() / numHands

    fun getCardPtsPerHand() = if (numHands == 0) 0.0 else cardPoints.toDouble() / numHands

    fun getTichuPct() = if (numTichuCalled == 0) 0.0 else 100.0 * numTichuMade / numTichuCalled

    fun getGTPct() = if (numGTCalled == 0) 0.0 else 100.0 * numGTMade / numGTCalled

    fun getTichuEfficiency() = if (tichuEfficiencyHands == 0) 0.0 else tichuEfficiencyPoints.toDouble() / tichuEfficiencyHands

    fun getHandsPerDW() = if (numDoubleWins == 0) 1000.0 else numHands.toDouble() / numDoubleWins

    fun nonCalls() = tichuEfficiencyHands - numTichuCalled

    fun getTichuStopPct() = if (numTichusCalledByOpps == 0) 0.0 else 100.0 * numTichusStopped / numTichusCalledByOpps

    fun getPartnerTichuPct() = if (numTichusCalledByPartner == 0) 0.0 else 100.0 * numTichusMadeByPartner / numTichusCalledByPartner

    override fun equals(other: Any?): Boolean {
        if (other !is Player) return false
        return name == other.name
    }

    override fun hashCode() = name.hashCode()

    override fun compareTo(other: Player) = name.compareTo(other.name)
}
