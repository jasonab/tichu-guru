package com.tichuguru.model

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PlayerTest {
    private lateinit var player: Player

    @Before fun setup() {
        player = Player("Alice")
    }

    // Helpers

    /** Build a fully scored hand with no bids. */
    private fun plainHand(
        cardScore1: Int,
        outFirst: Int = 0,
    ): Hand {
        val h = Hand()
        h.cardScoreTeamOne = cardScore1
        h.cardScoreTeamTwo = Hand.otherCardScore(cardScore1)
        h.playerOutFirst = outFirst
        return h
    }

    private fun game(
        score1: Int = 1000,
        score2: Int = 500,
    ): Game {
        val players = listOf(Player("A"), Player("B"), Player("C"), Player("D"))
        return Game(
            players = players.toMutableList(),
            score1 = score1,
            score2 = score2,
            gameLimit = 1000,
            gameOver = true
        )
    }

    // --- recordHand: card points ---

    @Test fun recordHand_team1Seat_addsCardScore1() {
        player.recordHand(plainHand(60), seat = 0)
        assertEquals(60, player.cardPoints)
        assertEquals(60, player.totalPoints)
        assertEquals(1, player.numHands)
    }

    @Test fun recordHand_team2Seat_addsCardScore2() {
        player.recordHand(plainHand(60), seat = 1) // cardScore2 = 40
        assertEquals(40, player.cardPoints)
        assertEquals(40, player.totalPoints)
    }

    @Test fun recordHand_seat2_isTeam1() {
        player.recordHand(plainHand(70), seat = 2)
        assertEquals(70, player.cardPoints)
    }

    @Test fun recordHand_seat3_isTeam2() {
        player.recordHand(plainHand(70), seat = 3)
        assertEquals(30, player.cardPoints)
    }

    // --- double win ---

    @Test fun recordHand_doubleWin_cardPointsCountAs100() {
        val h = Hand()
        h.cardScoreTeamOne = 200
        h.cardScoreTeamTwo = 0
        h.playerOutFirst = 0
        player.recordHand(h, seat = 0)
        assertEquals(100, player.cardPoints)
        assertEquals(1, player.numDoubleWins)
    }

    @Test fun recordHand_doubleWin_team2_cardPointsCountAs100() {
        val h = Hand()
        h.cardScoreTeamOne = 200
        h.cardScoreTeamTwo = 0
        h.playerOutFirst = 0
        player.recordHand(h, seat = 1) // team2 got 0 card points (double-win by team1)
        assertEquals(0, player.cardPoints)
        assertEquals(0, player.numDoubleWins)
    }

    @Test fun recordHand_doubleWinByTeam2() {
        val h = Hand()
        h.cardScoreTeamOne = 0
        h.cardScoreTeamTwo = 200
        h.playerOutFirst = 1
        player.recordHand(h, seat = 1)
        assertEquals(100, player.cardPoints)
        assertEquals(200, player.totalPoints)
        assertEquals(1, player.numDoubleWins)
    }

    // --- tichu called and made ---

    @Test fun recordHand_tichuCalledAndMade() {
        val h = Hand()
        h.setTichuFor(0)
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 0
        player.recordHand(h, seat = 0)
        assertEquals(1, player.numTichuCalled)
        assertEquals(1, player.numTichuMade)
        assertEquals(100, player.tichuEfficiencyPoints)
        assertEquals(1, player.tichuEfficiencyHands)
    }

    @Test fun recordHand_tichuCalledAndFailed() {
        val h = Hand()
        h.setTichuFor(0)
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 1 // player 1 wins
        player.recordHand(h, seat = 0)
        assertEquals(1, player.numTichuCalled)
        assertEquals(0, player.numTichuMade)
        assertEquals(-100, player.tichuEfficiencyPoints)
        assertEquals(1, player.tichuEfficiencyHands)
    }

    // --- grand tichu ---

    @Test fun recordHand_grandTichuCalledAndMade() {
        val h = Hand()
        h.setGrandTichuFor(0)
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 0
        player.recordHand(h, seat = 0)
        assertEquals(1, player.numGTCalled)
        assertEquals(1, player.numGTMade)
    }

    @Test fun recordHand_grandTichuCalledAndFailed() {
        val h = Hand()
        h.setGrandTichuFor(0)
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 1
        player.recordHand(h, seat = 0)
        assertEquals(1, player.numGTCalled)
        assertEquals(0, player.numGTMade)
    }

    // --- tichuEfficiencyHands: going out first with no bids ---

    @Test fun recordHand_noTichuOutFirst_incrementsEfficiencyHands() {
        player.recordHand(plainHand(60, outFirst = 0), seat = 0)
        assertEquals(1, player.tichuEfficiencyHands)
        assertEquals(0, player.tichuEfficiencyPoints)
    }

    @Test fun recordHand_noTichuNotOutFirst_doesNotIncrementEfficiencyHands() {
        player.recordHand(plainHand(60, outFirst = 1), seat = 0)
        assertEquals(0, player.tichuEfficiencyHands)
    }

    @Test fun recordHand_tichuCalledSoEfficiencyHandsNotDoubled() {
        // When tichu is called, the "no-bid going out first" bonus does NOT apply
        val h = Hand()
        h.setTichuFor(0)
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 0
        player.recordHand(h, seat = 0)
        // Only 1 efficiency hand (from the tichu call), not 2
        assertEquals(1, player.tichuEfficiencyHands)
    }

    // --- opponent tichu tracking ---

    @Test fun recordHand_opponentTichuFailed_incrementsStopped() {
        val h = Hand()
        h.setTichuFor(1) // opponent (seat 1) called tichu
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 0 // team1 wins → opponent tichu stopped
        player.recordHand(h, seat = 0)
        assertEquals(1, player.numTichusCalledByOpps)
        assertEquals(1, player.numTichusStopped)
    }

    @Test fun recordHand_opponentTichuSucceeded_notStopped() {
        val h = Hand()
        h.setTichuFor(1) // opponent (seat 1) called tichu and made it
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 1
        player.recordHand(h, seat = 0)
        assertEquals(1, player.numTichusCalledByOpps)
        assertEquals(0, player.numTichusStopped)
    }

    @Test fun recordHand_opponentSeat3Tichu() {
        val h = Hand()
        h.setTichuFor(3) // opponent seat 3 for a seat-2 player
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 2 // seat 2 wins → opp tichu stopped
        player.recordHand(h, seat = 2)
        assertEquals(1, player.numTichusCalledByOpps)
        assertEquals(1, player.numTichusStopped)
    }

    // --- partner tichu tracking ---

    @Test fun recordHand_partnerTichuCalledAndMade() {
        val h = Hand()
        h.setTichuFor(2) // partner of seat 0
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 2 // partner wins
        player.recordHand(h, seat = 0)
        assertEquals(1, player.numTichusCalledByPartner)
        assertEquals(1, player.numTichusMadeByPartner)
    }

    @Test fun recordHand_partnerTichuCalledAndFailed() {
        val h = Hand()
        h.setTichuFor(2) // partner of seat 0
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 0 // seat 0 wins, not partner
        player.recordHand(h, seat = 0)
        assertEquals(1, player.numTichusCalledByPartner)
        assertEquals(0, player.numTichusMadeByPartner)
    }

    // --- recordGame / unrecordGame ---

    @Test fun recordGame_team1Win_incrementsWin() {
        player.recordGame(game(score1 = 1000, score2 = 700), seat = 0)
        assertEquals(1, player.numGames)
        assertEquals(1, player.numWins)
    }

    @Test fun recordGame_team1Loss_noWin() {
        player.recordGame(game(score1 = 700, score2 = 1000), seat = 0)
        assertEquals(1, player.numGames)
        assertEquals(0, player.numWins)
    }

    @Test fun recordGame_team2Win_seat1() {
        player.recordGame(game(score1 = 700, score2 = 1000), seat = 1)
        assertEquals(1, player.numWins)
    }

    @Test fun recordGame_team2Loss_seat1() {
        player.recordGame(game(score1 = 1000, score2 = 700), seat = 1)
        assertEquals(0, player.numWins)
    }

    @Test fun unrecordGame_revertsWin() {
        val g = game(score1 = 1000, score2 = 700)
        player.recordGame(g, seat = 0)
        player.unrecordGame(g, seat = 0)
        assertEquals(0, player.numGames)
        assertEquals(0, player.numWins)
    }

    // --- unrecordHand is the inverse of recordHand ---

    @Test fun unrecordHand_revertsCardPoints() {
        val h = plainHand(60)
        player.recordHand(h, seat = 0)
        player.unrecordHand(h, seat = 0)
        assertEquals(0, player.cardPoints)
        assertEquals(0, player.numHands)
    }

    @Test fun unrecordHand_revertsTichu() {
        val h = Hand()
        h.setTichuFor(0)
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 0
        player.recordHand(h, seat = 0)
        player.unrecordHand(h, seat = 0)
        assertEquals(0, player.numTichuCalled)
        assertEquals(0, player.numTichuMade)
        assertEquals(0, player.tichuEfficiencyPoints)
        assertEquals(0, player.tichuEfficiencyHands)
    }

    @Test fun unrecordHand_revertsGrandTichu() {
        val h = Hand()
        h.setGrandTichuFor(0)
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 0
        player.recordHand(h, seat = 0)
        player.unrecordHand(h, seat = 0)
        assertEquals(0, player.numGTCalled)
        assertEquals(0, player.numGTMade)
    }

    @Test fun unrecordHand_revertsOpponentTichu() {
        val h = Hand()
        h.setTichuFor(1)
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 0
        player.recordHand(h, seat = 0)
        player.unrecordHand(h, seat = 0)
        assertEquals(0, player.numTichusCalledByOpps)
        assertEquals(0, player.numTichusStopped)
    }

    @Test fun unrecordHand_revertsDoubleWin() {
        val h = Hand()
        h.cardScoreTeamOne = 200
        h.cardScoreTeamTwo = 0
        h.playerOutFirst = 0
        player.recordHand(h, seat = 0)
        player.unrecordHand(h, seat = 0)
        assertEquals(0, player.cardPoints)
        assertEquals(0, player.numDoubleWins)
    }

    @Test fun unrecordHand_onEmptyPlayer_isNoOp() {
        val h = plainHand(60)
        player.unrecordHand(h, seat = 0) // numHands == 0 → returns early
        assertEquals(0, player.numHands)
    }

    // --- clearStats ---

    @Test fun clearStats_resetsAllFields() {
        val h = Hand()
        h.setTichuFor(0)
        h.setGrandTichuFor(2)
        h.cardScoreTeamOne = 60
        h.cardScoreTeamTwo = 40
        h.playerOutFirst = 0
        player.recordHand(h, seat = 0)
        player.recordGame(game(), seat = 0)
        player.clearStats()

        assertEquals(0, player.numHands)
        assertEquals(0, player.numGames)
        assertEquals(0, player.numWins)
        assertEquals(0, player.cardPoints)
        assertEquals(0, player.totalPoints)
        assertEquals(0, player.numTichuCalled)
        assertEquals(0, player.numTichuMade)
        assertEquals(0, player.numGTCalled)
        assertEquals(0, player.numGTMade)
        assertEquals(0, player.numDoubleWins)
        assertEquals(0, player.tichuEfficiencyPoints)
        assertEquals(0, player.tichuEfficiencyHands)
        assertEquals(0, player.numTichusCalledByOpps)
        assertEquals(0, player.numTichusStopped)
        assertEquals(0, player.numTichusCalledByPartner)
        assertEquals(0, player.numTichusMadeByPartner)
    }

    // --- stat helper functions ---

    @Test fun getWinPct_noGames_returnsZero() = assertEquals(0.0, player.getWinPct(), 0.0)

    @Test fun getWinPct_oneWinOneGame_returns100() {
        player.numGames = 1
        player.numWins = 1
        assertEquals(100.0, player.getWinPct(), 0.001)
    }

    @Test fun getPtsPerHand_noHands_returnsZero() = assertEquals(0.0, player.getPtsPerHand(), 0.0)

    @Test fun getPtsPerHand_calculated() {
        player.numHands = 4
        player.totalPoints = 200
        assertEquals(50.0, player.getPtsPerHand(), 0.001)
    }

    @Test fun getTichuPct_noCalls_returnsZero() = assertEquals(0.0, player.getTichuPct(), 0.0)

    @Test fun getTichuPct_calculated() {
        player.numTichuCalled = 4
        player.numTichuMade = 3
        assertEquals(75.0, player.getTichuPct(), 0.001)
    }

    @Test fun getTichuStopPct_noOpponentCalls_returnsZero() = assertEquals(0.0, player.getTichuStopPct(), 0.0)

    @Test fun getTichuStopPct_calculated() {
        player.numTichusCalledByOpps = 5
        player.numTichusStopped = 2
        assertEquals(40.0, player.getTichuStopPct(), 0.001)
    }

    // --- addOnFailure mode (#73) ---

    @Test fun recordHand_addOnFailure_ownTichuFails_noPenalty() {
        val h = Hand()
        h.setTichuFor(0)
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 1 // seat 0 fails tichu
        player.recordHand(h, seat = 0, addOnFailure = true)
        // No -100 penalty: totalScore = 50 (card only)
        assertEquals(50, player.totalPoints)
    }

    @Test fun recordHand_addOnFailure_ownTichuFails_normalModeDeducts() {
        val h = Hand()
        h.setTichuFor(0)
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 1
        player.recordHand(h, seat = 0, addOnFailure = false)
        // Normal mode: 50 (card) - 100 (penalty) = -50
        assertEquals(-50, player.totalPoints)
    }

    @Test fun recordHand_addOnFailure_opponentTichuFails_teamGainsPoints() {
        val h = Hand()
        h.setTichuFor(1) // opponent calls tichu and fails
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 0 // seat 0 goes out first; opponent tichu stopped
        player.recordHand(h, seat = 0, addOnFailure = true)
        // stoppedTichuScore adds +100 to team1: 50 (card) + 100 (stopped) = 150
        assertEquals(150, player.totalPoints)
    }

    @Test fun recordHand_addOnFailure_opponentTichuFails_normalModeNoGain() {
        val h = Hand()
        h.setTichuFor(1)
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 0
        player.recordHand(h, seat = 0, addOnFailure = false)
        // Normal mode: team1 gains nothing from stopping; 50 (card) only
        assertEquals(50, player.totalPoints)
    }

    @Test fun unrecordHand_addOnFailure_revertsCorrectly() {
        val h = Hand()
        h.setTichuFor(0)
        h.cardScoreTeamOne = 50
        h.cardScoreTeamTwo = 50
        h.playerOutFirst = 1
        player.recordHand(h, seat = 0, addOnFailure = true)
        player.unrecordHand(h, seat = 0, addOnFailure = true)
        assertEquals(0, player.totalPoints)
        assertEquals(0, player.numHands)
    }

    // --- untested derived stat helpers (#75) ---

    @Test fun getCardPtsPerHand_noHands_returnsZero() = assertEquals(0.0, player.getCardPtsPerHand(), 0.0)

    @Test fun getCardPtsPerHand_calculated() {
        player.numHands = 4
        player.cardPoints = 220
        assertEquals(55.0, player.getCardPtsPerHand(), 0.001)
    }

    @Test fun getTichuEfficiency_noHands_returnsZero() = assertEquals(0.0, player.getTichuEfficiency(), 0.0)

    @Test fun getTichuEfficiency_calculated() {
        player.tichuEfficiencyHands = 5
        player.tichuEfficiencyPoints = 150
        assertEquals(30.0, player.getTichuEfficiency(), 0.001)
    }

    @Test fun getGTPct_noCalls_returnsZero() = assertEquals(0.0, player.getGTPct(), 0.0)

    @Test fun getGTPct_calculated() {
        player.numGTCalled = 4
        player.numGTMade = 1
        assertEquals(25.0, player.getGTPct(), 0.001)
    }

    @Test fun getHandsPerDW_noDoubleWins_returns1000() = assertEquals(1000.0, player.getHandsPerDW(), 0.0)

    @Test fun getHandsPerDW_calculated() {
        player.numHands = 10
        player.numDoubleWins = 2
        assertEquals(5.0, player.getHandsPerDW(), 0.001)
    }

    @Test fun nonCalls_noEfficiencyHands_returnsZero() {
        assertEquals(0, player.nonCalls())
    }

    @Test fun nonCalls_calculatedAsEfficiencyHandsMinusTichuCalled() {
        player.tichuEfficiencyHands = 5
        player.numTichuCalled = 2
        assertEquals(3, player.nonCalls())
    }

    @Test fun getPartnerTichuPct_noCalls_returnsZero() = assertEquals(0.0, player.getPartnerTichuPct(), 0.0)

    @Test fun getPartnerTichuPct_calculated() {
        player.numTichusCalledByPartner = 4
        player.numTichusMadeByPartner = 3
        assertEquals(75.0, player.getPartnerTichuPct(), 0.001)
    }
}
