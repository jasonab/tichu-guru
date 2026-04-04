package com.tichuguru;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import com.tichuguru.model.Game;
import com.tichuguru.model.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class NewGameFragment extends Fragment {
    private boolean addingPlayer = false;
    private CheckBox addOnFailedTichuCB;
    private CheckBox affectStatsCB;
    private Game game;
    private EditText gameLimit;
    private CheckBox mercyRuleCB;
    private List<Spinner> nameSpinners;
    private ArrayAdapter<String> spinAdapter;

    public static NewGameFragment newInstance(Game game) {
        NewGameFragment f = new NewGameFragment();
        Bundle args = new Bundle();
        args.putSerializable("newGame", game);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.newgame, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("New Game");

        game = (Game) requireArguments().getSerializable("newGame");
        gameLimit = view.findViewById(R.id.newGameGameLimit);
        addOnFailedTichuCB = view.findViewById(R.id.newGameAddOnFailedTichu);
        affectStatsCB = view.findViewById(R.id.newGameAffectsStats);
        mercyRuleCB = view.findViewById(R.id.newGameMercyRule);
        view.findViewById(R.id.newGameRandomizeTeams).setOnClickListener(v -> onRandomizeTeams());
        view.findViewById(R.id.newGameStart).setOnClickListener(v -> onStartGame());

        nameSpinners = new ArrayList<>();
        nameSpinners.add(view.findViewById(R.id.newGameName1));
        nameSpinners.add(view.findViewById(R.id.newGameName2));
        nameSpinners.add(view.findViewById(R.id.newGameName3));
        nameSpinners.add(view.findViewById(R.id.newGameName4));

        List<Player> allPlayers = TGApp.getPlayers();
        List<String> choices = new ArrayList<>();
        for (Player p : allPlayers) choices.add(p.getName());
        choices.add("New Player");
        spinAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, choices);
        for (int i = 0; i < 4; i++) {
            Spinner spin = nameSpinners.get(i);
            spin.setOnItemSelectedListener(new PlayerSelectedListener(i));
            spin.setAdapter(spinAdapter);
        }
        updateNameSpinners();
        affectStatsCB.setChecked(true);
        addOnFailedTichuCB.setChecked(game.isAddOnFailure());
        mercyRuleCB.setChecked(game.isMercyRule());
    }

    private void onRandomizeTeams() {
        Random rand = new Random();
        List<Player> players = game.getPlayers();
        for (int i = 3; i >= 1; i--) {
            int j = rand.nextInt(i + 1);
            if (j != i) {
                Player p = players.get(i);
                players.set(i, players.get(j));
                players.set(j, p);
            }
        }
        updateNameSpinners();
    }

    private void onStartGame() {
        List<Player> players = game.getPlayers();
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                if (players.get(i).getName().equals(players.get(j).getName())) {
                    new AlertDialog.Builder(requireContext()).setMessage("You selected the same player twice").show();
                    return;
                }
            }
        }
        int limit;
        boolean badParse = false;
        try {
            limit = Integer.parseInt(gameLimit.getText().toString());
        } catch (Exception e) {
            badParse = true;
            limit = 0;
        }
        if (badParse || limit < 1) {
            new AlertDialog.Builder(requireContext()).setMessage("Enter a valid game limit").show();
            return;
        }
        game.setGameLimit(limit);
        game.setAddOnFailure(addOnFailedTichuCB.isChecked());
        game.setIgnoreStats(!affectStatsCB.isChecked());
        game.setMercyRule(mercyRuleCB.isChecked());
        TGApp.getGames().add(game);
        TGApp.setGame(game);
        ((TGApp) requireActivity().getApplication()).saveGames();
        getParentFragmentManager().setFragmentResult("new_game", new Bundle());
        getParentFragmentManager().popBackStack();
    }

    private void updateNameSpinners() {
        List<Player> players = game.getPlayers();
        for (int i = 0; i < 4; i++) {
            Player p = players.get(i);
            for (int j = 0; j < spinAdapter.getCount(); j++) {
                if (spinAdapter.getItem(j).equals(p.getName())) {
                    nameSpinners.get(i).setSelection(j);
                    break;
                }
            }
        }
    }

    private class PlayerSelectedListener implements AdapterView.OnItemSelectedListener {
        private final int playerNum;

        PlayerSelectedListener(int playerNum) {
            this.playerNum = playerNum;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int index, long id) {
            List<Player> allPlayers = TGApp.getPlayers();
            if (index < allPlayers.size()) {
                game.setPlayer(playerNum, allPlayers.get(index));
            } else if (!addingPlayer) {
                addingPlayer = true;
                getNewPlayerName();
            }
        }

        private void getNewPlayerName() {
            EditText input = new EditText(requireContext());
            new AlertDialog.Builder(requireContext())
                .setTitle("New Player")
                .setMessage("Enter the new player's name")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, which) -> {
                    String name = input.getText().toString();
                    if (name.isEmpty()) {
                        requireActivity().runOnUiThread(this::getNewPlayerName);
                        return;
                    }
                    List<Player> allPlayers = TGApp.getPlayers();
                    Player newPlayer = null;
                    for (Player p : allPlayers) {
                        if (name.equals(p.getName())) {
                            new AlertDialog.Builder(requireContext()).setMessage("That player already exists!").show();
                            newPlayer = p;
                            break;
                        }
                    }
                    if (newPlayer == null) {
                        newPlayer = new Player(name);
                        allPlayers.add(newPlayer);
                        Collections.sort(allPlayers);
                        spinAdapter.insert(newPlayer.getName(), allPlayers.indexOf(newPlayer));
                        ((TGApp) requireActivity().getApplication()).savePlayers();
                    }
                    game.setPlayer(playerNum, newPlayer);
                    addingPlayer = false;
                    requireActivity().runOnUiThread(NewGameFragment.this::updateNameSpinners);
                })
                .setNegativeButton("Cancel", (dialog, which) -> addingPlayer = false)
                .show();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }
}
