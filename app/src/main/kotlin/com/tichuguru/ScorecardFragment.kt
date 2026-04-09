package com.tichuguru

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tichuguru.model.Game

class ScorecardFragment : Fragment() {
    private lateinit var viewModel: TGViewModel
    private lateinit var scorecardList: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.scorecard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scorecardList = view.findViewById(R.id.scorecardList)
        scorecardList.layoutManager = LinearLayoutManager(requireContext())
        view.findViewById<View>(R.id.scorecardDelete).setOnClickListener { onDeleteHand() }
        viewModel = ViewModelProvider(requireActivity())[TGViewModel::class.java]
        viewModel.getCurrentGame().observe(viewLifecycleOwner) { refreshDisplay() }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) refreshDisplay()
    }

    private fun refreshDisplay() {
        val game = viewModel.getCurrentGame().value ?: return
        val view = requireView()
        val players = game.players
        view.findViewById<TextView>(R.id.scorecardName1).text = players[0].name
        view.findViewById<TextView>(R.id.scorecardName2).text = players[1].name
        view.findViewById<TextView>(R.id.scorecardName3).text = players[2].name
        view.findViewById<TextView>(R.id.scorecardName4).text = players[3].name
        scorecardList.adapter = ScorecardAdapter(game)
    }

    private fun onDeleteHand() {
        AlertDialog.Builder(requireContext())
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { _, _ -> viewModel.deleteLastHand() }
            .setNegativeButton("No", null)
            .show()
    }

    private class ScorecardAdapter(private val game: Game) : RecyclerView.Adapter<ScorecardAdapter.ViewHolder>() {

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val score1: TextView = v.findViewById(R.id.scorecardHandScore1)
            val score2: TextView = v.findViewById(R.id.scorecardHandScore2)
            val total1: TextView = v.findViewById(R.id.scorecardTotalScore1)
            val total2: TextView = v.findViewById(R.id.scorecardTotalScore2)
            val tichus = arrayOf(
                v.findViewById<TextView>(R.id.scorecardTichu1),
                v.findViewById(R.id.scorecardTichu2),
                v.findViewById(R.id.scorecardTichu3),
                v.findViewById<TextView>(R.id.scorecardTichu4)
            )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.scorecardrow, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val hand = game.hands[position]
            val s1 = hand.totalScore1
            holder.score1.text = "${if (s1 >= 0) "+" else ""}$s1"
            val s2 = hand.totalScore2
            holder.score2.text = "${if (s2 >= 0) "+" else ""}$s2"

            for (i in 0..3) {
                val tv = holder.tichus[i]
                tv.text = when {
                    hand.isTichuFor(i)      -> "T"
                    hand.isGrandTichuFor(i) -> "GT"
                    else                    -> ""
                }
                tv.setTextColor(if (hand.outFirst() == i) 0xFF00AA00.toInt() else Color.RED)
            }

            var t1 = 0; var t2 = 0
            for (i in 0..position) {
                t1 += game.hands[i].totalScore1
                t2 += game.hands[i].totalScore2
            }
            holder.total1.text = t1.toString()
            holder.total2.text = t2.toString()
        }

        override fun getItemCount() = game.hands.size
    }
}
