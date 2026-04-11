package com.tichuguru.model

import org.junit.Assert.assertEquals
import org.junit.Test

class HandTest {

    // --- companion helpers ---

    @Test fun otherCardScore_normal() = assertEquals(50, Hand.otherCardScore(50))
    @Test fun otherCardScore_zero() = assertEquals(100, Hand.otherCardScore(0))
    @Test fun otherCardScore_hundred() = assertEquals(0, Hand.otherCardScore(100))
    @Test fun otherCardScore_doubleWin() = assertEquals(0, Hand.otherCardScore(200))

    @Test fun cardScoreIndex_zero() = assertEquals(0, Hand.cardScoreIndex(0))
    @Test fun cardScoreIndex_fifty() = assertEquals(10, Hand.cardScoreIndex(50))
    @Test fun cardScoreIndex_hundred() = assertEquals(20, Hand.cardScoreIndex(100))
    @Test fun cardScoreIndex_doubleWin() = assertEquals(21, Hand.cardScoreIndex(200))

    // --- setCardScore updates totals ---

    @Test fun setCardScore1_updatesTotalScore1() {
        val hand = Hand()
        hand.setCardScore1(60)
        assertEquals(60, hand.totalScore1)
    }

    @Test fun setCardScore2_updatesTotalScore2() {
        val hand = Hand()
        hand.setCardScore2(40)
        assertEquals(40, hand.totalScore2)
    }

    @Test fun setCardScore_includesTichuScoreInTotal() {
        val hand = Hand()
        hand.setTichuFor(0)
        hand.setOutFirst(0)         // tichu success: +100 to team1
        hand.setCardScore1(60)      // total1 = 60 + 100 = 160
        assertEquals(160, hand.totalScore1)
    }

    // --- tichu scoring (subtract-on-failure mode, default) ---

    @Test fun tichu_success_addsToCallerTeam() {
        val hand = Hand()
        hand.setTichuFor(0)          // player 0 (team1) calls tichu
        hand.setCardScore1(50)
        hand.setCardScore2(50)
        hand.setOutFirst(0)          // player 0 goes out first → success
        assertEquals(100, hand.tichuScore1)
        assertEquals(0, hand.tichuScore2)
        assertEquals(150, hand.totalScore1)
        assertEquals(50, hand.totalScore2)
    }

    @Test fun tichu_failure_subtractsFromCallerTeam() {
        val hand = Hand()
        hand.setTichuFor(0)          // player 0 (team1) calls tichu
        hand.setCardScore1(50)
        hand.setCardScore2(50)
        hand.setOutFirst(1)          // player 1 (team2) goes out first → failure
        assertEquals(-100, hand.tichuScore1)
        assertEquals(0, hand.tichuScore2)
        assertEquals(-50, hand.totalScore1)
        assertEquals(50, hand.totalScore2)
    }

    @Test fun tichu_team2_success() {
        val hand = Hand()
        hand.setTichuFor(1)          // player 1 (team2) calls tichu
        hand.setCardScore1(50)
        hand.setCardScore2(50)
        hand.setOutFirst(1)          // player 1 goes out first → success
        assertEquals(0, hand.tichuScore1)
        assertEquals(100, hand.tichuScore2)
    }

    @Test fun tichu_team2_failure() {
        val hand = Hand()
        hand.setTichuFor(3)          // player 3 (team2) calls tichu
        hand.setCardScore1(50)
        hand.setCardScore2(50)
        hand.setOutFirst(0)          // player 0 (team1) goes out first → failure for p3
        assertEquals(0, hand.tichuScore1)
        assertEquals(-100, hand.tichuScore2)
    }

    // --- grand tichu scoring ---

    @Test fun grandTichu_success_adds200() {
        val hand = Hand()
        hand.setGrandTichuFor(2)     // player 2 (team1) calls GT
        hand.setCardScore1(50)
        hand.setCardScore2(50)
        hand.setOutFirst(2)          // success
        assertEquals(200, hand.tichuScore1)
    }

    @Test fun grandTichu_failure_subtracts200() {
        val hand = Hand()
        hand.setGrandTichuFor(2)     // player 2 (team1) calls GT
        hand.setCardScore1(50)
        hand.setCardScore2(50)
        hand.setOutFirst(1)          // failure
        assertEquals(-200, hand.tichuScore1)
    }

    // --- addOnFailure mode ---

    @Test fun addOnFailure_tichu_success_adds100() {
        val hand = Hand(isAddOnFailure = true)
        hand.setTichuFor(0)
        hand.setCardScore1(50)
        hand.setCardScore2(50)
        hand.setOutFirst(0)          // success
        assertEquals(100, hand.tichuScore1)
    }

    @Test fun addOnFailure_tichu_failure_addsToOpponents() {
        val hand = Hand(isAddOnFailure = true)
        hand.setTichuFor(0)          // player 0 (team1) calls tichu, fails
        hand.setCardScore1(50)
        hand.setCardScore2(50)
        hand.setOutFirst(1)          // player 1 (team2) goes out first
        // In addOnFailure mode, failed tichu score goes to opponent team
        assertEquals(0, hand.tichuScore1)
        assertEquals(100, hand.tichuScore2)
    }

    @Test fun addOnFailure_grandTichu_failure_addsToOpponents() {
        val hand = Hand(isAddOnFailure = true)
        hand.setGrandTichuFor(0)     // player 0 (team1) calls GT, fails
        hand.setOutFirst(1)
        assertEquals(0, hand.tichuScore1)
        assertEquals(200, hand.tichuScore2)
    }

    // --- multiple bids in one hand ---

    @Test fun bothTeamsTichu_bothSucceed() {
        val hand = Hand()
        hand.setTichuFor(0)          // team1 tichu
        hand.setTichuFor(1)          // team2 tichu
        hand.setCardScore1(50)
        hand.setCardScore2(50)
        hand.setOutFirst(0)          // player 0 wins → p0 tichu succeeds, p1 fails
        assertEquals(100, hand.tichuScore1)   // p0 success
        assertEquals(-100, hand.tichuScore2)  // p1 failure
    }

    @Test fun twoTeammateTichus_bothSucceed() {
        val hand = Hand()
        hand.setTichuFor(0)
        hand.setTichuFor(2)          // both team1 players call tichu, only one can succeed
        hand.setCardScore1(50)
        hand.setCardScore2(50)
        hand.setOutFirst(0)          // player 0 wins → p0 success, p2 failure
        assertEquals(0, hand.tichuScore1)     // +100 (p0) - 100 (p2 failure) = 0
    }

    // --- setOutFirst resets tichu scores on each call ---

    @Test fun setOutFirst_calledTwice_recalculates() {
        val hand = Hand()
        hand.setTichuFor(0)
        hand.setCardScore1(50)
        hand.setCardScore2(50)
        hand.setOutFirst(1)          // failure first
        hand.setOutFirst(0)          // then success
        assertEquals(100, hand.tichuScore1)
        assertEquals(0, hand.tichuScore2)
    }
}
