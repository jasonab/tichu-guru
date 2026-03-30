package com.tichuguru;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.tichuguru.model.Game;
import com.tichuguru.model.Player;
import java.text.SimpleDateFormat;
import java.util.List;

public class AllGamesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.allgames, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        View view = requireView();
        ListView gamesList = view.findViewById(R.id.gamesList);
        gamesList.setAdapter(new GamesAdapter(requireContext(), R.id.gamesDate));
        gamesList.setOnItemClickListener((parent, child, position, id) -> {
            CurHandActivity.clearTichuButtonsNow = true;
            List<Game> games = TGApp.getGames();
            TGApp.setGame(games.get((games.size() - position) - 1));
            ((TGActivity) requireActivity()).navigateToTab(0);
        });
    }

    private class GamesAdapter extends ArrayAdapter<Game> {
        private final SimpleDateFormat df = new SimpleDateFormat("M/d");

        public GamesAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            int index = 0;
            for (Game g : TGApp.getGames()) {
                super.insert(g, index++);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from(requireContext()).inflate(R.layout.allgamesrow, null);
            }
            List<Game> games = TGApp.getGames();
            Game game = games.get((games.size() - position) - 1);

            ((TextView) v.findViewById(R.id.gamesDate)).setText(df.format(game.getDate()));
            List<Player> players = game.getPlayers();
            TextView team1 = v.findViewById(R.id.gamesTeam1);
            team1.setText(players.get(0).getName() + " and " + players.get(2).getName());
            TextView team2 = v.findViewById(R.id.gamesTeam2);
            team2.setText(players.get(1).getName() + " and " + players.get(3).getName());
            TextView score1 = v.findViewById(R.id.gamesScore1);
            score1.setText(String.valueOf(game.getScore1()));
            TextView score2 = v.findViewById(R.id.gamesScore2);
            score2.setText(String.valueOf(game.getScore2()));

            if (game.isGameOver()) {
                boolean team1wins = game.getScore1() > game.getScore2();
                int win = -256, lose = -7829368;
                team1.setTextColor(team1wins ? win : lose);
                score1.setTextColor(team1wins ? win : lose);
                team2.setTextColor(team1wins ? lose : win);
                score2.setTextColor(team1wins ? lose : win);
            } else {
                int gray = -7829368;
                team1.setTextColor(gray);
                score1.setTextColor(gray);
                team2.setTextColor(gray);
                score2.setTextColor(gray);
            }

            ((Button) v.findViewById(R.id.gamesDeleteOne))
                .setOnClickListener(new DeleteGameClickListener((games.size() - position) - 1));
            return v;
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
                    games.remove(gameNum);
                    if (TGApp.getGame() == game) {
                        CurHandActivity.clearTichuButtonsNow = true;
                        TGApp.setGame(games.isEmpty() ? null : games.get(games.size() - 1));
                    }
                    refreshList();
                    if (TGApp.getGames().isEmpty()) {
                        ((TGActivity) requireActivity()).createFirstGame();
                    }
                })
                .setNegativeButton("No", null)
                .show();
        }
    }
}
