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
        private var cumTotals1: IntArray = intArrayOf()
        private var cumTotals2: IntArray = intArrayOf()

        var game: Game? = null
            @Suppress("NotifyDataSetChanged")
            set(value) {
                field = value
                value?.let { g ->
                    cumTotals1 = IntArray(g.hands.size)
                    cumTotals2 = IntArray(g.hands.size)
                    var t1 = 0
                    var t2 = 0
                    g.hands.forEachIndexed { i, hand ->
                        t1 += hand.totalScoreTeamOne(g.addOnFailure)
                        t2 += hand.totalScoreTeamTwo(g.addOnFailure)
                        cumTotals1[i] = t1
                        cumTotals2[i] = t2
                    }
                } ?: run {
                    cumTotals1 = intArrayOf()
                    cumTotals2 = intArrayOf()
                }
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
            val game = checkNotNull(game) { "game not set" }
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

            holder.binding.scorecardTotalScore1.text = String.format(Locale.getDefault(), "%d", cumTotals1[position])
            holder.binding.scorecardTotalScore2.text = String.format(Locale.getDefault(), "%d", cumTotals2[position])
        }

        override fun getItemCount() = game?.hands?.size ?: 0
    }
}
