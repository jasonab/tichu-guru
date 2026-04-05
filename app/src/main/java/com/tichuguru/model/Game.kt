package com.tichuguru.model

import java.util.Date
import kotlin.math.abs

class Game {

    var players: MutableList<Player> = mutableListOf()
    var hands: MutableList<Hand> = mutableListOf()
    var score1: Int = 0
    var score2: Int = 0
    var gameLimit: Int = 0
    var gameOver: Boolean = false
    var mercyRule: Boolean = false
    var ignoreStats: Boolean = false
    var addOnFailure: Boolean = false
    var date: Date = Date()
    var dbId: Long = 0

    constructor()

    constructor(players: List<Player>) {
        this.players = players.toMutableList()
        this.gameLimit = 1000
        this.mercyRule = true
        this.date = Date()
    }

    constructor(g: Game) : this(g.players) {
        this.mercyRule = g.mercyRule
        this.addOnFailure = g.addOnFailure
    }

    fun endGame() {
        gameOver = true
        if (!ignoreStats) {
            for (i in 0..3) players[i].recordGame(this, i)
        }
    }

    fun scoreHand(hand: Hand) {
        if (!gameOver) {
            hands.add(hand)
            score1 += hand.totalScore1
            score2 += hand.totalScore2
            if (score1 != score2 && (score1 >= gameLimit || score2 >= gameLimit ||
                    (mercyRule && abs(score1 - score2) >= gameLimit))) {
                gameOver = true
            }
            if (!ignoreStats) {
                for (i in 0..3) {
                    players[i].recordHand(hand, i)
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
            p.unrecordHand(hand, i)
        }
        score1 -= hand.totalScore1
        score2 -= hand.totalScore2
        hands.removeAt(handNum)
    }

    fun containsPlayer(p: Player): Boolean = players.any { it === p }

    fun setPlayer(seat: Int, p: Player) { players[seat] = p }
}
