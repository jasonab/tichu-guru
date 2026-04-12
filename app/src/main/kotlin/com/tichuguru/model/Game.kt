package com.tichuguru.model

import java.io.Serializable
import java.time.Instant
import kotlin.math.abs

class Game(
    val players: MutableList<Player> = mutableListOf(),
    val hands: MutableList<Hand> = mutableListOf(),
    var score1: Int = 0,
    var score2: Int = 0,
    val gameLimit: Int = 0,
    var gameOver: Boolean = false,
    val mercyRule: Boolean = false,
    val ignoreStats: Boolean = false,
    val addOnFailure: Boolean = false,
    val date: Instant = Instant.now(),
    var dbId: Long = 0,
) : Serializable {
    constructor(
        players: List<Player>,
        mercyRule: Boolean = false,
        ignoreStats: Boolean = false,
    ) : this(
        players = players.toMutableList(),
        gameLimit = 1000,
        mercyRule = mercyRule,
        ignoreStats = ignoreStats
    )

    fun endGame() {
        gameOver = true
        if (!ignoreStats) {
            for (i in 0..3) players[i].recordGame(this, i)
        }
    }

    fun scoreHand(hand: Hand) {
        if (!gameOver) {
            hands.add(hand)
            score1 += hand.totalScoreTeamOne(addOnFailure)
            score2 += hand.totalScoreTeamTwo(addOnFailure)
            if (score1 != score2 && (
                    score1 >= gameLimit || score2 >= gameLimit ||
                        (mercyRule && abs(score1 - score2) >= gameLimit)
                )
            ) {
                gameOver = true
            }
            if (!ignoreStats) {
                for (i in 0..3) {
                    players[i].recordHand(hand, i, addOnFailure)
                    if (gameOver) players[i].recordGame(this, i)
                }
            }
        }
    }

    fun removeHand(handNum: Int) {
        val hand = hands[handNum]
        val undoGame = gameOver
        gameOver = false
        for (i in 0..3) {
            val p = players[i]
            if (undoGame) p.unrecordGame(this, i)
            p.unrecordHand(hand, i, addOnFailure)
        }
        score1 -= hand.totalScoreTeamOne(addOnFailure)
        score2 -= hand.totalScoreTeamTwo(addOnFailure)
        hands.removeAt(handNum)
    }

    fun containsPlayer(p: Player): Boolean = players.any { it === p }
}
