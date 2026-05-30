package com.tichuguru.db

import com.tichuguru.model.Game
import com.tichuguru.model.Hand
import com.tichuguru.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class EntityMapperTest {
    // --- PlayerEntity ---

    @Test fun playerEntity_from_mapsAllFields() {
        val p =
            Player("Alice").apply {
                dbId = 7
                numGames = 10
                numWins = 6
                numHands = 40
                totalPoints = 2000
                cardPoints = 1800
                numDoubleWins = 3
                numTichuCalled = 8
                numTichuMade = 5
                numGTCalled = 2
                numGTMade = 1
                tichuEfficiencyPoints = 300
                tichuEfficiencyHands = 12
                numTichusStopped = 4
                numTichusCalledByOpps = 6
                numTichusCalledByPartner = 3
                numTichusMadeByPartner = 2
            }
        val e = PlayerEntity.from(p)
        assertEquals(7L, e.id)
        assertEquals("Alice", e.name)
        assertEquals(10, e.numGames)
        assertEquals(6, e.numWins)
        assertEquals(40, e.numHands)
        assertEquals(2000, e.totalPoints)
        assertEquals(1800, e.cardPoints)
        assertEquals(3, e.numDoubleWins)
        assertEquals(8, e.numTichuCalled)
        assertEquals(5, e.numTichuMade)
        assertEquals(2, e.numGTCalled)
        assertEquals(1, e.numGTMade)
        assertEquals(300, e.tichuEfficiencyPoints)
        assertEquals(12, e.tichuEfficiencyHands)
        assertEquals(4, e.numTichusStopped)
        assertEquals(6, e.numTichusCalledByOpps)
        assertEquals(3, e.numTichusCalledByPartner)
        assertEquals(2, e.numTichusMadeByPartner)
    }

    @Test fun playerEntity_toPlayer_mapsAllFields() {
        val e =
            PlayerEntity(
                id = 5,
                name = "Bob",
                numGames = 8,
                numWins = 4,
                numHands = 30,
                totalPoints = 1500,
                cardPoints = 1200,
                numDoubleWins = 2,
                numTichuCalled = 6,
                numTichuMade = 4,
                numGTCalled = 1,
                numGTMade = 0,
                tichuEfficiencyPoints = 200,
                tichuEfficiencyHands = 9,
                numTichusStopped = 3,
                numTichusCalledByOpps = 5,
                numTichusCalledByPartner = 2,
                numTichusMadeByPartner = 1
            )
        val p = e.toPlayer()
        assertEquals(5L, p.dbId)
        assertEquals("Bob", p.name)
        assertEquals(8, p.numGames)
        assertEquals(4, p.numWins)
        assertEquals(30, p.numHands)
        assertEquals(1500, p.totalPoints)
        assertEquals(1200, p.cardPoints)
        assertEquals(2, p.numDoubleWins)
        assertEquals(6, p.numTichuCalled)
        assertEquals(4, p.numTichuMade)
        assertEquals(1, p.numGTCalled)
        assertEquals(0, p.numGTMade)
        assertEquals(200, p.tichuEfficiencyPoints)
        assertEquals(9, p.tichuEfficiencyHands)
        assertEquals(3, p.numTichusStopped)
        assertEquals(5, p.numTichusCalledByOpps)
        assertEquals(2, p.numTichusCalledByPartner)
        assertEquals(1, p.numTichusMadeByPartner)
    }

    @Test fun playerEntity_roundTrip_preservesAllFields() {
        val original =
            Player("Carol").apply {
                dbId = 3
                numGames = 5
                numWins = 3
                numHands = 20
                totalPoints = 900
                cardPoints = 700
                numDoubleWins = 1
                numTichuCalled = 4
                numTichuMade = 3
                numGTCalled = 1
                numGTMade = 1
                tichuEfficiencyPoints = 150
                tichuEfficiencyHands = 7
                numTichusStopped = 2
                numTichusCalledByOpps = 3
                numTichusCalledByPartner = 2
                numTichusMadeByPartner = 2
            }
        val restored = PlayerEntity.from(original).toPlayer()
        assertEquals(original.dbId, restored.dbId)
        assertEquals(original.name, restored.name)
        assertEquals(original.numGames, restored.numGames)
        assertEquals(original.numWins, restored.numWins)
        assertEquals(original.numHands, restored.numHands)
        assertEquals(original.totalPoints, restored.totalPoints)
        assertEquals(original.cardPoints, restored.cardPoints)
        assertEquals(original.numDoubleWins, restored.numDoubleWins)
        assertEquals(original.numTichuCalled, restored.numTichuCalled)
        assertEquals(original.numTichuMade, restored.numTichuMade)
        assertEquals(original.numGTCalled, restored.numGTCalled)
        assertEquals(original.numGTMade, restored.numGTMade)
        assertEquals(original.tichuEfficiencyPoints, restored.tichuEfficiencyPoints)
        assertEquals(original.tichuEfficiencyHands, restored.tichuEfficiencyHands)
        assertEquals(original.numTichusStopped, restored.numTichusStopped)
        assertEquals(original.numTichusCalledByOpps, restored.numTichusCalledByOpps)
        assertEquals(original.numTichusCalledByPartner, restored.numTichusCalledByPartner)
        assertEquals(original.numTichusMadeByPartner, restored.numTichusMadeByPartner)
    }

    // --- HandEntity ---

    private fun hand(
        cardScore1: Int = 60,
        outFirst: Int = 0,
    ): Hand {
        val h = Hand()
        h.cardScoreTeamOne = cardScore1
        h.cardScoreTeamTwo = Hand.otherCardScore(cardScore1)
        h.playerOutFirst = outFirst
        return h
    }

    @Test fun handEntity_from_mapsCardScoresAndOutFirst() {
        val h = hand(70, outFirst = 2)
        h.dbId = 11
        val e = HandEntity.from(h, gameId = 99, order = 3, addOnFailure = false)
        assertEquals(11L, e.id)
        assertEquals(99L, e.gameId)
        assertEquals(3, e.handOrder)
        assertEquals(70, e.cardScore1)
        assertEquals(30, e.cardScore2)
        assertEquals(2, e.outFirst)
        assertFalse(e.addOnFailure)
    }

    @Test fun handEntity_from_mapsBids() {
        val h = hand()
        h.setTichuFor(1)
        h.setGrandTichuFor(2)
        val e = HandEntity.from(h, gameId = 1, order = 0, addOnFailure = false)
        assertFalse(e.tichu0)
        assertTrue(e.tichu1)
        assertFalse(e.tichu2)
        assertFalse(e.tichu3)
        assertFalse(e.grandTichu0)
        assertFalse(e.grandTichu1)
        assertTrue(e.grandTichu2)
        assertFalse(e.grandTichu3)
    }

    @Test fun handEntity_from_computesTichuScores() {
        val h = hand(50)
        h.setTichuFor(0) // seat 0 calls tichu, goes out first → +100
        val e = HandEntity.from(h, gameId = 1, order = 0, addOnFailure = false)
        assertEquals(100, e.tichuScore1)
        assertEquals(0, e.tichuScore2)
        assertEquals(150, e.totalScore1) // 50 card + 100 tichu
        assertEquals(50, e.totalScore2)
    }

    @Test fun handEntity_toHand_restoresCardScoresAndOutFirst() {
        val e =
            HandEntity(
                id = 4,
                gameId = 1,
                handOrder = 0,
                cardScore1 = 65,
                cardScore2 = 35,
                outFirst = 3,
                totalScore1 = 65,
                totalScore2 = 35,
                tichuScore1 = 0,
                tichuScore2 = 0
            )
        val h = e.toHand()
        assertEquals(4L, h.dbId)
        assertEquals(65, h.cardScoreTeamOne)
        assertEquals(35, h.cardScoreTeamTwo)
        assertEquals(3, h.playerOutFirst)
    }

    @Test fun handEntity_toHand_restoresBids() {
        val e =
            HandEntity(
                id = 0,
                gameId = 1,
                handOrder = 0,
                cardScore1 = 50,
                cardScore2 = 50,
                outFirst = 0,
                totalScore1 = 50,
                totalScore2 = 50,
                tichuScore1 = 0,
                tichuScore2 = 0,
                tichu0 = false,
                tichu1 = true,
                tichu2 = false,
                tichu3 = false,
                grandTichu0 = false,
                grandTichu1 = false,
                grandTichu2 = true,
                grandTichu3 = false
            )
        val h = e.toHand()
        assertFalse(h.isTichuFor(0))
        assertTrue(h.isTichuFor(1))
        assertFalse(h.isGrandTichuFor(1))
        assertTrue(h.isGrandTichuFor(2))
        assertFalse(h.isGrandTichuFor(3))
    }

    @Test fun handEntity_roundTrip_preservesBidsAndScores() {
        val original = hand(60)
        original.setTichuFor(0)
        original.setGrandTichuFor(3)
        original.dbId = 8

        val restored = HandEntity.from(original, gameId = 1, order = 0, addOnFailure = false).toHand()

        assertEquals(original.dbId, restored.dbId)
        assertEquals(original.cardScoreTeamOne, restored.cardScoreTeamOne)
        assertEquals(original.cardScoreTeamTwo, restored.cardScoreTeamTwo)
        assertEquals(original.playerOutFirst, restored.playerOutFirst)
        assertTrue(restored.isTichuFor(0))
        assertFalse(restored.isTichuFor(1))
        assertFalse(restored.isGrandTichuFor(0))
        assertTrue(restored.isGrandTichuFor(3))
    }

    // --- GameEntity ---

    private fun game(): Game {
        val players =
            listOf(Player("A"), Player("B"), Player("C"), Player("D"))
                .onEachIndexed { i, p -> p.dbId = (i + 1).toLong() }
        return Game(
            players = players.toMutableList(),
            score1 = 750,
            score2 = 500,
            gameLimit = 1000,
            gameOver = false,
            mercyRule = true,
            ignoreStats = false,
            addOnFailure = true,
            date = Instant.ofEpochMilli(1_700_000_000_000L)
        ).also { it.dbId = 42 }
    }

    @Test fun gameEntity_from_mapsScoresAndFlags() {
        val e = GameEntity.from(game())
        assertEquals(42L, e.id)
        assertEquals(750, e.score1)
        assertEquals(500, e.score2)
        assertEquals(1000, e.gameLimit)
        assertFalse(e.gameOver)
        assertTrue(e.mercyRule)
        assertFalse(e.ignoreStats)
        assertTrue(e.addOnFailure)
    }

    @Test fun gameEntity_from_mapsPlayerIds() {
        val e = GameEntity.from(game())
        assertEquals(1L, e.player0)
        assertEquals(2L, e.player1)
        assertEquals(3L, e.player2)
        assertEquals(4L, e.player3)
    }

    @Test fun gameEntity_from_mapsDate() {
        val e = GameEntity.from(game())
        assertEquals(1_700_000_000_000L, e.dateMs)
    }
}
