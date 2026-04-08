package com.tichuguru

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import androidx.core.os.BundleCompat
import com.tichuguru.model.Game
import com.tichuguru.model.Player
import java.util.Collections

class NewGameFragment : Fragment() {
    private var addingPlayer = false
    private lateinit var addOnFailedTichuCB: CheckBox
    private lateinit var affectStatsCB: CheckBox
    private lateinit var game: Game
    private lateinit var gameLimit: EditText
    private lateinit var mercyRuleCB: CheckBox
    private lateinit var nameSpinners: List<Spinner>
    private lateinit var spinAdapter: ArrayAdapter<String>

    companion object {
        private const val ARG_GAME = "game"

        fun newInstance(game: Game): NewGameFragment {
            return NewGameFragment().apply {
                arguments = Bundle().apply { putSerializable(ARG_GAME, game) }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.newgame, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = "New Game"

        game = BundleCompat.getSerializable(requireArguments(), ARG_GAME, Game::class.java)!!
        gameLimit          = view.findViewById(R.id.newGameGameLimit)
        addOnFailedTichuCB = view.findViewById(R.id.newGameAddOnFailedTichu)
        affectStatsCB      = view.findViewById(R.id.newGameAffectsStats)
        mercyRuleCB        = view.findViewById(R.id.newGameMercyRule)
        view.findViewById<View>(R.id.newGameRandomizeTeams).setOnClickListener { onRandomizeTeams() }
        view.findViewById<View>(R.id.newGameStart).setOnClickListener { onStartGame() }

        nameSpinners = listOf(
            view.findViewById(R.id.newGameName1),
            view.findViewById(R.id.newGameName2),
            view.findViewById(R.id.newGameName3),
            view.findViewById(R.id.newGameName4)
        )

        val allPlayers = TGApp.getPlayers()
        val choices = allPlayers.map { it.name }.toMutableList<String>()
        choices.add("New Player")
        spinAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, choices)
        for (i in 0..3) {
            nameSpinners[i].setOnItemSelectedListener(PlayerSelectedListener(i))
            nameSpinners[i].adapter = spinAdapter
        }
        updateNameSpinners()
        affectStatsCB.isChecked      = true
        addOnFailedTichuCB.isChecked = game.addOnFailure
        mercyRuleCB.isChecked        = game.mercyRule
    }

    private fun onRandomizeTeams() {
        val players = game.players
        for (i in 3 downTo 1) {
            val j = (Math.random() * (i + 1)).toInt()
            if (j != i) {
                val tmp = players[i]
                players[i] = players[j]
                players[j] = tmp
            }
        }
        updateNameSpinners()
    }

    private fun onStartGame() {
        val players = game.players
        for (i in 0 until 4) {
            for (j in i + 1 until 4) {
                if (players[i].name == players[j].name) {
                    AlertDialog.Builder(requireContext()).setMessage("You selected the same player twice").show()
                    return
                }
            }
        }
        val limit: Int
        try {
            limit = Integer.parseInt(gameLimit.text.toString())
        } catch (e: Exception) {
            AlertDialog.Builder(requireContext()).setMessage("Enter a valid game limit").show()
            return
        }
        if (limit < 1) {
            AlertDialog.Builder(requireContext()).setMessage("Enter a valid game limit").show()
            return
        }
        game.gameLimit    = limit
        game.addOnFailure = addOnFailedTichuCB.isChecked
        game.ignoreStats  = !affectStatsCB.isChecked
        game.mercyRule    = mercyRuleCB.isChecked
        (TGApp.getGames() as MutableList<Game>).add(game)
        TGApp.setGame(game)
        (requireActivity().application as TGApp).saveGames()
        parentFragmentManager.setFragmentResult("new_game", Bundle())
        parentFragmentManager.popBackStack()
    }

    private fun updateNameSpinners() {
        val players = game.players
        for (i in 0..3) {
            val name = players[i].name
            for (j in 0 until spinAdapter.count) {
                if (spinAdapter.getItem(j) == name) {
                    nameSpinners[i].setSelection(j)
                    break
                }
            }
        }
    }

    private inner class PlayerSelectedListener(private val playerNum: Int) : AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>, view: View?, index: Int, id: Long) {
            val allPlayers = TGApp.getPlayers()
            if (index < allPlayers.size) {
                game.setPlayer(playerNum, allPlayers[index])
            } else if (!addingPlayer) {
                addingPlayer = true
                getNewPlayerName()
            }
        }

        private fun getNewPlayerName() {
            val input = EditText(requireContext())
            AlertDialog.Builder(requireContext())
                .setTitle("New Player")
                .setMessage("Enter the new player's name")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("Ok") { _, _ ->
                    val name = input.text.toString()
                    if (name.isEmpty()) {
                        requireActivity().runOnUiThread { getNewPlayerName() }
                        return@setPositiveButton
                    }
                    val allPlayers = TGApp.getPlayers() as MutableList<Player>
                    var newPlayer = allPlayers.find { it.name == name }
                    if (newPlayer != null) {
                        AlertDialog.Builder(requireContext()).setMessage("That player already exists!").show()
                    } else {
                        newPlayer = Player(name)
                        allPlayers.add(newPlayer)
                        Collections.sort(allPlayers)
                        spinAdapter.insert(newPlayer.name, allPlayers.indexOf(newPlayer))
                        (requireActivity().application as TGApp).savePlayers()
                    }
                    game.setPlayer(playerNum, newPlayer)
                    addingPlayer = false
                    requireActivity().runOnUiThread { updateNameSpinners() }
                }
                .setNegativeButton("Cancel") { _, _ -> addingPlayer = false }
                .show()
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }
}
