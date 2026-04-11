package com.tichuguru.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameTest {
    private lateinit var game: Game

    @Before fun setup() {
        val players = listOf(Player("A"), Player("B"), Player("C"), Player("D"))
        game = Game(players)
    }

    private fun hand(
        score1: Int,
        score2: Int = Hand.otherCardScore(score1),
    ): Hand {
        val h = Hand()
        h.setCardScore1(score1)
        h.setCardScore2(score2)
        h.setOutFirst(0)
        return h
    }

    // --- scoreHand accumulates scores ---

    @Test fun scoreHand_addsTotals() {
        game.scoreHand(hand(60))
        assertEquals(60, game.score1)
        assertEquals(40, game.score2)
    }

    @Test fun scoreHand_multiplehands_accumulate() {
        game.scoreHand(hand(60))
        game.scoreHand(hand(70))
        assertEquals(130, game.score1)
        assertEquals(70, game.score2)
    }

    @Test fun scoreHand_ignored_afterGameOver() {
        game.scoreHand(hand(1000))
        assertTrue(game.gameOver)
        game.scoreHand(hand(60))
        assertEquals(1000, game.score1)
    }

    // --- game-end conditions ---

    @Test fun gameEnds_whenScoreReachesLimit() {
        game.scoreHand(hand(1000))
        assertTrue(game.gameOver)
    }

    @Test fun gameDoesNotEnd_whenTied() {
        // Both teams reach 1000 simultaneously → tied scores → game continues
        val h = Hand()
        h.setCardScore1(1000)
        h.setCardScore2(1000)
        h.setOutFirst(0)
        game.scoreHand(h)
        assertFalse(game.gameOver)
    }

    @Test fun gameEnds_whenScore2ReachesLimit() {
        game.scoreHand(hand(0)) // score1=0, score2=100
        repeat(9) { game.scoreHand(hand(0)) } // score2=1000
        assertTrue(game.gameOver)
    }

    // --- mercy rule ---

    @Test fun mercyRule_disabled_gameDoesNotEndEarly() {
        game.mercyRule = false
        game.scoreHand(hand(600)) // diff = 500, not 1000
        assertFalse(game.gameOver)
    }

    @Test fun mercyRule_enabled_gameEndsWhenDiffReachesLimit() {
        // A failed grand tichu (−200 to team2) with normal card split gives:
        // totalScore1=50, totalScore2=−150 per hand.
        // After 5 hands: score1=250, score2=−750, diff=1000 → mercy rule fires.
        // Neither team reaches gameLimit (1000) through normal scoring.
        game.mercyRule = true
        val h = Hand()
        h.setGrandTichuFor(1) // team2 player 1 calls GT and fails
        h.setCardScore1(50)
        h.setCardScore2(50)
        h.setOutFirst(0) // team1 wins → GT failure → −200 to team2
        repeat(4) {
            game.scoreHand(h)
            assertFalse(game.gameOver)
        }
        game.scoreHand(h) // 5th hand: diff reaches 1000
        assertTrue(game.gameOver)
    }

    // --- double win ---

    @Test fun doubleWin_score200_countsAs200ForTeam1() {
        val h = Hand()
        h.setCardScore1(200)
        h.setCardScore2(0)
        h.setOutFirst(0)
        game.scoreHand(h)
        assertEquals(200, game.score1)
        assertEquals(0, game.score2)
    }

    // --- removeHand ---

    @Test fun removeHand_revertsScores() {
        game.scoreHand(hand(60))
        game.scoreHand(hand(70))
        game.removeHand(0)
        assertEquals(70, game.score1)
        assertEquals(30, game.score2)
    }

    @Test fun removeHand_singleHand_resetsToZero() {
        game.scoreHand(hand(60))
        game.removeHand(0)
        assertEquals(0, game.score1)
        assertEquals(0, game.score2)
    }

    @Test fun removeHand_undoesGameOver() {
        game.scoreHand(hand(1000))
        assertTrue(game.gameOver)
        game.removeHand(0)
        assertFalse(game.gameOver)
    }

    @Test fun removeHand_removesFromHandList() {
        game.scoreHand(hand(60))
        game.scoreHand(hand(70))
        game.removeHand(0)
        assertEquals(1, game.hands.size)
    }

    // --- ignoreStats ---

    @Test fun ignoreStats_doesNotRecordPlayerStats() {
        game.ignoreStats = true
        game.scoreHand(hand(60))
        assertEquals(0, game.players[0].numHands)
    }

    @Test fun withStats_recordsPlayerStats() {
        game.scoreHand(hand(60))
        assertEquals(1, game.players[0].numHands)
    }
}
