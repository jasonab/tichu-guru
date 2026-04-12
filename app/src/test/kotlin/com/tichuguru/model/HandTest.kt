package com.tichuguru.model

import org.junit.Assert.assertEquals
import org.junit.Test

class HandTest {
    // --- companion helpers ---

    @Test fun otherCardScore_normal() = assertEquals(50, Hand.otherCardScore(50))

    @Test fun otherCardScore_zero() = assertEquals(100, Hand.otherCardScore(0))

    @Test fun otherCardScore_hundred() = assertEquals(0, Hand.otherCardScore(100))

    @Test fun otherCardScore_negativeTwentyFive() = assertEquals(125, Hand.otherCardScore(-25))

    @Test fun otherCardScore_hundredTwentyFive() = assertEquals(-25, Hand.otherCardScore(125))

    @Test fun otherCardScore_doubleWin() = assertEquals(0, Hand.otherCardScore(200))

    @Test fun cardScoreIndex_negativeTwentyFive() = assertEquals(0, Hand.cardScoreIndex(-25))

    @Test fun cardScoreIndex_zero() = assertEquals(5, Hand.cardScoreIndex(0))

    @Test fun cardScoreIndex_fifty() = assertEquals(15, Hand.cardScoreIndex(50))

    @Test fun cardScoreIndex_hundred() = assertEquals(25, Hand.cardScoreIndex(100))

    @Test fun cardScoreIndex_hundredTwentyFive() = assertEquals(30, Hand.cardScoreIndex(125))

    @Test fun cardScoreIndex_doubleWin() = assertEquals(31, Hand.cardScoreIndex(200))

    // --- setCardScore updates totals ---

    @Test fun setCardScore1_updatesTotalScore1() {
        val hand = Hand()
        hand.cardScoreTeamOne = 60
        assertEquals(0, hand.tichuScoreTeamOne(false))
        assertEquals(0, hand.tichuScoreTeamTwo(false))
        assertEquals(60, hand.cardScoreTeamOne)
        assertEquals(60, hand.totalScoreTeamOne(false))
    }

    @Test fun setCardScore2_updatesTotalScore2() {
        val hand = Hand()
        hand.cardScoreTeamTwo = 40
        assertEquals(0, hand.tichuScoreTeamOne(false))
        assertEquals(0, hand.tichuScoreTeamTwo(false))
        assertEquals(40, hand.cardScoreTeamTwo)
        assertEquals(40, hand.totalScoreTeamTwo(false))
    }

    @Test fun setCardScore_includesTichuScoreInTotal() {
        val hand = Hand()
        hand.setTichuFor(0)
        hand.playerOutFirst = 0 // tichu success: +100 to team1
        hand.cardScoreTeamOne = 60
        hand.cardScoreTeamTwo = 40
        assertEquals(60, hand.cardScoreTeamOne)
        assertEquals(100, hand.tichuScoreTeamOne(false))
        assertEquals(160, hand.totalScoreTeamOne(false))
        assertEquals(40, hand.cardScoreTeamTwo)
        assertEquals(0, hand.tichuScoreTeamTwo(false))
        assertEquals(40, hand.totalScoreTeamTwo(false))
    }

    // --- tichu scoring (subtract-on-failure mode, default) ---

    @Test fun tichu_success_addsToCallerTeam() {
        val hand = Hand()
        hand.setTichuFor(0) // player 0 (team1) calls tichu
        hand.cardScoreTeamOne = 50
        hand.cardScoreTeamTwo = 50
        hand.playerOutFirst = 0 // player 0 goes out first → success
        assertEquals(50, hand.cardScoreTeamOne)
        assertEquals(100, hand.tichuScoreTeamOne(false))
        assertEquals(150, hand.totalScoreTeamOne(false))
        assertEquals(50, hand.cardScoreTeamTwo)
        assertEquals(0, hand.tichuScoreTeamTwo(false))
        assertEquals(50, hand.totalScoreTeamTwo(false))
    }

