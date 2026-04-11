package com.tichuguru

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tichuguru.databinding.StatslistrowBinding
import com.tichuguru.model.Player

class StatsListFragment : Fragment() {
    private var player: Player? = null
    private lateinit var viewModel: TGViewModel

    companion object {
        private const val ARG_PLAYER_NAME = "playerName"

        fun newInstance(
            title: String,
            labels: Array<String>,
            values: Array<String?>,
            player: Player?,
        ): StatsListFragment =
            StatsListFragment().apply {
                arguments =
                    Bundle().apply {
                        putString("title", title)
                        putStringArray("labels", labels)
                        @Suppress("UNCHECKED_CAST")
                        putStringArray("values", values as Array<String>)
                        if (player != null) putString(ARG_PLAYER_NAME, player.name)
                    }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewModel = ViewModelProvider(requireActivity())[TGViewModel::class.java]
        val playerName = requireArguments().getString(ARG_PLAYER_NAME)
        player = if (playerName != null) viewModel.getPlayer(playerName) else null
        return inflater.inflate(if (player == null) R.layout.rankinglist else R.layout.statslist, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        requireActivity().title = args.getString("title")

        val labels = requireNotNull(args.getStringArray("labels")) { "labels arg missing" }

        @Suppress("UNCHECKED_CAST")
        val values = args.getStringArray("values") as Array<String?>

        val statsList = view.findViewById<RecyclerView>(R.id.statsList)
        statsList.layoutManager = LinearLayoutManager(requireContext())
        statsList.adapter = StatsAdapter(labels, values)

        if (player != null) {
            view.findViewById<View>(R.id.statsClearPlayerStats).setOnClickListener { onClearStats() }
            view.findViewById<View>(R.id.statsDelPlayer).setOnClickListener { onDeletePlayer() }
        }
    }

    private fun onClearStats() {
        AlertDialog
            .Builder(requireContext())
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.clearPlayerStats(checkNotNull(player) { "player not set" })
                parentFragmentManager.popBackStack()
            }.setNegativeButton("No", null)
            .show()
    }

    private fun onDeletePlayer() {
        AlertDialog
            .Builder(requireContext())
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deletePlayer(checkNotNull(player) { "player not set" })
                parentFragmentManager.popBackStack()
            }.setNegativeButton("No", null)
            .show()
    }

    private class StatsAdapter(private val labels: Array<String>, private val values: Array<String?>) :
        RecyclerView.Adapter<StatsAdapter.ViewHolder>() {
        class ViewHolder(val binding: StatslistrowBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): ViewHolder = ViewHolder(StatslistrowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int,
        ) {
            holder.binding.statsLabel.text = labels[position]
            val v = values[position]
            if (v != null) {
                holder.binding.statsLabel.setTypeface(null, Typeface.NORMAL)
                holder.binding.statsLabel.textSize = 18f
                holder.binding.statsValue.text = v
            } else {
                holder.binding.statsLabel.setTypeface(null, Typeface.BOLD)
                holder.binding.statsLabel.textSize = 24f
                holder.binding.statsValue.text = ""
            }
        }

        override fun getItemCount() = labels.size
    }
}
