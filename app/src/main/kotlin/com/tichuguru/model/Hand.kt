package com.tichuguru.model

import java.io.Serializable

class Hand(var isAddOnFailure: Boolean = false) : Serializable {
    enum class Bid(val points: Int) {
        NONE(0),
        TICHU(100),
        GRAND_TICHU(200),
    }

    companion object {
        /** All card score values selectable during scoring (0-100 in steps of 5, plus 200 for double win). */
        val CARD_SCORE_OPTIONS: IntArray =
            intArrayOf(
                0,
                5,
                10,
                15,
                20,
                25,
                30,
                35,
                40,
                45,
                50,
                55,
                60,
                65,
                70,
                75,
                80,
                85,
                90,
                95,
                100,
                200
            )

        /** Returns the complementary card score for the other team (scores always sum to 100; double win yields 0). */
        fun otherCardScore(score: Int) = if (score == 200) 0 else 100 - score

        /** Returns the index of a card score in [CARD_SCORE_OPTIONS]. */
        fun cardScoreIndex(score: Int) = if (score == 200) 21 else score / 5
    }

    var dbId: Long = 0
    private var bids = Array(4) { Bid.NONE }
    private var _outFirst: Int = 0
    private var _cardScore1: Int = 0
    private var _cardScore2: Int = 0
    var tichuScore1: Int = 0
    var tichuScore2: Int = 0
    var totalScore1: Int = 0
    var totalScore2: Int = 0

    val cardScore1: Int get() = _cardScore1
    val cardScore2: Int get() = _cardScore2

    val outFirst: Int get() = _outFirst

    fun setCardScore1(score: Int) {
        _cardScore1 = score
        totalScore1 = _cardScore1 + tichuScore1
    }

    fun setCardScore2(score: Int) {
        _cardScore2 = score
        totalScore2 = _cardScore2 + tichuScore2
    }

    fun setTichuFor(player: Int) {
        bids[player] = Bid.TICHU
    }

    fun setGrandTichuFor(player: Int) {
        bids[player] = Bid.GRAND_TICHU
    }

    fun isTichuFor(player: Int) = bids[player] == Bid.TICHU

    fun isGrandTichuFor(player: Int) = bids[player] == Bid.GRAND_TICHU

    fun setOutFirst(player: Int) {
        _outFirst = player
        tichuScore1 = 0
        tichuScore2 = 0
        for (i in 0..3) {
            val points = bids[i].points
            if (points > 0) applyBidPoints(i % 2 == 0, _outFirst == i, points)
        }
        totalScore1 = _cardScore1 + tichuScore1
        totalScore2 = _cardScore2 + tichuScore2
    }

    private fun applyBidPoints(
        isTeam1: Boolean,
        made: Boolean,
        points: Int,
    ) {
        if (isAddOnFailure) {
            val toTeam1 = if (made) isTeam1 else !isTeam1
            if (toTeam1) tichuScore1 += points else tichuScore2 += points
        } else {
            val delta = if (made) points else -points
            if (isTeam1) tichuScore1 += delta else tichuScore2 += delta
        }
    }

    /** Set cardScore1 without recalculating totals (use when loading persisted data). */
    fun setCardScore1Direct(v: Int) {
        _cardScore1 = v
    }

    /** Set cardScore2 without recalculating totals (use when loading persisted data). */
    fun setCardScore2Direct(v: Int) {
        _cardScore2 = v
    }

    /** Set outFirst without recalculating tichu scores (use when loading persisted data). */
    fun setOutFirstDirect(v: Int) {
        _outFirst = v
    }

    /** Set tichu flag directly without recalculating scores (use when loading persisted data). */
    fun setTichuDirect(
        player: Int,
        v: Boolean,
    ) {
        if (v) bids[player] = Bid.TICHU
    }

    /** Set grand tichu flag directly without recalculating scores (use when loading persisted data). */
    fun setGrandTichuDirect(
        player: Int,
        v: Boolean,
    ) {
        if (v) bids[player] = Bid.GRAND_TICHU
    }
}