    @Test fun tichu_failure_subtractsFromCallerTeam() {
        val hand = Hand()
        hand.setTichuFor(0) // player 0 (team1) calls tichu
        hand.cardScoreTeamOne = 50
        hand.cardScoreTeamTwo = 50
        hand.playerOutFirst = 1 // player 1 (team2) goes out first → failure
        assertEquals(50, hand.cardScoreTeamOne)
        assertEquals(-100, hand.tichuScoreTeamOne(false))
        assertEquals(-50, hand.totalScoreTeamOne(false))
        assertEquals(50, hand.cardScoreTeamTwo)
        assertEquals(0, hand.tichuScoreTeamTwo(false))
        assertEquals(50, hand.totalScoreTeamTwo(false))
    }

    @Test fun tichu_team2_success() {
        val hand = Hand()
        hand.setTichuFor(1) // player 1 (team2) calls tichu
        hand.cardScoreTeamOne = 50
        hand.cardScoreTeamTwo = 50
        hand.playerOutFirst = 1 // player 1 goes out first → success
        assertEquals(50, hand.cardScoreTeamOne)
        assertEquals(0, hand.tichuScoreTeamOne(false))
        assertEquals(50, hand.totalScoreTeamOne(false))
        assertEquals(50, hand.cardScoreTeamTwo)
        assertEquals(100, hand.tichuScoreTeamTwo(false))
        assertEquals(150, hand.totalScoreTeamTwo(false))
    }

    @Test fun tichu_team2_failure() {
        val hand = Hand()
        hand.setTichuFor(3) // player 3 (team2) calls tichu
        hand.cardScoreTeamOne = 50
        hand.cardScoreTeamTwo = 50
        hand.playerOutFirst = 0 // player 0 (team1) goes out first → failure for p3
        assertEquals(50, hand.cardScoreTeamOne)
        assertEquals(0, hand.tichuScoreTeamOne(false))
        assertEquals(50, hand.totalScoreTeamOne(false))
        assertEquals(50, hand.cardScoreTeamTwo)
        assertEquals(-100, hand.tichuScoreTeamTwo(false))
        assertEquals(-50, hand.totalScoreTeamTwo(false))
    }

    // --- grand tichu scoring ---

    @Test fun grandTichu_success_adds200() {
        val hand = Hand()
        hand.setGrandTichuFor(2) // player 2 (team1) calls GT
        hand.cardScoreTeamOne = 50
        hand.cardScoreTeamTwo = 50
        hand.playerOutFirst = 2 // success
        assertEquals(50, hand.cardScoreTeamOne)
        assertEquals(200, hand.tichuScoreTeamOne(false))
        assertEquals(250, hand.totalScoreTeamOne(false))
        assertEquals(50, hand.cardScoreTeamTwo)
        assertEquals(0, hand.tichuScoreTeamTwo(false))
        assertEquals(50, hand.totalScoreTeamTwo(false))
    }

    @Test fun grandTichu_failure_subtracts200() {
        val hand = Hand()
        hand.setGrandTichuFor(2) // player 2 (team1) calls GT
        hand.cardScoreTeamOne = 50
        hand.cardScoreTeamTwo = 50
        hand.playerOutFirst = 1 // failure
        assertEquals(50, hand.cardScoreTeamOne)
        assertEquals(-200, hand.tichuScoreTeamOne(false))
        assertEquals(-150, hand.totalScoreTeamOne(false))
        assertEquals(50, hand.cardScoreTeamTwo)
        assertEquals(0, hand.tichuScoreTeamTwo(false))
        assertEquals(50, hand.totalScoreTeamTwo(false))
    }

    // --- addOnFailure mode ---

    @Test fun addOnFailure_tichu_success_adds100() {
        val hand = Hand()
        hand.setTichuFor(0)
        hand.cardScoreTeamOne = 50
        hand.cardScoreTeamTwo = 50
        hand.playerOutFirst = 0 // success
        assertEquals(50, hand.cardScoreTeamOne)
        assertEquals(100, hand.tichuScoreTeamOne(true))
        assertEquals(150, hand.totalScoreTeamOne(true))
        assertEquals(50, hand.cardScoreTeamTwo)
        assertEquals(0, hand.tichuScoreTeamTwo(true))
        assertEquals(50, hand.totalScoreTeamTwo(true))
    }

