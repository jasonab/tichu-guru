package com.tichuguru

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.core.os.BundleCompat
import com.tichuguru.databinding.ScorehandBinding
import com.tichuguru.model.Hand

class ScoreHandFragment : Fragment() {
    private lateinit var hand: Hand
    private lateinit var binding: ScorehandBinding

    companion object {
        private const val ARG_HAND         = "hand"
        private const val ARG_PLAYER_NAMES = "playerNames"

        fun newInstance(hand: Hand, playerNames: Array<String>): ScoreHandFragment {
            return ScoreHandFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_HAND, hand)
                    putStringArray(ARG_PLAYER_NAMES, playerNames)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ScorehandBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = "Score Hand"

        hand = requireNotNull(BundleCompat.getSerializable(requireArguments(), ARG_HAND, Hand::class.java)) { "hand arg missing" }
        val playerNames = requireNotNull(requireArguments().getStringArray(ARG_PLAYER_NAMES)) { "playerNames arg missing" }

        val scoreLabels = Array(Hand.CARD_SCORE_OPTIONS.size) { Hand.CARD_SCORE_OPTIONS[it].toString() }

        val changeListener = NumberPicker.OnValueChangeListener { picker, _, _ ->
            if (picker != binding.scoreHandOutFirst) {
                val other = if (picker == binding.scoreHandScore1) binding.scoreHandScore2 else binding.scoreHandScore1
                val v = Hand.CARD_SCORE_OPTIONS[picker.value]
                val otherVal = Hand.otherCardScore(v)
                if (v != 0 || other.value != Hand.cardScoreIndex(200)) {
                    other.value = Hand.cardScoreIndex(otherVal)
                }
            }
            updateHandScore()
        }

        binding.scoreHandScore1.minValue = 0
        binding.scoreHandScore1.maxValue = Hand.CARD_SCORE_OPTIONS.size - 1
        binding.scoreHandScore1.displayedValues = scoreLabels
        binding.scoreHandScore1.wrapSelectorWheel = false
        binding.scoreHandScore1.setOnValueChangedListener(changeListener)

        binding.scoreHandScore2.minValue = 0
        binding.scoreHandScore2.maxValue = Hand.CARD_SCORE_OPTIONS.size - 1
        binding.scoreHandScore2.displayedValues = scoreLabels
        binding.scoreHandScore2.wrapSelectorWheel = false
        binding.scoreHandScore2.setOnValueChangedListener(changeListener)

        binding.scoreHandOutFirst.minValue = 0
        binding.scoreHandOutFirst.maxValue = 3
        binding.scoreHandOutFirst.displayedValues = playerNames
        binding.scoreHandOutFirst.wrapSelectorWheel = true
        binding.scoreHandOutFirst.setOnValueChangedListener(changeListener)

        binding.scoreHandSave.setOnClickListener { onSave() }

        binding.scoreHandName1.text = playerNames[0]
        binding.scoreHandName2.text = playerNames[1]
        binding.scoreHandName3.text = playerNames[2]
        binding.scoreHandName4.text = playerNames[3]

        binding.scoreHandScore1.value = Hand.cardScoreIndex(50)
        binding.scoreHandScore2.value = Hand.cardScoreIndex(50)
        for (i in 0..3) {
            if (hand.isTichuFor(i) || hand.isGrandTichuFor(i)) {
                binding.scoreHandOutFirst.value = i
                break
            }
        }
        updateHandScore()
    }

    private fun updateHandScore() {
        hand.setCardScore1(Hand.CARD_SCORE_OPTIONS[binding.scoreHandScore1.value])
        hand.setCardScore2(Hand.CARD_SCORE_OPTIONS[binding.scoreHandScore2.value])
        hand.setOutFirst(binding.scoreHandOutFirst.value)
        binding.scoreHandTotal1.text = hand.totalScore1.toString()
        binding.scoreHandTotal2.text = hand.totalScore2.toString()
    }

    private fun onSave() {
        ViewModelProvider(requireActivity())[TGViewModel::class.java].scoreHand(hand)
        parentFragmentManager.setFragmentResult("score_hand", Bundle())
        parentFragmentManager.popBackStack()
    }
}
