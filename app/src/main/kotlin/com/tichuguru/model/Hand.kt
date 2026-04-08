package com.tichuguru.model

import java.io.Serializable

class Hand : Serializable {

    companion object {
        /** All card score values selectable during scoring (0-100 in steps of 5, plus 200 for double win). */
        val CARD_SCORE_OPTIONS: IntArray = intArrayOf(
            0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 200
        )

        /** Returns the complementary card score for the other team (scores always sum to 100; double win yields 0). */
        fun otherCardScore(score: Int) = if (score == 200) 0 else 100 - score

        /** Returns the index of a card score in [CARD_SCORE_OPTIONS]. */
        fun cardScoreIndex(score: Int) = if (score == 200) 21 else score / 5
    }

    var dbId: Long = 0
    var isAddOnFailure: Boolean = false
    private var tichu = BooleanArray(4)
    private var grandTichu = BooleanArray(4)
    private var _outFirst: Int = 0
    private var _cardScore1: Int = 0
    private var _cardScore2: Int = 0
    var tichuScore1: Int = 0
    var tichuScore2: Int = 0
    var totalScore1: Int = 0
    var totalScore2: Int = 0

    val cardScore1: Int get() = _cardScore1
    val cardScore2: Int get() = _cardScore2
    fun outFirst(): Int = _outFirst

    constructor()

    constructor(game: Game) {
        isAddOnFailure = game.addOnFailure
    }

    fun setCardScore1(score: Int) {
        _cardScore1 = score
        totalScore1 = _cardScore1 + tichuScore1
    }

    fun setCardScore2(score: Int) {
        _cardScore2 = score
        totalScore2 = _cardScore2 + tichuScore2
    }

    fun setTichuFor(player: Int) { tichu[player] = true }
    fun setGrandTichuFor(player: Int) { grandTichu[player] = true }
    fun isTichuFor(player: Int) = tichu[player]
    fun isGrandTichuFor(player: Int) = grandTichu[player]

    fun setOutFirst(player: Int) {
        _outFirst = player
        tichuScore1 = 0
        tichuScore2 = 0
        if (isAddOnFailure) {
            if (tichu[0]) { if (_outFirst == 0) tichuScore1 += 100 else tichuScore2 += 100 }
            if (tichu[2]) { if (_outFirst == 2) tichuScore1 += 100 else tichuScore2 += 100 }
            if (tichu[1]) { if (_outFirst == 1) tichuScore2 += 100 else tichuScore1 += 100 }
            if (tichu[3]) { if (_outFirst == 3) tichuScore2 += 100 else tichuScore1 += 100 }
            if (grandTichu[0]) { if (_outFirst == 0) tichuScore1 += 200 else tichuScore2 += 200 }
            if (grandTichu[2]) { if (_outFirst == 2) tichuScore1 += 200 else tichuScore2 += 200 }
            if (grandTichu[1]) { if (_outFirst == 1) tichuScore2 += 200 else tichuScore1 += 200 }
            if (grandTichu[3]) { if (_outFirst == 3) tichuScore2 += 200 else tichuScore1 += 200 }
        } else {
            if (tichu[0]) tichuScore1 += if (_outFirst == 0) 100 else -100
            if (tichu[2]) tichuScore1 += if (_outFirst == 2) 100 else -100
            if (tichu[1]) tichuScore2 += if (_outFirst == 1) 100 else -100
            if (tichu[3]) tichuScore2 += if (_outFirst == 3) 100 else -100
            if (grandTichu[0]) tichuScore1 += if (_outFirst == 0) 200 else -200
            if (grandTichu[2]) tichuScore1 += if (_outFirst == 2) 200 else -200
            if (grandTichu[1]) tichuScore2 += if (_outFirst == 1) 200 else -200
            if (grandTichu[3]) tichuScore2 += if (_outFirst == 3) 200 else -200
        }
        totalScore1 = _cardScore1 + tichuScore1
        totalScore2 = _cardScore2 + tichuScore2
    }

    /** Set cardScore1 without recalculating totals (use when loading persisted data). */
    fun setCardScore1Direct(v: Int) { _cardScore1 = v }
    /** Set cardScore2 without recalculating totals (use when loading persisted data). */
    fun setCardScore2Direct(v: Int) { _cardScore2 = v }
    /** Set outFirst without recalculating tichu scores (use when loading persisted data). */
    fun setOutFirstDirect(v: Int) { _outFirst = v }
    /** Set tichu flag directly without recalculating scores (use when loading persisted data). */
    fun setTichuDirect(player: Int, v: Boolean) { tichu[player] = v }
    /** Set grand tichu flag directly without recalculating scores (use when loading persisted data). */
    fun setGrandTichuDirect(player: Int, v: Boolean) { grandTichu[player] = v }
}
