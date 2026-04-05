package com.tichuguru

import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import com.tichuguru.model.Game
import com.tichuguru.model.Hand

class CurHandFragment : Fragment(), MenuProvider {
    private lateinit var viewModel: TGViewModel
    private var grp1: RadioGroup? = null
    private var grp2: RadioGroup? = null
    private var grp3: RadioGroup? = null
    private var grp4: RadioGroup? = null
    private lateinit var name1: TextView
    private lateinit var name2: TextView
    private lateinit var name3: TextView
    private lateinit var name4: TextView
    private lateinit var score1: TextView
    private lateinit var score2: TextView
    private lateinit var scoreHandButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.curhand, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        score1 = view.findViewById(R.id.curHandScore1)
        score2 = view.findViewById(R.id.curHandScore2)
        name1 = view.findViewById(R.id.curHandP1)
        name2 = view.findViewById(R.id.curHandP2)
        name3 = view.findViewById(R.id.curHandP3)
        name4 = view.findViewById(R.id.curHandP4)
        grp1 = view.findViewById(R.id.curHandP1RG)
        grp2 = view.findViewById(R.id.curHandP2RG)
        grp3 = view.findViewById(R.id.curHandP3RG)
        grp4 = view.findViewById(R.id.curHandP4RG)
        scoreHandButton = view.findViewById(R.id.curHandScoreHand)
        scoreHandButton.setOnClickListener { onScoreHand() }
        view.findViewById<View>(R.id.curHandNewGame).setOnClickListener { onNewGame() }

        if (savedInstanceState != null) {
            grp1!!.check(savedInstanceState.getInt("tichu1", R.id.curHandP1None))
            grp2!!.check(savedInstanceState.getInt("tichu2", R.id.curHandP2None))
            grp3!!.check(savedInstanceState.getInt("tichu3", R.id.curHandP3None))
            grp4!!.check(savedInstanceState.getInt("tichu4", R.id.curHandP4None))
        }

        viewModel = ViewModelProvider(requireActivity())[TGViewModel::class.java]
        viewModel.getCurrentGame().observe(viewLifecycleOwner) { updateDisplay() }
        viewModel.getClearTichuButtons().observe(viewLifecycleOwner) { clear ->
            if (clear) clearTichuButtons()
        }

        parentFragmentManager.setFragmentResultListener("score_hand", viewLifecycleOwner) { _, _ ->
            clearTichuButtons()
            viewModel.notifyGameChanged()
        }
        parentFragmentManager.setFragmentResultListener("new_game", viewLifecycleOwner) { _, _ ->
            clearTichuButtons()
            viewModel.notifyGameChanged()
        }

        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        grp1?.let {
            outState.putInt("tichu1", grp1!!.checkedRadioButtonId)
            outState.putInt("tichu2", grp2!!.checkedRadioButtonId)
            outState.putInt("tichu3", grp3!!.checkedRadioButtonId)
            outState.putInt("tichu4", grp4!!.checkedRadioButtonId)
        }
        super.onSaveInstanceState(outState)
    }

    private fun onNewGame() {
        (requireActivity() as TGActivity).pushFragment(
            NewGameFragment.newInstance(Game(TGApp.getGame()!!))
        )
    }

    private fun onEndGame() {
        val game = TGApp.getGame()!!
        if (game.score1 == game.score2) {
            AlertDialog.Builder(requireContext()).setMessage("You can't end the game when the score is tied.").show()
            return
        }
        AlertDialog.Builder(requireContext())
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { _, _ ->
                TGApp.getGame()!!.endGame()
                val app = requireActivity().application as TGApp
                app.saveGames()
                app.savePlayers()
                updateDisplay()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun onScoreHand() {
        val hand = Hand(TGApp.getGame()!!)
        if (grp1!!.checkedRadioButtonId == R.id.curHandP1GT) hand.setGrandTichuFor(0)
        if (grp1!!.checkedRadioButtonId == R.id.curHandP1T)  hand.setTichuFor(0)
        if (grp2!!.checkedRadioButtonId == R.id.curHandP2GT) hand.setGrandTichuFor(1)
        if (grp2!!.checkedRadioButtonId == R.id.curHandP2T)  hand.setTichuFor(1)
        if (grp3!!.checkedRadioButtonId == R.id.curHandP3GT) hand.setGrandTichuFor(2)
        if (grp3!!.checkedRadioButtonId == R.id.curHandP3T)  hand.setTichuFor(2)
        if (grp4!!.checkedRadioButtonId == R.id.curHandP4GT) hand.setGrandTichuFor(3)
        if (grp4!!.checkedRadioButtonId == R.id.curHandP4T)  hand.setTichuFor(3)
        (requireActivity() as TGActivity).pushFragment(ScoreHandFragment.newInstance(hand))
    }

    private fun clearTichuButtons() {
        grp1?.check(R.id.curHandP1None)
        grp2?.check(R.id.curHandP2None)
        grp3?.check(R.id.curHandP3None)
        grp4?.check(R.id.curHandP4None)
    }

    private fun updateDisplay() {
        val game = TGApp.getGame() ?: return
        score1.text = game.score1.toString()
        score2.text = game.score2.toString()
        val players = game.players
        name1.text = players[0].name
        name2.text = players[1].name
        name3.text = players[2].name
        name4.text = players[3].name
        if (game.gameOver) {
            val team1wins = game.score1 > game.score2
            val winColor = Color.YELLOW
            val loseColor = Color.GRAY
            score1.setTextColor(if (team1wins) winColor else loseColor)
            name1.setTextColor(if (team1wins) winColor else loseColor)
            name3.setTextColor(if (team1wins) winColor else loseColor)
            score2.setTextColor(if (team1wins) loseColor else winColor)
            name2.setTextColor(if (team1wins) loseColor else winColor)
            name4.setTextColor(if (team1wins) loseColor else winColor)
            scoreHandButton.isEnabled = false
        } else {
            val color = Color.GRAY
            score1.setTextColor(color)
            name1.setTextColor(color)
            name3.setTextColor(color)
            score2.setTextColor(color)
            name2.setTextColor(color)
            name4.setTextColor(color)
            scoreHandButton.isEnabled = true
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_curhand, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
        R.id.menu_end_game -> { onEndGame(); true }
        R.id.menu_quit     -> { requireActivity().finish(); true }
        else               -> false
    }
}
