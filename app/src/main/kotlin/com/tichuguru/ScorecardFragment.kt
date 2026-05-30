package com.tichuguru

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tichuguru.databinding.ScorecardBinding
import com.tichuguru.databinding.ScorecardrowBinding
import com.tichuguru.model.Game
import java.util.Locale

class ScorecardFragment : Fragment() {
    private lateinit var viewModel: TGViewModel
    private lateinit var binding: ScorecardBinding
    private lateinit var adapter: ScorecardAdapter

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
        adapter = ScorecardAdapter()
        binding.scorecardList.adapter = adapter
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
        adapter.game = game
    }

    private fun onDeleteHand() {
        confirmAction("Are you sure?") { viewModel.deleteLastHand() }
    }

    private class ScorecardAdapter : RecyclerView.Adapter<ScorecardAdapter.ViewHolder>() {
        var game: Game? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

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
            val game = game ?: return
            val hand = game.hands[position]
            val s1 = hand.totalScoreTeamOne(game.addOnFailure)
            holder.binding.scorecardHandScore1.text = String.format(Locale.getDefault(), "%+d", s1)
            val s2 = hand.totalScoreTeamTwo(game.addOnFailure)
            holder.binding.scorecardHandScore2.text = String.format(Locale.getDefault(), "%+d", s2)

            holder.tichus.forEachIndexed { i, tv ->
                tv.text =
                    when {
                        hand.isTichuFor(i) -> "T"
                        hand.isGrandTichuFor(i) -> "GT"
                        else -> ""
                    }
                tv.setTextColor(if (hand.playerOutFirst == i) 0xFF00AA00.toInt() else Color.RED)
            }

            var t1 = 0
            var t2 = 0
            for (i in 0..position) {
                t1 += game.hands[i].totalScoreTeamOne(game.addOnFailure)
                t2 += game.hands[i].totalScoreTeamTwo(game.addOnFailure)
            }
            holder.binding.scorecardTotalScore1.text = String.format(Locale.getDefault(), "%d", t1)
            holder.binding.scorecardTotalScore2.text = String.format(Locale.getDefault(), "%d", t2)
        }

        override fun getItemCount() = game?.hands?.size ?: 0
    }
}
