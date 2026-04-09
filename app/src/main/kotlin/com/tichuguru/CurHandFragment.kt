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
import com.tichuguru.databinding.CurhandBinding
import com.tichuguru.model.Game
import com.tichuguru.model.Hand

class CurHandFragment : Fragment(), MenuProvider {
    private lateinit var viewModel: TGViewModel
    private lateinit var binding: CurhandBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = CurhandBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.curHandScoreHand.isEnabled = false
        binding.curHandScoreHand.setOnClickListener { onScoreHand() }
        binding.curHandNewGame.setOnClickListener { onNewGame() }

        if (savedInstanceState != null) {
            binding.curHandP1RG.check(savedInstanceState.getInt("tichu1", R.id.curHandP1None))
            binding.curHandP2RG.check(savedInstanceState.getInt("tichu2", R.id.curHandP2None))
            binding.curHandP3RG.check(savedInstanceState.getInt("tichu3", R.id.curHandP3None))
            binding.curHandP4RG.check(savedInstanceState.getInt("tichu4", R.id.curHandP4None))
        }

        viewModel = ViewModelProvider(requireActivity())[TGViewModel::class.java]
        viewModel.getCurrentGame().observe(viewLifecycleOwner) { updateDisplay() }
        viewModel.getClearTichuButtons().observe(viewLifecycleOwner) { clear ->
            if (clear) clearTichuButtons()
        }

        parentFragmentManager.setFragmentResultListener("score_hand", viewLifecycleOwner) { _, _ ->
            clearTichuButtons()
        }
        parentFragmentManager.setFragmentResultListener("new_game", viewLifecycleOwner) { _, _ ->
            clearTichuButtons()
        }

        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::binding.isInitialized) {
            outState.putInt("tichu1", binding.curHandP1RG.checkedRadioButtonId)
            outState.putInt("tichu2", binding.curHandP2RG.checkedRadioButtonId)
            outState.putInt("tichu3", binding.curHandP3RG.checkedRadioButtonId)
            outState.putInt("tichu4", binding.curHandP4RG.checkedRadioButtonId)
        }
        super.onSaveInstanceState(outState)
    }

    private fun onNewGame() {
        val game = viewModel.getCurrentGame().value ?: return
        (requireActivity() as TGActivity).pushFragment(
            NewGameFragment.newInstance(Game(game), viewModel.getAllPlayers().value ?: emptyList())
        )
    }

    private fun onEndGame() {
        val game = viewModel.getCurrentGame().value ?: return
        if (game.score1 == game.score2) {
            AlertDialog.Builder(requireContext()).setMessage("You can't end the game when the score is tied.").show()
            return
        }
        AlertDialog.Builder(requireContext())
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.endGame()
                updateDisplay()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun onScoreHand() {
        val game = viewModel.getCurrentGame().value ?: return
        val hand = Hand(game.addOnFailure)
        if (binding.curHandP1RG.checkedRadioButtonId == R.id.curHandP1GT) hand.setGrandTichuFor(0)
        if (binding.curHandP1RG.checkedRadioButtonId == R.id.curHandP1T)  hand.setTichuFor(0)
        if (binding.curHandP2RG.checkedRadioButtonId == R.id.curHandP2GT) hand.setGrandTichuFor(1)
        if (binding.curHandP2RG.checkedRadioButtonId == R.id.curHandP2T)  hand.setTichuFor(1)
        if (binding.curHandP3RG.checkedRadioButtonId == R.id.curHandP3GT) hand.setGrandTichuFor(2)
        if (binding.curHandP3RG.checkedRadioButtonId == R.id.curHandP3T)  hand.setTichuFor(2)
        if (binding.curHandP4RG.checkedRadioButtonId == R.id.curHandP4GT) hand.setGrandTichuFor(3)
        if (binding.curHandP4RG.checkedRadioButtonId == R.id.curHandP4T)  hand.setTichuFor(3)
        val playerNames = Array(4) { game.players[it].name }
        (requireActivity() as TGActivity).pushFragment(ScoreHandFragment.newInstance(hand, playerNames))
    }

    private fun clearTichuButtons() {
        binding.curHandP1RG.check(R.id.curHandP1None)
        binding.curHandP2RG.check(R.id.curHandP2None)
        binding.curHandP3RG.check(R.id.curHandP3None)
        binding.curHandP4RG.check(R.id.curHandP4None)
    }

    private fun updateDisplay() {
        val game = viewModel.getCurrentGame().value
        if (game == null) {
            binding.curHandScoreHand.isEnabled = false
            return
        }
        binding.curHandScore1.text = game.score1.toString()
        binding.curHandScore2.text = game.score2.toString()
        val players = game.players
        binding.curHandP1.text = players[0].name
        binding.curHandP2.text = players[1].name
        binding.curHandP3.text = players[2].name
        binding.curHandP4.text = players[3].name
        if (game.gameOver) {
            val team1wins = game.score1 > game.score2
            val winColor  = Color.YELLOW
            val loseColor = Color.GRAY
            binding.curHandScore1.setTextColor(if (team1wins) winColor else loseColor)
            binding.curHandP1.setTextColor(if (team1wins) winColor else loseColor)
            binding.curHandP3.setTextColor(if (team1wins) winColor else loseColor)
            binding.curHandScore2.setTextColor(if (team1wins) loseColor else winColor)
            binding.curHandP2.setTextColor(if (team1wins) loseColor else winColor)
            binding.curHandP4.setTextColor(if (team1wins) loseColor else winColor)
            binding.curHandScoreHand.isEnabled = false
        } else {
            val color = Color.GRAY
            binding.curHandScore1.setTextColor(color)
            binding.curHandP1.setTextColor(color)
            binding.curHandP3.setTextColor(color)
            binding.curHandScore2.setTextColor(color)
            binding.curHandP2.setTextColor(color)
            binding.curHandP4.setTextColor(color)
            binding.curHandScoreHand.isEnabled = true
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
