package com.tichuguru.model

import java.io.Serializable
import java.time.Instant
import kotlin.math.abs

class Game(
    val players: MutableList<Player> = mutableListOf(),
    val hands: MutableList<Hand> = mutableListOf(),
    var teamOneTotal: Int = 0,
    var teamTwoTotal: Int = 0,
    val gameLimit: Int = 0,
    var gameOver: Boolean = false,
    val mercyRule: Boolean = false,
    val ignoreStats: Boolean = false,
    val addOnFailure: Boolean = false,
    val date: Instant = Instant.now(),
    var dbId: Long = 0,
) : Serializable {
    fun endGame() {
        gameOver = true
        if (!ignoreStats) {
            players.forEachIndexed { i, p -> p.recordGame(this, i) }
        }
    }

    fun scoreHand(hand: Hand) {
        if (!gameOver) {
            hands.add(hand)
            teamOneTotal += hand.totalScoreTeamOne(addOnFailure)
            teamTwoTotal += hand.totalScoreTeamTwo(addOnFailure)
            if (teamOneTotal != teamTwoTotal && (
                    teamOneTotal >= gameLimit || teamTwoTotal >= gameLimit ||
                        (mercyRule && abs(teamOneTotal - teamTwoTotal) >= gameLimit)
                )
            ) {
                gameOver = true
            }
            if (!ignoreStats) {
                players.forEachIndexed { i, p ->
                    p.recordHand(hand, i, addOnFailure)
                    if (gameOver) p.recordGame(this, i)
                }
            }
        }
    }

    fun removeHand(handNum: Int) {
        val hand = hands[handNum]
        val undoGame = gameOver
        gameOver = false
        players.forEachIndexed { i, p ->
            if (undoGame) p.unrecordGame(this, i)
            p.unrecordHand(hand, i, addOnFailure)
        }
        teamOneTotal -= hand.totalScoreTeamOne(addOnFailure)
        teamTwoTotal -= hand.totalScoreTeamTwo(addOnFailure)
        hands.removeAt(handNum)
    }

    fun unrecordStats() {
        if (ignoreStats) return
        players.forEachIndexed { i, p ->
            if (gameOver) p.unrecordGame(this, i)
            hands.forEach { hand -> p.unrecordHand(hand, i, addOnFailure) }
        }
    }

    fun containsPlayer(p: Player): Boolean = players.any { it === p }
}
