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
import android.widget.Button
import android.widget.TextView
import com.tichuguru.model.Player
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sign

class StatsFragment : Fragment() {
    private var adapter: StatsAdapter? = null
    private lateinit var viewModel: TGViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val statsList = view.findViewById<RecyclerView>(R.id.statsList)
        statsList.layoutManager = LinearLayoutManager(requireContext())
        view.findViewById<View>(R.id.statsClearAll).setOnClickListener { onClearStats() }
        viewModel = ViewModelProvider(requireActivity())[TGViewModel::class.java]
        viewModel.getAllPlayers().observe(viewLifecycleOwner) { players ->
            if (adapter == null || adapter!!.itemCount != players.size + 11) {
                adapter = StatsAdapter(players)
                statsList.adapter = adapter
            }
        }
    }

    private fun onClearStats() {
        AlertDialog.Builder(requireContext())
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.clearAllPlayerStats()
                adapter?.notifyDataSetChanged()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private inner class StatsAdapter(private val players: List<Player>) : RecyclerView.Adapter<StatsAdapter.ViewHolder>() {

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val name: TextView        = v.findViewById(R.id.statsName)
            val expandButton: Button  = v.findViewById(R.id.statsExpandButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.statsrow, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val statLabels = arrayOf("Win %", "Pts / Hand", "Card Pts / Hand",
                "Hands / Double Win", "Tichu %", "Grand Tichu %",
                "Tichu Efficiency", "Tichu Stop %", "Partner Tichu %")

            val label = when {
                position == 0               -> "Players"
                position <= players.size    -> players[position - 1].name
                position == players.size + 1 -> "Rankings"
                else                         -> statLabels[position - players.size - 2]
            }
            holder.name.text = label

            when {
                position == 0 || position == players.size + 1 -> {
                    holder.name.setTypeface(null, Typeface.BOLD)
                    holder.expandButton.visibility = View.INVISIBLE
                }
                position <= players.size -> {
                    holder.name.setTypeface(null, Typeface.NORMAL)
                    holder.expandButton.visibility = View.VISIBLE
                    holder.expandButton.setOnClickListener(PlayerExpandListener(players[position - 1]))
                }
                else -> {
                    holder.name.setTypeface(null, Typeface.NORMAL)
                    holder.expandButton.visibility = View.VISIBLE
                    val num = (position - players.size) - 2
                    holder.expandButton.setOnClickListener(when (num) {
                        0 -> RankExpandListener("Win %",              true,  Player::getWinPct,         Player::numWins,                   Player::numGames)
                        1 -> RankExpandListener("Pts / Hand",         true,  Player::getPtsPerHand,     Player::numHands,                  null)
                        2 -> RankExpandListener("Card Pts / Hand",    true,  Player::getCardPtsPerHand, Player::numHands,                  null)
                        3 -> RankExpandListener("Hands / DW",         false, Player::getHandsPerDW,     Player::numDoubleWins,             null)
                        4 -> RankExpandListener("Tichu %",            true,  Player::getTichuPct,       Player::numTichuMade,              Player::numTichuCalled)
                        5 -> RankExpandListener("Grand Tichu %",      true,  Player::getGTPct,          Player::numGTMade,                 Player::numGTCalled)
                        6 -> RankExpandListener("Tichu Efficiency",   true,  Player::getTichuEfficiency,Player::tichuEfficiencyHands,      null)
                        7 -> RankExpandListener("Tichu Stop %",       true,  Player::getTichuStopPct,   Player::numTichusStopped,          Player::numTichusCalledByOpps)
                        8 -> RankExpandListener("Partner Tichu %",    true,  Player::getPartnerTichuPct,Player::numTichusMadeByPartner,    Player::numTichusCalledByPartner)
                        else -> throw RuntimeException("Unknown rank index: $num")
                    })
                }
            }
        }

        override fun getItemCount() = players.size + 11

        inner class PlayerExpandListener(private val player: Player) : View.OnClickListener {
            override fun onClick(v: View) {
                val labels = arrayOf("Games", "# Played", "Win %", "Hands", "# Played",
                    "Average Pts / Hand", "Average Card Pts / Hand", "# Double Wins",
                    "Hands / Double Win", "Tichus", "# of Tichus", "% Tichus Made",
                    "Non-Calls", "Tichu Efficiency", "# of Grand Tichus",
                    "% Grand Tichus Made", "# of Tichus Stopped", "Tichu Stop %",
                    "Partner Tichu %")
                val values = arrayOfNulls<String>(labels.size)
                values[1]  = player.numGames.toString()
                values[2]  = "%.2f".format(player.getWinPct())
                values[4]  = player.numHands.toString()
                values[5]  = "%.2f".format(player.getPtsPerHand())
                values[6]  = "%.2f".format(player.getCardPtsPerHand())
                values[7]  = player.numDoubleWins.toString()
                values[8]  = "%.2f".format(player.getHandsPerDW())
                values[10] = player.numTichuCalled.toString()
                values[11] = "%.2f".format(player.getTichuPct())
                values[12] = player.nonCalls().toString()
                values[13] = "%.2f".format(player.getTichuEfficiency())
                values[14] = player.numGTCalled.toString()
                values[15] = "%.2f".format(player.getGTPct())
                values[16] = player.numTichusStopped.toString()
                values[17] = "%.2f".format(player.getTichuStopPct())
                values[18] = "%.2f".format(player.getPartnerTichuPct())

                (v.context as TGActivity).pushFragment(
                    StatsListFragment.newInstance("Stats for ${player.name}", labels, values, player)
                )
            }
        }

        inner class RankExpandListener(
            private val title: String,
            private val sortDescending: Boolean,
            private val rankValueGetter: (Player) -> Double,
            private val extraGetter1: (Player) -> Int,
            private val extraGetter2: ((Player) -> Int)?
        ) : View.OnClickListener {

            override fun onClick(v: View) {
                val sorted = players.toMutableList()
                sorted.sortWith { p1, p2 ->
                    val diff = rankValueGetter(p1) - rankValueGetter(p2)
                    (if (sortDescending) -diff else diff).sign.toInt()
                }

                var digits = 1
                for (p in sorted) {
                    val g = extraGetter2 ?: extraGetter1
                    val log = log10(g(p).toDouble()) + 1.0
                    if (!log.isNaN()) digits = max(digits, log.toInt())
                }
                val fmt = if (extraGetter2 != null)
                    String.format("%%.2f%%%% (%%0%dd/%%0%dd)", digits, digits)
                else
                    String.format("%%.2f (%%0%dd)", digits)

                val names  = Array(sorted.size) { sorted[it].name }
                val values = Array<String?>(sorted.size) { i ->
                    val p = sorted[i]
                    if (extraGetter2 != null)
                        String.format(fmt, rankValueGetter(p), extraGetter1(p), extraGetter2(p))
                    else
                        String.format(fmt, rankValueGetter(p), extraGetter1(p))
                }

                (v.context as TGActivity).pushFragment(
                    StatsListFragment.newInstance(title, names, values, null)
                )
            }
        }
    }
}
