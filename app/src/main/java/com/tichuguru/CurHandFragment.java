package com.tichuguru;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.tichuguru.model.Game;
import com.tichuguru.model.Hand;
import com.tichuguru.model.Player;
import java.util.List;

public class CurHandFragment extends Fragment implements MenuProvider {
    private TGViewModel viewModel;
    RadioGroup grp1, grp2, grp3, grp4;
    TextView name1, name2, name3, name4;
    TextView score1, score2;
    Button scoreHandButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.curhand, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        score1 = view.findViewById(R.id.curHandScore1);
        score2 = view.findViewById(R.id.curHandScore2);
        name1 = view.findViewById(R.id.curHandP1);
        name2 = view.findViewById(R.id.curHandP2);
        name3 = view.findViewById(R.id.curHandP3);
        name4 = view.findViewById(R.id.curHandP4);
        grp1 = view.findViewById(R.id.curHandP1RG);
        grp2 = view.findViewById(R.id.curHandP2RG);
        grp3 = view.findViewById(R.id.curHandP3RG);
        grp4 = view.findViewById(R.id.curHandP4RG);
        scoreHandButton = view.findViewById(R.id.curHandScoreHand);
        scoreHandButton.setOnClickListener(v -> onScoreHand());
        view.findViewById(R.id.curHandNewGame).setOnClickListener(v -> onNewGame());

        if (savedInstanceState != null) {
            grp1.check(savedInstanceState.getInt("tichu1", R.id.curHandP1None));
            grp2.check(savedInstanceState.getInt("tichu2", R.id.curHandP2None));
            grp3.check(savedInstanceState.getInt("tichu3", R.id.curHandP3None));
            grp4.check(savedInstanceState.getInt("tichu4", R.id.curHandP4None));
        }

        viewModel = new ViewModelProvider(requireActivity()).get(TGViewModel.class);
        viewModel.getCurrentGame().observe(getViewLifecycleOwner(), game -> updateDisplay());
        viewModel.getClearTichuButtons().observe(getViewLifecycleOwner(), clear -> {
            if (Boolean.TRUE.equals(clear)) clearTichuButtons();
        });

        getParentFragmentManager().setFragmentResultListener("score_hand", getViewLifecycleOwner(), (key, result) -> {
            clearTichuButtons();
            viewModel.notifyGameChanged();
        });
        getParentFragmentManager().setFragmentResultListener("new_game", getViewLifecycleOwner(), (key, result) -> {
            clearTichuButtons();
            viewModel.notifyGameChanged();
        });

        requireActivity().addMenuProvider(this, getViewLifecycleOwner());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (grp1 != null) {
            outState.putInt("tichu1", grp1.getCheckedRadioButtonId());
            outState.putInt("tichu2", grp2.getCheckedRadioButtonId());
            outState.putInt("tichu3", grp3.getCheckedRadioButtonId());
            outState.putInt("tichu4", grp4.getCheckedRadioButtonId());
        }
        super.onSaveInstanceState(outState);
    }

    private void onNewGame() {
        ((TGActivity) requireActivity()).pushFragment(
            NewGameFragment.newInstance(new Game(TGApp.getGame()))
        );
    }

    private void onEndGame() {
        Game game = TGApp.getGame();
        if (game.getScore1() == game.getScore2()) {
            new AlertDialog.Builder(requireContext()).setMessage("You can't end the game when the score is tied.").show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    TGApp.getGame().endGame();
                    TGApp app = (TGApp) requireActivity().getApplication();
                    app.saveGames();
                    app.savePlayers();
                    updateDisplay();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void onScoreHand() {
        Hand hand = new Hand(TGApp.getGame());
        if (grp1.getCheckedRadioButtonId() == R.id.curHandP1GT) hand.setGrandTichuFor(0);
        if (grp1.getCheckedRadioButtonId() == R.id.curHandP1T) hand.setTichuFor(0);
        if (grp2.getCheckedRadioButtonId() == R.id.curHandP2GT) hand.setGrandTichuFor(1);
        if (grp2.getCheckedRadioButtonId() == R.id.curHandP2T) hand.setTichuFor(1);
        if (grp3.getCheckedRadioButtonId() == R.id.curHandP3GT) hand.setGrandTichuFor(2);
        if (grp3.getCheckedRadioButtonId() == R.id.curHandP3T) hand.setTichuFor(2);
        if (grp4.getCheckedRadioButtonId() == R.id.curHandP4GT) hand.setGrandTichuFor(3);
        if (grp4.getCheckedRadioButtonId() == R.id.curHandP4T) hand.setTichuFor(3);
        ((TGActivity) requireActivity()).pushFragment(ScoreHandFragment.newInstance(hand));
    }

    private void clearTichuButtons() {
        grp1.check(R.id.curHandP1None);
        grp2.check(R.id.curHandP2None);
        grp3.check(R.id.curHandP3None);
        grp4.check(R.id.curHandP4None);
    }

    private void updateDisplay() {
        Game game = TGApp.getGame();
        if (game == null) return;
        score1.setText(String.valueOf(game.getScore1()));
        score2.setText(String.valueOf(game.getScore2()));
        List<Player> players = game.getPlayers();
        name1.setText(players.get(0).getName());
        name2.setText(players.get(1).getName());
        name3.setText(players.get(2).getName());
        name4.setText(players.get(3).getName());
        if (game.isGameOver()) {
            boolean team1wins = game.getScore1() > game.getScore2();
            int winColor = Color.YELLOW;
            int loseColor = Color.GRAY;
            score1.setTextColor(team1wins ? winColor : loseColor);
            name1.setTextColor(team1wins ? winColor : loseColor);
            name3.setTextColor(team1wins ? winColor : loseColor);
            score2.setTextColor(team1wins ? loseColor : winColor);
            name2.setTextColor(team1wins ? loseColor : winColor);
            name4.setTextColor(team1wins ? loseColor : winColor);
            scoreHandButton.setEnabled(false);
        } else {
            int color = Color.GRAY;
            score1.setTextColor(color);
            name1.setTextColor(color);
            name3.setTextColor(color);
            score2.setTextColor(color);
            name2.setTextColor(color);
            name4.setTextColor(color);
            scoreHandButton.setEnabled(true);
        }
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_curhand, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_end_game) {
            onEndGame();
            return true;
        } else if (id == R.id.menu_quit) {
            requireActivity().finish();
            return true;
        }
        return false;
    }
}
