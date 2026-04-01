package com.tichuguru;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.tichuguru.model.Game;
import com.tichuguru.model.Player;
import java.util.List;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

public class StatsListActivity extends AppCompatActivity {
    private Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = getIntent().getExtras();
        setTitle((String) data.getSerializable("title"));
        String[] labels = (String[]) data.getSerializable("labels");
        String[] values = (String[]) data.getSerializable("values");
        String playerName = (String) data.getSerializable("playerName");
        if (playerName != null) {
            this.player = TGApp.getPlayer(playerName);
        }
        setContentView(this.player == null ? R.layout.rankinglist : R.layout.statslist);

        RecyclerView statsList = findViewById(R.id.statsList);
        statsList.setLayoutManager(new LinearLayoutManager(this));
        statsList.setAdapter(new StatsAdapter(labels, values));

        if (this.player != null) {
            Button clearButton = findViewById(R.id.statsClearPlayerStats);
            clearButton.setOnClickListener(v -> onClearStats());
            Button delButton = findViewById(R.id.statsDelPlayer);
            delButton.setOnClickListener(v -> onDeletePlayer());
        }
    }

    private void onClearStats() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            if (which == AbstractWheelTextAdapter.TEXT_VIEW_ITEM_RESOURCE) {
                this.player.clearStats();
                finish();
            }
        };
        new AlertDialog.Builder(this)
            .setMessage("Are you sure?")
            .setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener)
            .show();
    }

    private void onDeletePlayer() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            if (which == AbstractWheelTextAdapter.TEXT_VIEW_ITEM_RESOURCE) {
                boolean updateSharedGame = false;
                List<Game> games = TGApp.getGames();
                for (int i = games.size() - 1; i >= 0; i--) {
                    Game game = games.get(i);
                    if (game.containsPlayer(this.player)) {
                        games.remove(i);
                        if (game == TGApp.getGame()) {
                            updateSharedGame = true;
                        }
                    }
                }
                if (updateSharedGame) {
                    TGApp.setGame(games.isEmpty() ? null : games.get(games.size() - 1));
                }
                TGApp.getPlayers().remove(this.player);
                finish();
            }
        };
        new AlertDialog.Builder(this)
            .setMessage("Are you sure?")
            .setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener)
            .show();
    }

    private static class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.ViewHolder> {
        private final String[] labels;
        private final String[] values;

        StatsAdapter(String[] labels, String[] values) {
            this.labels = labels;
            this.values = values;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView label, value;

            ViewHolder(View v) {
                super(v);
                label = v.findViewById(R.id.statsLabel);
                value = v.findViewById(R.id.statsValue);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.statslistrow, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.label.setText(labels[position]);
            if (values[position] != null) {
                holder.label.setTypeface(null, 0);
                holder.label.setTextSize(18.0f);
                holder.value.setText(values[position]);
            } else {
                holder.label.setTypeface(null, 1);
                holder.label.setTextSize(24.0f);
                holder.value.setText("");
            }
        }

        @Override
        public int getItemCount() {
            return labels.length;
        }
    }
}
