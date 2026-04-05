package com.tichuguru

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tichuguru.model.Game
import com.tichuguru.model.Player

class StatsListFragment : Fragment() {
    private var player: Player? = null

    companion object {
        fun newInstance(title: String, labels: Array<String>, values: Array<String?>, playerName: String?): StatsListFragment {
            return StatsListFragment().apply {
                arguments = Bundle().apply {
                    putString("title", title)
                    putStringArray("labels", labels)
                    @Suppress("UNCHECKED_CAST")
                    putStringArray("values", values as Array<String>)
                    if (playerName != null) putString("playerName", playerName)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val playerName = requireArguments().getString("playerName")
        player = if (playerName != null) TGApp.getPlayer(playerName) else null
        return inflater.inflate(if (player == null) R.layout.rankinglist else R.layout.statslist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        requireActivity().title = args.getString("title")

        val labels = args.getStringArray("labels")!!
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
        AlertDialog.Builder(requireContext())
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { _, _ ->
                player!!.clearStats()
                (requireActivity().application as TGApp).savePlayers()
                ViewModelProvider(requireActivity())[TGViewModel::class.java].notifyPlayersChanged()
                parentFragmentManager.popBackStack()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun onDeletePlayer() {
        AlertDialog.Builder(requireContext())
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { _, _ ->
                val p = player!!
                var updateSharedGame = false
                val games = TGApp.getGames() as MutableList<Game>
                for (i in games.indices.reversed()) {
                    val game = games[i]
                    if (game.containsPlayer(p)) {
                        games.removeAt(i)
                        if (game === TGApp.getGame()) updateSharedGame = true
                    }
                }
                if (updateSharedGame) {
                    TGApp.setGame(if (games.isEmpty()) null else games.last())
                }
                (TGApp.getPlayers() as MutableList<Player>).remove(p)
                val app = requireActivity().application as TGApp
                app.saveGames()
                app.savePlayers()
                val vm = ViewModelProvider(requireActivity())[TGViewModel::class.java]
                vm.notifyGamesChanged()
                vm.notifyPlayersChanged()
                parentFragmentManager.popBackStack()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private class StatsAdapter(
        private val labels: Array<String>,
        private val values: Array<String?>
    ) : RecyclerView.Adapter<StatsAdapter.ViewHolder>() {

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val label: TextView = v.findViewById(R.id.statsLabel)
            val value: TextView = v.findViewById(R.id.statsValue)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.statslistrow, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.label.text = labels[position]
            val v = values[position]
            if (v != null) {
                holder.label.setTypeface(null, Typeface.NORMAL)
                holder.label.textSize = 18f
                holder.value.text = v
            } else {
                holder.label.setTypeface(null, Typeface.BOLD)
                holder.label.textSize = 24f
                holder.value.text = ""
            }
        }

        override fun getItemCount() = labels.size
    }
}
