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
import android.widget.Button
import android.widget.TextView
import com.tichuguru.model.Game
import java.text.SimpleDateFormat

class AllGamesFragment : Fragment() {
    private lateinit var viewModel: TGViewModel
    private lateinit var gamesList: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.allgames, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gamesList = view.findViewById(R.id.gamesList)
        gamesList.layoutManager = LinearLayoutManager(requireContext())
        viewModel = ViewModelProvider(requireActivity())[TGViewModel::class.java]
        viewModel.getAllGames().observe(viewLifecycleOwner) { games -> gamesList.adapter = GamesAdapter(games) }
    }

    @SuppressLint("SimpleDateFormat")
    private inner class GamesAdapter(private val games: List<Game>) : RecyclerView.Adapter<GamesAdapter.ViewHolder>() {
        private val df = SimpleDateFormat("M/d")

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val date: TextView    = v.findViewById(R.id.gamesDate)
            val team1: TextView   = v.findViewById(R.id.gamesTeam1)
            val team2: TextView   = v.findViewById(R.id.gamesTeam2)
            val score1: TextView  = v.findViewById(R.id.gamesScore1)
            val score2: TextView  = v.findViewById(R.id.gamesScore2)
            val deleteBtn: Button = v.findViewById(R.id.gamesDeleteOne)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.allgamesrow, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val idx = games.size - position - 1
            val game = games[idx]
            val players = game.players

            holder.date.text   = df.format(game.date)
            holder.team1.text  = "${players[0].name} and ${players[2].name}"
            holder.team2.text  = "${players[1].name} and ${players[3].name}"
            holder.score1.text = game.score1.toString()
            holder.score2.text = game.score2.toString()

            if (game.gameOver) {
                val team1wins = game.score1 > game.score2
                holder.team1.setTextColor(if (team1wins) Color.YELLOW else Color.GRAY)
                holder.score1.setTextColor(if (team1wins) Color.YELLOW else Color.GRAY)
                holder.team2.setTextColor(if (team1wins) Color.GRAY else Color.YELLOW)
                holder.score2.setTextColor(if (team1wins) Color.GRAY else Color.YELLOW)
            } else {
                holder.team1.setTextColor(Color.GRAY)
                holder.score1.setTextColor(Color.GRAY)
                holder.team2.setTextColor(Color.GRAY)
                holder.score2.setTextColor(Color.GRAY)
            }

            holder.deleteBtn.setOnClickListener { onDeleteGame(games[idx]) }
            holder.itemView.setOnClickListener {
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
