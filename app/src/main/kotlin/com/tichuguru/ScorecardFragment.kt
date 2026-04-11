package com.tichuguru

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tichuguru.databinding.ScorecardBinding
import com.tichuguru.databinding.ScorecardrowBinding
import com.tichuguru.model.Game

class ScorecardFragment : Fragment() {
    private lateinit var viewModel: TGViewModel
    private lateinit var binding: ScorecardBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = ScorecardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.scorecardList.layoutManager = LinearLayoutManager(requireContext())
        binding.scorecardDelete.setOnClickListener { onDeleteHand() }
        viewModel = ViewModelProvider(requireActivity())[TGViewModel::class.java]
        viewModel.getCurrentGame().observe(viewLifecycleOwner) { refreshDisplay() }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) refreshDisplay()
    }

    private fun refreshDisplay() {
        val game = viewModel.getCurrentGame().value ?: return
        val players = game.players
        binding.scorecardName1.text = players[0].name
        binding.scorecardName2.text = players[1].name
        binding.scorecardName3.text = players[2].name
        binding.scorecardName4.text = players[3].name
        binding.scorecardList.adapter = ScorecardAdapter(game)
    }

    private fun onDeleteHand() {
        AlertDialog
            .Builder(requireContext())
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { _, _ -> viewModel.deleteLastHand() }
            .setNegativeButton("No", null)
            .show()
    }

    private class ScorecardAdapter(private val game: Game) : RecyclerView.Adapter<ScorecardAdapter.ViewHolder>() {
        class ViewHolder(val binding: ScorecardrowBinding) : RecyclerView.ViewHolder(binding.root) {
            val tichus =
                arrayOf(
                    binding.scorecardTichu1,
                    binding.scorecardTichu2,
                    binding.scorecardTichu3,
                    binding.scorecardTichu4
                )
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): ViewHolder = ViewHolder(ScorecardrowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int,
        ) {
            val hand = game.hands[position]
            val s1 = hand.totalScore1
            holder.binding.scorecardHandScore1.text = "${if (s1 >= 0) "+" else ""}$s1"
            val s2 = hand.totalScore2
            holder.binding.scorecardHandScore2.text = "${if (s2 >= 0) "+" else ""}$s2"

            for (i in 0..3) {
                val tv = holder.tichus[i]
                tv.text =
                    when {
                        hand.isTichuFor(i) -> "T"
                        hand.isGrandTichuFor(i) -> "GT"
                        else -> ""
                    }
                tv.setTextColor(if (hand.outFirst == i) 0xFF00AA00.toInt() else Color.RED)
            }

            var t1 = 0
            var t2 = 0
            for (i in 0..position) {
                t1 += game.hands[i].totalScore1
                t2 += game.hands[i].totalScore2
            }
            holder.binding.scorecardTotalScore1.text = t1.toString()
            holder.binding.scorecardTotalScore2.text = t2.toString()
        }

        override fun getItemCount() = game.hands.size
    }
}
