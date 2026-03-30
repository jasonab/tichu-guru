package com.tichuguru;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.tichuguru.model.Game;
import com.tichuguru.model.Player;
import java.text.SimpleDateFormat;
import java.util.List;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

/* loaded from: classes.dex */
public class AllGamesActivity extends AppCompatActivity {
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.allgames);
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        GamesAdapter adapter = new GamesAdapter(this, R.id.gamesDate);
        ListView gamesList = findViewById(R.id.gamesList);
        gamesList.setAdapter(adapter);

        gamesList.setOnItemClickListener((parent, child, position, id) -> {
            CurHandActivity.clearTichuButtonsNow = true;
            List<Game> games = TGApp.getGames();
            TGApp.setGame(games.get((games.size() - position) - 1));
            // superseded by AllGamesFragment
        });
    }

    /* loaded from: classes.dex */
    private class GamesAdapter extends ArrayAdapter<Game> {
        SimpleDateFormat df;

        public GamesAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.df = new SimpleDateFormat("M/d");
            int index = 0;
            for (Game g : TGApp.getGames()) {
                super.insert(g, index);
                index++;
            }
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) AllGamesActivity.this.getSystemService("layout_inflater");
                v = vi.inflate(R.layout.allgamesrow, null);
            }
            List<Game> games = TGApp.getGames();
            Game game = games.get((games.size() - position) - 1);
            TextView date = v.findViewById(R.id.gamesDate);
            date.setText(this.df.format(game.getDate()));
            List<Player> players = game.getPlayers();
            TextView team1 = v.findViewById(R.id.gamesTeam1);
            team1.setText(String.valueOf(players.get(0).getName()) + " and " + players.get(2).getName());
            TextView team2 = v.findViewById(R.id.gamesTeam2);
            team2.setText(String.valueOf(players.get(1).getName()) + " and " + players.get(3).getName());
            TextView score1 = v.findViewById(R.id.gamesScore1);
            score1.setText(String.valueOf(game.getScore1()));
            TextView score2 = v.findViewById(R.id.gamesScore2);
            score2.setText(String.valueOf(game.getScore2()));
            if (game.isGameOver()) {
                if (game.getScore1() > game.getScore2()) {
                    team1.setTextColor(-256);
                    score1.setTextColor(-256);
                    team2.setTextColor(-7829368);
                    score2.setTextColor(-7829368);
                } else {
                    team1.setTextColor(-7829368);
                    score1.setTextColor(-7829368);
                    team2.setTextColor(-256);
                    score2.setTextColor(-256);
                }
            } else {
                team1.setTextColor(-7829368);
                score1.setTextColor(-7829368);
                team2.setTextColor(-7829368);
                score2.setTextColor(-7829368);
            }
            Button button = v.findViewById(R.id.gamesDeleteOne);
            button.setOnClickListener(new DeleteGameClickListener((games.size() - position) - 1));
            return v;
        }
    }

    /* loaded from: classes.dex */
    class DeleteGameClickListener implements View.OnClickListener {
        private int gameNum;

        public DeleteGameClickListener(int gameNum) {
            this.gameNum = gameNum;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case AbstractWheelTextAdapter.TEXT_VIEW_ITEM_RESOURCE /* -1 */:
                        List<Game> games = TGApp.getGames();
                        Game game = games.get(DeleteGameClickListener.this.gameNum);
                        games.remove(DeleteGameClickListener.this.gameNum);
                        if (TGApp.getGame() == game) {
                            CurHandActivity.clearTichuButtonsNow = true;
                            if (!games.isEmpty()) {
                                TGApp.setGame(games.get(games.size() - 1));
                            } else {
                                TGApp.setGame(null);
                            }
                        }
                        GamesAdapter adapter = new GamesAdapter(AllGamesActivity.this, R.id.gamesDate);
                        ListView gamesList = (ListView) AllGamesActivity.this.findViewById(R.id.gamesList);
                        gamesList.setAdapter((ListAdapter) adapter);
                        if (TGApp.getGames().isEmpty()) {
                            // superseded by AllGamesFragment
                            return;
                        }
                        return;
                    default:
                        return;
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(AllGamesActivity.this);
            builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
        }
    }
}
