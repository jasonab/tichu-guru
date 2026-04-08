package com.tichuguru.model

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

    fun recordGame(game: Game, seat: Int) {
        numGames++
        if (seat == 0 || seat == 2) {
            if (game.score1 > game.score2) numWins++
        } else if (game.score1 < game.score2) {
            numWins++
        }
    }

    fun unrecordGame(game: Game, seat: Int) {
        numGames--
        if (seat == 0 || seat == 2) {
            if (game.score1 > game.score2) numWins--
        } else if (game.score1 < game.score2) {
            numWins--
        }
    }

    fun recordHand(hand: Hand, seat: Int) {
        numHands++
        val numTichusThisHand = (0..3).count { hand.isTichuFor(it) || hand.isGrandTichuFor(it) }
        if (seat == 0 || seat == 2) {
            totalPoints += hand.totalScore1
            if (hand.cardScore1 == 200) { cardPoints += 100; numDoubleWins++ }
            else cardPoints += hand.cardScore1
            if (hand.isTichuFor(1) || hand.isGrandTichuFor(1)) {
                numTichusCalledByOpps++
                if (hand.outFirst() != 1) numTichusStopped++
            }
            if (hand.isTichuFor(3) || hand.isGrandTichuFor(3)) {
                numTichusCalledByOpps++
                if (hand.outFirst() != 3) numTichusStopped++
            }
        } else {
            totalPoints += hand.totalScore2
            if (hand.cardScore2 == 200) { cardPoints += 100; numDoubleWins++ }
            else cardPoints += hand.cardScore2
            if (hand.isTichuFor(0) || hand.isGrandTichuFor(0)) {
                numTichusCalledByOpps++
                if (hand.outFirst() != 0) numTichusStopped++
            }
            if (hand.isTichuFor(2) || hand.isGrandTichuFor(2)) {
                numTichusCalledByOpps++
                if (hand.outFirst() != 2) numTichusStopped++
            }
        }
        if (numTichusThisHand == 0 && hand.outFirst() == seat) tichuEfficiencyHands++
        if (hand.isTichuFor(seat)) {
            numTichuCalled++
            if (hand.outFirst() == seat) numTichuMade++
            tichuEfficiencyPoints += if (seat == hand.outFirst()) 100 else -100
            tichuEfficiencyHands++
        }
        if (hand.isGrandTichuFor(seat)) {
            numGTCalled++
            if (hand.outFirst() == seat) numGTMade++
        }
        val partner = (seat + 2) % 4
        if (hand.isTichuFor(partner)) {
            numTichusCalledByPartner++
            if (hand.outFirst() == partner) numTichusMadeByPartner++
        }
    }

    fun unrecordHand(hand: Hand, seat: Int) {
        if (numHands == 0) return
        numHands--
        val numTichusThisHand = (0..3).count { hand.isTichuFor(it) || hand.isGrandTichuFor(it) }
        if (seat == 0 || seat == 2) {
            totalPoints -= hand.totalScore1
            if (hand.cardScore1 == 200) { cardPoints -= 100; numDoubleWins-- }
            else cardPoints -= hand.cardScore1
            if (hand.isTichuFor(seat)) {
                tichuEfficiencyPoints -= if (seat == hand.outFirst()) 100 else -100
                tichuEfficiencyHands--
            }
            if (hand.isTichuFor(1) || hand.isGrandTichuFor(1)) {
                numTichusCalledByOpps--
                if (hand.outFirst() != 1) numTichusStopped--
            }
            if (hand.isTichuFor(3) || hand.isGrandTichuFor(3)) {
                numTichusCalledByOpps--
                if (hand.outFirst() != 3) numTichusStopped--
            }
        } else {
            totalPoints -= hand.totalScore2
            if (hand.cardScore2 == 200) { cardPoints -= 100; numDoubleWins-- }
            else cardPoints -= hand.cardScore2
            if (hand.isTichuFor(seat)) {
                tichuEfficiencyPoints -= if (seat == hand.outFirst()) 100 else -100
                tichuEfficiencyHands--
            }
            if (hand.isTichuFor(0) || hand.isGrandTichuFor(0)) {
                numTichusCalledByOpps--
                if (hand.outFirst() != 0) numTichusStopped--
            }
            if (hand.isTichuFor(2) || hand.isGrandTichuFor(2)) {
                numTichusCalledByOpps--
                if (hand.outFirst() != 2) numTichusStopped--
            }
        }
        if (numTichusThisHand == 0 && hand.outFirst() == seat) tichuEfficiencyHands--
        if (hand.isTichuFor(seat)) {
            numTichuCalled--
            if (hand.outFirst() == seat) numTichuMade--
        }
        if (hand.isGrandTichuFor(seat)) {
            numGTCalled--
            if (hand.outFirst() == seat) numGTMade--
        }
        val partner = (seat + 2) % 4
        if (hand.isTichuFor(partner)) {
            numTichusCalledByPartner--
            if (hand.outFirst() == partner) numTichusMadeByPartner--
        }
    }

    fun clearStats() {
        numHands = 0; numWins = 0; numGames = 0
        numTichuMade = 0; numTichuCalled = 0; numDoubleWins = 0
        cardPoints = 0; totalPoints = 0
        tichuEfficiencyHands = 0; tichuEfficiencyPoints = 0
        numGTMade = 0; numGTCalled = 0
        numTichusCalledByOpps = 0; numTichusStopped = 0
        numTichusMadeByPartner = 0; numTichusCalledByPartner = 0
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
