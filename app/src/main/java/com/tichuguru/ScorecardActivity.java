package com.tichuguru;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.tichuguru.model.Hand;
import com.tichuguru.model.Player;
import java.util.ArrayList;
import java.util.List;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

/* loaded from: classes.dex */
public class ScorecardActivity extends AppCompatActivity {
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scorecard);

        Button button = findViewById(R.id.scorecardDelete);
        button.setOnClickListener(v -> ScorecardActivity.this.onDeleteHand());
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        List<Player> players = TGApp.getGame().getPlayers();
        TextView name = findViewById(R.id.scorecardName1);
        name.setText(players.get(0).getName());
        TextView name2 = findViewById(R.id.scorecardName2);
        name2.setText(players.get(1).getName());
        TextView name3 = findViewById(R.id.scorecardName3);
        name3.setText(players.get(2).getName());
        TextView name4 = findViewById(R.id.scorecardName4);
        name4.setText(players.get(3).getName());
        ScorecardAdapter adapter = new ScorecardAdapter(this, R.id.scorecardHandScore1, TGApp.getGame());
        ListView handList = findViewById(R.id.scorecardList);
        handList.setAdapter(adapter);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDeleteHand() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case AbstractWheelTextAdapter.TEXT_VIEW_ITEM_RESOURCE /* -1 */:
                    Game game = TGApp.getGame();
                    game.removeHand(game.getHands().size() - 1);
                    ScorecardAdapter adapter = new ScorecardAdapter(ScorecardActivity.this, R.id.scorecardHandScore1, TGApp.getGame());
                    ListView handList = ScorecardActivity.this.findViewById(R.id.scorecardList);
                    handList.setAdapter(adapter);
                    return;
                default:
                    return;
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
    }

    /* loaded from: classes.dex */
    private class ScorecardAdapter extends ArrayAdapter<Hand> {
        private Game game;

        public ScorecardAdapter(Context context, int textViewResourceId, Game game) {
            super(context, textViewResourceId);
            this.game = game;
            int index = 0;
            for (Hand h : game.getHands()) {
                super.insert(h, index);
                index++;
            }
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) ScorecardActivity.this.getSystemService("layout_inflater");
                v = vi.inflate(R.layout.scorecardrow, null);
            }
            Hand hand = this.game.getHands().get(position);
            TextView scoreTV = (TextView) v.findViewById(R.id.scorecardHandScore1);
            int score = hand.getTotalScore1();
            scoreTV.setText(String.valueOf(score >= 0 ? "+" : "") + String.valueOf(score));
            TextView scoreTV2 = (TextView) v.findViewById(R.id.scorecardHandScore2);
            int score2 = hand.getTotalScore2();
            scoreTV2.setText(String.valueOf(score2 >= 0 ? "+" : "") + String.valueOf(score2));
            List<TextView> tichus = new ArrayList<>();
            tichus.add(v.findViewById(R.id.scorecardTichu1));
            tichus.add(v.findViewById(R.id.scorecardTichu2));
            tichus.add(v.findViewById(R.id.scorecardTichu3));
            tichus.add(v.findViewById(R.id.scorecardTichu4));
            for (int i = 0; i < 4; i++) {
                TextView tv = tichus.get(i);
                if (hand.isTichuFor(i)) {
                    tv.setText("T");
                } else if (hand.isGrandTichuFor(i)) {
                    tv.setText("GT");
                } else {
                    tv.setText("");
                }
                if (hand.outFirst() == i) {
                    tv.setTextColor(0xFF00AA00);
                } else {
                    tv.setTextColor(-65536);
                }
            }
            int score1 = 0;
            int score22 = 0;
            for (int i2 = 0; i2 <= position; i2++) {
                Hand h = this.game.getHands().get(i2);
                score1 += h.getTotalScore1();
                score22 += h.getTotalScore2();
            }
            TextView scoreTV3 = v.findViewById(R.id.scorecardTotalScore1);
            scoreTV3.setText(String.valueOf(score1));
            TextView scoreTV4 = v.findViewById(R.id.scorecardTotalScore2);
            scoreTV4.setText(String.valueOf(score22));
            return v;
        }
    }
}
