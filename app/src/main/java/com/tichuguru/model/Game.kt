package com.tichuguru.model

import java.util.Date
import kotlin.math.abs

class Game {

    var players: MutableList<Player> = mutableListOf()
    var hands: MutableList<Hand> = mutableListOf()
    var score1: Int = 0
    var score2: Int = 0
    var gameLimit: Int = 0
    var isGameOver: Boolean = false
    var isMercyRule: Boolean = false
    var isIgnoreStats: Boolean = false
    var isAddOnFailure: Boolean = false
    var date: Date = Date()
    var dbId: Long = 0

    constructor()

    constructor(players: List<Player>) {
        this.players = players.toMutableList()
        this.gameLimit = 1000
        this.isMercyRule = true
        this.date = Date()
    }

    constructor(g: Game) : this(g.players) {
        this.isMercyRule = g.isMercyRule
        this.isAddOnFailure = g.isAddOnFailure
    }

    fun lastScore1() = if (hands.isEmpty()) 0 else hands.last().totalScore1

    fun lastScore2() = if (hands.isEmpty()) 0 else hands.last().totalScore2

    fun endGame() {
        isGameOver = true
        if (!isIgnoreStats) {
            for (i in 0..3) players[i].recordGame(this, i)
        }
    }

    fun scoreHand(hand: Hand) {
        if (!isGameOver) {
            hands.add(hand)
            score1 += hand.totalScore1
            score2 += hand.totalScore2
            if (score1 != score2 && (score1 >= gameLimit || score2 >= gameLimit ||
                    (isMercyRule && abs(score1 - score2) >= gameLimit))) {
                isGameOver = true
            }
            if (!isIgnoreStats) {
                for (i in 0..3) {
                    players[i].recordHand(hand, i)
                    if (isGameOver) players[i].recordGame(this, i)
                }
            }
        }
    }

    fun removeHand(handNum: Int) {
        val hand = hands[handNum]
        val undoGame = isGameOver
        isGameOver = false
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
