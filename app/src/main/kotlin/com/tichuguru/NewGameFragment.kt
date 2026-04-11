package com.tichuguru

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.tichuguru.databinding.NewgameBinding
import com.tichuguru.model.Game
import com.tichuguru.model.Player

class NewGameFragment : Fragment() {
    private var addingPlayer = false
    private lateinit var allPlayers: MutableList<Player>
    private lateinit var game: Game
    private lateinit var nameSpinners: List<Spinner>
    private lateinit var spinAdapter: ArrayAdapter<String>
    private lateinit var viewModel: TGViewModel
    private lateinit var binding: NewgameBinding

    companion object {
        private const val ARG_GAME = "game"
        private const val ARG_PLAYERS = "players"

        fun newInstance(
            game: Game,
            players: List<Player>,
        ): NewGameFragment =
            NewGameFragment().apply {
                arguments =
                    Bundle().apply {
                        putSerializable(ARG_GAME, game)
                        putSerializable(ARG_PLAYERS, ArrayList(players))
                    }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = NewgameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = "New Game"
        viewModel = ViewModelProvider(requireActivity())[TGViewModel::class.java]

        game = requireNotNull(BundleCompat.getSerializable(requireArguments(), ARG_GAME, Game::class.java)) { "game arg missing" }
        @Suppress("UNCHECKED_CAST")
        allPlayers =
            (
                requireNotNull(BundleCompat.getSerializable(requireArguments(), ARG_PLAYERS, ArrayList::class.java)) {
                    "players arg missing"
                } as ArrayList<Player>
            ).toMutableList()

        binding.newGameRandomizeTeams.setOnClickListener { onRandomizeTeams() }
        binding.newGameStart.setOnClickListener { onStartGame() }

        nameSpinners =
            listOf(
                binding.newGameName1,
                binding.newGameName2,
                binding.newGameName3,
                binding.newGameName4
            )

        val choices = allPlayers.map { it.name }.toMutableList()
        choices.add("New Player")
        spinAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, choices)
        for (i in 0..3) {
            nameSpinners[i].onItemSelectedListener = PlayerSelectedListener(i)
            nameSpinners[i].adapter = spinAdapter
        }
        updateNameSpinners()
        binding.newGameAffectsStats.isChecked = true
        binding.newGameAddOnFailedTichu.isChecked = game.addOnFailure
        binding.newGameMercyRule.isChecked = game.mercyRule
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
            limit = Integer.parseInt(binding.newGameGameLimit.text.toString())
        } catch (e: Exception) {
            AlertDialog.Builder(requireContext()).setMessage("Enter a valid game limit").show()
            return
        }
        if (limit < 1) {
            AlertDialog.Builder(requireContext()).setMessage("Enter a valid game limit").show()
            return
        }
        game.gameLimit = limit
        game.addOnFailure = binding.newGameAddOnFailedTichu.isChecked
        game.ignoreStats = !binding.newGameAffectsStats.isChecked
        game.mercyRule = binding.newGameMercyRule.isChecked
        viewModel.addGame(game)
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
        override fun onItemSelected(
            parent: AdapterView<*>,
            view: View?,
            index: Int,
            id: Long,
        ) {
            if (index < allPlayers.size) {
                game.setPlayer(playerNum, allPlayers[index])
            } else if (!addingPlayer) {
                addingPlayer = true
                getNewPlayerName()
            }
        }

        private fun getNewPlayerName() {
            val input = android.widget.EditText(requireContext())
            AlertDialog
                .Builder(requireContext())
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
                    var newPlayer = allPlayers.find { it.name == name }
                    if (newPlayer != null) {
                        AlertDialog.Builder(requireContext()).setMessage("That player already exists!").show()
                    } else {
                        newPlayer = Player(name)
                        val insertIndex = allPlayers.indexOfFirst { it.name > newPlayer.name }.takeIf { it >= 0 } ?: allPlayers.size
                        allPlayers.add(insertIndex, newPlayer)
                        spinAdapter.insert(newPlayer.name, insertIndex)
                        viewModel.addPlayer(newPlayer)
                    }
                    game.setPlayer(playerNum, newPlayer)
                    addingPlayer = false
                    requireActivity().runOnUiThread { updateNameSpinners() }
                }.setNegativeButton("Cancel") { _, _ -> addingPlayer = false }
                .show()
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }
}
