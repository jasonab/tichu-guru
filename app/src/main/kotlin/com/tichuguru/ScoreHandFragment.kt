package com.tichuguru

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import com.tichuguru.model.Hand

class ScoreHandFragment : Fragment() {
    private lateinit var hand: Hand
    private lateinit var outFirst: NumberPicker
    private lateinit var score1: NumberPicker
    private lateinit var score2: NumberPicker
    private lateinit var total1: TextView
    private lateinit var total2: TextView

    companion object {
        private const val ARG_HAND = "hand"

        fun newInstance(hand: Hand): ScoreHandFragment {
            return ScoreHandFragment().apply {
                arguments = Bundle().apply { putSerializable(ARG_HAND, hand) }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.scorehand, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = "Score Hand"

        hand = androidx.core.os.BundleCompat.getSerializable(requireArguments(), ARG_HAND, Hand::class.java)!!
        total1 = view.findViewById(R.id.scoreHandTotal1)
        total2 = view.findViewById(R.id.scoreHandTotal2)

        val scoreLabels = Array(Hand.CARD_SCORE_OPTIONS.size) { Hand.CARD_SCORE_OPTIONS[it].toString() }

        val changeListener = NumberPicker.OnValueChangeListener { picker, _, _ ->
            if (picker != outFirst) {
                val other = if (picker == score1) score2 else score1
                val v = Hand.CARD_SCORE_OPTIONS[picker.value]
                val otherVal = Hand.otherCardScore(v)
                if (v != 0 || other.value != Hand.cardScoreIndex(200)) {
                    other.value = Hand.cardScoreIndex(otherVal)
                }
            }
            updateHandScore()
        }

        score1 = view.findViewById(R.id.scoreHandScore1)
        score1.minValue = 0
        score1.maxValue = Hand.CARD_SCORE_OPTIONS.size - 1
        score1.displayedValues = scoreLabels
        score1.wrapSelectorWheel = false
        score1.setOnValueChangedListener(changeListener)

        score2 = view.findViewById(R.id.scoreHandScore2)
        score2.minValue = 0
        score2.maxValue = Hand.CARD_SCORE_OPTIONS.size - 1
        score2.displayedValues = scoreLabels
        score2.wrapSelectorWheel = false
        score2.setOnValueChangedListener(changeListener)

        val players = TGApp.getGame()!!.players
        val names = Array(4) { players[it].name }

        outFirst = view.findViewById(R.id.scoreHandOutFirst)
        outFirst.minValue = 0
        outFirst.maxValue = 3
        outFirst.displayedValues = names
        outFirst.wrapSelectorWheel = true
        outFirst.setOnValueChangedListener(changeListener)

        view.findViewById<View>(R.id.scoreHandSave).setOnClickListener { onSave() }

        view.findViewById<TextView>(R.id.scoreHandName1).text = players[0].name
        view.findViewById<TextView>(R.id.scoreHandName2).text = players[1].name
        view.findViewById<TextView>(R.id.scoreHandName3).text = players[2].name
        view.findViewById<TextView>(R.id.scoreHandName4).text = players[3].name

        score1.value = Hand.cardScoreIndex(50)
        score2.value = Hand.cardScoreIndex(50)
        for (i in 0..3) {
            if (hand.isTichuFor(i) || hand.isGrandTichuFor(i)) {
                outFirst.value = i
                break
            }
        }
        updateHandScore()
    }

    private fun updateHandScore() {
        hand.setCardScore1(Hand.CARD_SCORE_OPTIONS[score1.value])
        hand.setCardScore2(Hand.CARD_SCORE_OPTIONS[score2.value])
        hand.setOutFirst(outFirst.value)
        total1.text = hand.totalScore1.toString()
        total2.text = hand.totalScore2.toString()
    }

    private fun onSave() {
        val app = requireActivity().application as TGApp
        TGApp.getGame()!!.scoreHand(hand)
        app.saveGames()
        app.savePlayers()
        parentFragmentManager.setFragmentResult("score_hand", Bundle())
        parentFragmentManager.popBackStack()
    }
}
