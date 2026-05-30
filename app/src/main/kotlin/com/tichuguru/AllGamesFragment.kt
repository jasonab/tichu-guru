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
import com.tichuguru.databinding.AllgamesBinding
import com.tichuguru.databinding.AllgamesrowBinding
import com.tichuguru.model.Game
import com.tichuguru.model.isTeamOne
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal fun winColor(team1wins: Boolean): Int = if (team1wins) Color.YELLOW else Color.GRAY

internal fun Fragment.confirmAction(
    message: String,
    onConfirm: () -> Unit,
) {
    AlertDialog
        .Builder(requireContext())
        .setMessage(message)
        .setPositiveButton("Yes") { _, _ -> onConfirm() }
        .setNegativeButton("No", null)
        .show()
}

class AllGamesFragment : Fragment() {
    private lateinit var viewModel: TGViewModel
    private lateinit var binding: AllgamesBinding
    private lateinit var adapter: GamesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = AllgamesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.gamesList.layoutManager = LinearLayoutManager(requireContext())
        adapter = GamesAdapter()
        binding.gamesList.adapter = adapter
        viewModel = ViewModelProvider(requireActivity())[TGViewModel::class.java]
        viewModel.getAllGames().observe(viewLifecycleOwner) { games ->
            adapter.games = games.reversed()
        }
    }

    private inner class GamesAdapter : RecyclerView.Adapter<GamesAdapter.ViewHolder>() {
        var games: List<Game> = emptyList()
            @Suppress("NotifyDataSetChanged")
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        private val df = DateTimeFormatter.ofPattern("M/d").withZone(ZoneId.systemDefault())

        inner class ViewHolder(val binding: AllgamesrowBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): ViewHolder = ViewHolder(AllgamesrowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = games.size

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int,
        ) {
            val game = games[position]
            val players = game.players

            holder.binding.gamesDate.text = df.format(game.date)
            holder.binding.gamesTeam1.text = players.filterIndexed { i, _ -> isTeamOne(i) }.joinToString(" and ") { p -> p.name }
            holder.binding.gamesTeam2.text = players.filterIndexed { i, _ -> !isTeamOne(i) }.joinToString(" and ") { p -> p.name }
            holder.binding.gamesScore1.text = game.teamOneTotal.toString()
            holder.binding.gamesScore2.text = game.teamTwoTotal.toString()

            if (game.gameOver) {
                val team1wins = game.teamOneTotal > game.teamTwoTotal
                holder.binding.gamesTeam1.setTextColor(winColor(team1wins))
                holder.binding.gamesScore1.setTextColor(winColor(team1wins))
                holder.binding.gamesTeam2.setTextColor(winColor(!team1wins))
                holder.binding.gamesScore2.setTextColor(winColor(!team1wins))
            } else {
                holder.binding.gamesTeam1.setTextColor(Color.GRAY)
                holder.binding.gamesScore1.setTextColor(Color.GRAY)
                holder.binding.gamesTeam2.setTextColor(Color.GRAY)
                holder.binding.gamesScore2.setTextColor(Color.GRAY)
            }

            holder.binding.gamesDeleteOne.setOnClickListener { onDeleteGame(game) }
            holder.binding.root.setOnClickListener {
                viewModel.requestClearTichuButtons()
                viewModel.setGame(game)
                (requireActivity() as TGActivity).navigateToTab(0)
            }
        }
    }

    private fun onDeleteGame(game: Game) {
        confirmAction("Are you sure?") {
            viewModel.requestClearTichuButtons()
            viewModel.deleteGame(game)
            if (viewModel.getAllGames().value.isNullOrEmpty()) {
                (requireActivity() as TGActivity).createFirstGame()
            }
        }
    }
}
