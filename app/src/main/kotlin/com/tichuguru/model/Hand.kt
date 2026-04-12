package com.tichuguru.model

import com.tichuguru.model.Hand.Companion.CARD_SCORE_OPTIONS
import java.io.Serializable

class Hand : Serializable {
    enum class Bid(val points: Int) {
        NONE(0),
        TICHU(100),
        GRAND_TICHU(200),
    }

    companion object {
        /** All card score values selectable during scoring (-25 to 125 in steps of 5, plus 200 for double win). */
        val CARD_SCORE_OPTIONS: IntArray =
            intArrayOf(
                -25,
                -20,
                -15,
                -10,
                -5,
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
                105,
                110,
                115,
                120,
                125,
                200
            )

        /** Returns the complementary card score for the other team (scores always sum to 100; double win yields 0). */
        fun otherCardScore(score: Int) = if (score == 200) 0 else 100 - score

        /** Returns the index of a card score in [CARD_SCORE_OPTIONS]. */
        fun cardScoreIndex(score: Int) = if (score == 200) 31 else (score + 25) / 5
    }

    var dbId: Long = 0
    private val bids = Array(4) { Bid.NONE }
    var playerOutFirst: Int = 0
    var cardScoreTeamOne: Int = 0
    var cardScoreTeamTwo: Int = 0

    fun tichuScoreTeamOne(addOnFailure: Boolean): Int = computeTichuScore(forTeam1 = true, addOnFailure = addOnFailure)

    fun tichuScoreTeamTwo(addOnFailure: Boolean): Int = computeTichuScore(forTeam1 = false, addOnFailure = addOnFailure)

    fun totalScoreTeamOne(addOnFailure: Boolean): Int = cardScoreTeamOne + tichuScoreTeamOne(addOnFailure)

    fun totalScoreTeamTwo(addOnFailure: Boolean): Int = cardScoreTeamTwo + tichuScoreTeamTwo(addOnFailure)

    fun setTichuFor(player: Int) {
        bids[player] = Bid.TICHU
    }

    fun setGrandTichuFor(player: Int) {
        bids[player] = Bid.GRAND_TICHU
    }

    fun isTichuFor(player: Int) = bids[player] == Bid.TICHU

    fun isGrandTichuFor(player: Int) = bids[player] == Bid.GRAND_TICHU

    private fun computeTichuScore(
        forTeam1: Boolean,
        addOnFailure: Boolean,
    ): Int =
        (0..3).sumOf { player ->
            val isTeam1 = player % 2 == 0
            val made = playerOutFirst == player
            val points = bids[player].points
            when {
                addOnFailure -> if (made == (isTeam1 == forTeam1)) points else 0
                isTeam1 == forTeam1 -> if (made) points else -points
                else -> 0
            }
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
