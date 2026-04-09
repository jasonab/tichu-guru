package com.tichuguru

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tichuguru.databinding.AllgamesBinding
import com.tichuguru.databinding.AllgamesrowBinding
import com.tichuguru.model.Game
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AllGamesFragment : Fragment() {
    private lateinit var viewModel: TGViewModel
    private lateinit var binding: AllgamesBinding
    private lateinit var adapter: GamesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = AllgamesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.gamesList.layoutManager = LinearLayoutManager(requireContext())
        adapter = GamesAdapter()
        binding.gamesList.adapter = adapter
        viewModel = ViewModelProvider(requireActivity())[TGViewModel::class.java]
        viewModel.getAllGames().observe(viewLifecycleOwner) { games ->
            adapter.games = games
            adapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private inner class GamesAdapter : RecyclerView.Adapter<GamesAdapter.ViewHolder>() {
        var games: List<Game> = emptyList()
        private val df = DateTimeFormatter.ofPattern("M/d").withZone(ZoneId.systemDefault())

        inner class ViewHolder(val binding: AllgamesrowBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(AllgamesrowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val idx = games.size - position - 1
            val game = games[idx]
            val players = game.players

            holder.binding.gamesDate.text   = df.format(game.date)
            holder.binding.gamesTeam1.text  = "${players[0].name} and ${players[2].name}"
            holder.binding.gamesTeam2.text  = "${players[1].name} and ${players[3].name}"
            holder.binding.gamesScore1.text = game.score1.toString()
            holder.binding.gamesScore2.text = game.score2.toString()

            if (game.gameOver) {
                val team1wins = game.score1 > game.score2
                holder.binding.gamesTeam1.setTextColor(if (team1wins) Color.YELLOW else Color.GRAY)
                holder.binding.gamesScore1.setTextColor(if (team1wins) Color.YELLOW else Color.GRAY)
                holder.binding.gamesTeam2.setTextColor(if (team1wins) Color.GRAY else Color.YELLOW)
                holder.binding.gamesScore2.setTextColor(if (team1wins) Color.GRAY else Color.YELLOW)
            } else {
                holder.binding.gamesTeam1.setTextColor(Color.GRAY)
                holder.binding.gamesScore1.setTextColor(Color.GRAY)
                holder.binding.gamesTeam2.setTextColor(Color.GRAY)
                holder.binding.gamesScore2.setTextColor(Color.GRAY)
            }

            holder.binding.gamesDeleteOne.setOnClickListener { onDeleteGame(games[idx]) }
            holder.binding.root.setOnClickListener {
                viewModel.requestClearTichuButtons()
                viewModel.setGame(games[idx])
                (requireActivity() as TGActivity).navigateToTab(0)
            }
        }

        override fun getItemCount() = games.size
    }

    private fun onDeleteGame(game: Game) {
        AlertDialog.Builder(requireContext())
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.requestClearTichuButtons()
                viewModel.deleteGame(game)
                if (viewModel.getAllGames().value.isNullOrEmpty()) {
                    (requireActivity() as TGActivity).createFirstGame()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}