    @Test fun addOnFailure_tichu_failure_addsToOpponents() {
        val hand = Hand()
        hand.setTichuFor(0) // player 0 (team1) calls tichu, fails
        hand.cardScoreTeamOne = 50
        hand.cardScoreTeamTwo = 50
        hand.playerOutFirst = 1 // player 1 (team2) goes out first
        // In addOnFailure mode, failed tichu score goes to opponent team
        assertEquals(50, hand.cardScoreTeamOne)
        assertEquals(0, hand.tichuScoreTeamOne(true))
        assertEquals(50, hand.totalScoreTeamOne(true))
        assertEquals(50, hand.cardScoreTeamTwo)
        assertEquals(100, hand.tichuScoreTeamTwo(true))
        assertEquals(150, hand.totalScoreTeamTwo(true))
    }

    @Test fun addOnFailure_grandTichu_failure_addsToOpponents() {
        val hand = Hand()
        hand.setGrandTichuFor(0) // player 0 (team1) calls GT, fails
        hand.cardScoreTeamOne = 50
        hand.cardScoreTeamTwo = 50
        hand.playerOutFirst = 1
        assertEquals(50, hand.cardScoreTeamOne)
        assertEquals(0, hand.tichuScoreTeamOne(true))
        assertEquals(50, hand.totalScoreTeamOne(true))
        assertEquals(50, hand.cardScoreTeamTwo)
        assertEquals(200, hand.tichuScoreTeamTwo(true))
        assertEquals(250, hand.totalScoreTeamTwo(true))
    }

    // --- multiple bids in one hand ---

    @Test fun opposingTichus_outFirstSucceedsOtherFails() {
        val hand = Hand()
        hand.setTichuFor(0) // team1 tichu
        hand.setTichuFor(1) // team2 tichu
        hand.cardScoreTeamOne = 50
        hand.cardScoreTeamTwo = 50
        hand.playerOutFirst = 0 // player 0 wins → p0 tichu succeeds, p1 fails
        assertEquals(50, hand.cardScoreTeamOne)
        assertEquals(100, hand.tichuScoreTeamOne(false)) // p0 success
        assertEquals(150, hand.totalScoreTeamOne(false))
        assertEquals(50, hand.cardScoreTeamTwo)
        assertEquals(-100, hand.tichuScoreTeamTwo(false)) // p1 failure
        assertEquals(-50, hand.totalScoreTeamTwo(false))
    }

    @Test fun twoTeammateTichus_onlyOutFirstSucceeds() {
        val hand = Hand()
        hand.setTichuFor(0)
        hand.setTichuFor(2) // both team1 players call tichu, only one can succeed
        hand.cardScoreTeamOne = 50
        hand.cardScoreTeamTwo = 50
        hand.playerOutFirst = 0 // player 0 wins → p0 success, p2 failure
        assertEquals(50, hand.cardScoreTeamOne)
        assertEquals(0, hand.tichuScoreTeamOne(false)) // +100 (p0) - 100 (p2 failure) = 0
        assertEquals(50, hand.totalScoreTeamOne(false))
        assertEquals(50, hand.cardScoreTeamTwo)
        assertEquals(0, hand.tichuScoreTeamTwo(false))
        assertEquals(50, hand.totalScoreTeamTwo(false))
    }

    // --- setOutFirst resets tichu scores on each call ---

    @Test fun setOutFirst_calledTwice_recalculates() {
        val hand = Hand()
        hand.setTichuFor(0)
        hand.cardScoreTeamOne = 50
        hand.cardScoreTeamTwo = 50
        hand.playerOutFirst = 1 // failure first
        hand.playerOutFirst = 0 // then success
        assertEquals(50, hand.cardScoreTeamOne)
        assertEquals(100, hand.tichuScoreTeamOne(false))
        assertEquals(150, hand.totalScoreTeamOne(false))
        assertEquals(50, hand.cardScoreTeamTwo)
        assertEquals(0, hand.tichuScoreTeamTwo(false))
        assertEquals(50, hand.totalScoreTeamTwo(false))
    }
}
