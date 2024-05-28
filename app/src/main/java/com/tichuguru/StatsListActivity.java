package com.tichuguru;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.tichuguru.model.Game;
import com.tichuguru.model.Player;
import java.util.List;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

/* loaded from: classes.dex */
public class StatsListActivity extends Activity {
    private Player player;

    @Override // android.app.Activity
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
        StatsAdapter adapter = new StatsAdapter(this, R.id.statsLabel, labels, values);
        ListView statsList = (ListView) findViewById(R.id.statsList);
        statsList.setAdapter((ListAdapter) adapter);
        if (this.player != null) {
            Button clearButton = (Button) findViewById(R.id.statsClearPlayerStats);
            clearButton.setOnClickListener(new View.OnClickListener() { // from class: com.tichuguru.StatsListActivity.1
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    StatsListActivity.this.onClearStats();
                }
            });
            Button delButton = (Button) findViewById(R.id.statsDelPlayer);
            delButton.setOnClickListener(new View.OnClickListener() { // from class: com.tichuguru.StatsListActivity.2
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    StatsListActivity.this.onDeletePlayer();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onClearStats() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() { // from class: com.tichuguru.StatsListActivity.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AbstractWheelTextAdapter.TEXT_VIEW_ITEM_RESOURCE /* -1 */:
                        StatsListActivity.this.player.clearStats();
                        StatsListActivity.this.finish();
                        return;
                    default:
                        return;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDeletePlayer() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() { // from class: com.tichuguru.StatsListActivity.4
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AbstractWheelTextAdapter.TEXT_VIEW_ITEM_RESOURCE /* -1 */:
                        boolean updateSharedGame = false;
                        List<Game> games = TGApp.getGames();
                        for (int i = games.size() - 1; i >= 0; i--) {
                            Game game = games.get(i);
                            if (game.containsPlayer(StatsListActivity.this.player)) {
                                games.remove(i);
                                if (game == TGApp.getGame()) {
                                    updateSharedGame = true;
                                }
                            }
                        }
                        if (updateSharedGame) {
                            if (games.size() > 0) {
                                TGApp.setGame(games.get(games.size() - 1));
                            } else {
                                TGApp.setGame(null);
                            }
                        }
                        List<Player> allPlayers = TGApp.getPlayers();
                        allPlayers.remove(StatsListActivity.this.player);
                        StatsListActivity.this.finish();
                        return;
                    default:
                        return;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
    }

    /* loaded from: classes.dex */
    private class StatsAdapter extends ArrayAdapter<String> {
        private String[] labels;
        private String[] values;

        public StatsAdapter(Context context, int textViewResourceId, String[] labels, String[] values) {
            super(context, textViewResourceId);
            this.labels = labels;
            this.values = values;
            for (String label : labels) {
                super.add(label);
            }
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) StatsListActivity.this.getSystemService("layout_inflater");
                v = vi.inflate(R.layout.statslistrow, (ViewGroup) null);
            }
            TextView label = (TextView) v.findViewById(R.id.statsLabel);
            TextView value = (TextView) v.findViewById(R.id.statsValue);
            label.setText(this.labels[position]);
            if (this.values[position] != null) {
                label.setTypeface(null, 0);
                label.setTextSize(18.0f);
                value.setText(this.values[position]);
            } else {
                label.setTypeface(null, 1);
                label.setTextSize(24.0f);
                value.setText("");
            }
            return v;
        }
    }
}
