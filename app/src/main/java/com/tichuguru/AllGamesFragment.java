package com.tichuguru;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.tichuguru.model.Game;
import com.tichuguru.model.Player;
import java.text.SimpleDateFormat;
import java.util.List;

public class AllGamesFragment extends Fragment {
    private TGViewModel viewModel;
    private RecyclerView gamesList;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.allgames, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gamesList = view.findViewById(R.id.gamesList);
        gamesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        viewModel = new ViewModelProvider(requireActivity()).get(TGViewModel.class);
        viewModel.getAllGames().observe(getViewLifecycleOwner(), games -> refreshList());
    }

    private void refreshList() {
        gamesList.setAdapter(new GamesAdapter());
    }

    private class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.ViewHolder> {
        private final SimpleDateFormat df = new SimpleDateFormat("M/d");

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView date, team1, team2, score1, score2;
            Button deleteBtn;

            ViewHolder(View v) {
                super(v);
                date = v.findViewById(R.id.gamesDate);
                team1 = v.findViewById(R.id.gamesTeam1);
                team2 = v.findViewById(R.id.gamesTeam2);
                score1 = v.findViewById(R.id.gamesScore1);
                score2 = v.findViewById(R.id.gamesScore2);
                deleteBtn = v.findViewById(R.id.gamesDeleteOne);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.allgamesrow, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            List<Game> games = TGApp.getGames();
            int idx = games.size() - position - 1;
            Game game = games.get(idx);

            holder.date.setText(df.format(game.getDate()));
            List<Player> players = game.getPlayers();
            holder.team1.setText(players.get(0).getName() + " and " + players.get(2).getName());
            holder.team2.setText(players.get(1).getName() + " and " + players.get(3).getName());
            holder.score1.setText(String.valueOf(game.getScore1()));
            holder.score2.setText(String.valueOf(game.getScore2()));

            if (game.isGameOver()) {
                boolean team1wins = game.getScore1() > game.getScore2();
                holder.team1.setTextColor(team1wins ? Color.YELLOW : Color.GRAY);
                holder.score1.setTextColor(team1wins ? Color.YELLOW : Color.GRAY);
                holder.team2.setTextColor(team1wins ? Color.GRAY : Color.YELLOW);
                holder.score2.setTextColor(team1wins ? Color.GRAY : Color.YELLOW);
            } else {
                holder.team1.setTextColor(Color.GRAY);
                holder.score1.setTextColor(Color.GRAY);
                holder.team2.setTextColor(Color.GRAY);
                holder.score2.setTextColor(Color.GRAY);
            }

            holder.deleteBtn.setOnClickListener(new DeleteGameClickListener(idx));
            holder.itemView.setOnClickListener(v -> {
                viewModel.requestClearTichuButtons();
                viewModel.setGame(TGApp.getGames().get(idx));
                ((TGActivity) requireActivity()).navigateToTab(0);
            });
        }

        @Override
        public int getItemCount() {
            return TGApp.getGames().size();
        }
    }

    private class DeleteGameClickListener implements View.OnClickListener {
        private final int gameNum;

        DeleteGameClickListener(int gameNum) {
            this.gameNum = gameNum;
        }

        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(requireContext())
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    List<Game> games = TGApp.getGames();
                    Game game = games.get(gameNum);
                    ((TGApp) requireActivity().getApplication()).deleteGame(game);
                    games.remove(gameNum);
                    if (TGApp.getGame() == game) {
                        viewModel.requestClearTichuButtons();
                        viewModel.setGame(games.isEmpty() ? null : games.get(games.size() - 1));
                    }
                    viewModel.notifyGamesChanged();
                    if (TGApp.getGames().isEmpty()) {
                        ((TGActivity) requireActivity()).createFirstGame();
                    }
                })
                .setNegativeButton("No", null)
                .show();
        }
    }
}
