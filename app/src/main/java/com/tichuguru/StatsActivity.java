package com.tichuguru;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.tichuguru.model.Player;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import kankan.wheel.widget.WheelScroller;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

/* loaded from: classes.dex */
public class StatsActivity extends Activity {
    private StatsAdapter adapter;

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats);
        Button button = (Button) findViewById(R.id.statsClearAll);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.tichuguru.StatsActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                StatsActivity.this.onClearStats();
            }
        });
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        if (this.adapter == null || this.adapter.getCount() != TGApp.getPlayers().size() + 11) {
            this.adapter = new StatsAdapter(this, R.id.statsName);
            ListView statsList = (ListView) findViewById(R.id.statsList);
            statsList.setAdapter((ListAdapter) this.adapter);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onClearStats() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() { // from class: com.tichuguru.StatsActivity.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AbstractWheelTextAdapter.TEXT_VIEW_ITEM_RESOURCE /* -1 */:
                        for (Player p : TGApp.getPlayers()) {
                            p.clearStats();
                        }
                        StatsActivity.this.adapter.notifyDataSetChanged();
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
        public StatsAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            int index = 0 + 1;
            super.insert("Players", 0);
            List<Player> players = TGApp.getPlayers();
            Iterator<Player> it = players.iterator();
            while (true) {
                int index2 = index;
                if (it.hasNext()) {
                    Player p = it.next();
                    index = index2 + 1;
                    super.insert(p.getName(), index2);
                } else {
                    int index3 = index2 + 1;
                    super.insert("Rankings", index2);
                    int index4 = index3 + 1;
                    super.insert("Win %", index3);
                    int index5 = index4 + 1;
                    super.insert("Pts / Hand", index4);
                    int index6 = index5 + 1;
                    super.insert("Card Pts / Hand", index5);
                    int index7 = index6 + 1;
                    super.insert("Hands / Double Win", index6);
                    int index8 = index7 + 1;
                    super.insert("Tichu %", index7);
                    int index9 = index8 + 1;
                    super.insert("Grand Tichu %", index8);
                    int index10 = index9 + 1;
                    super.insert("Tichu Efficiency", index9);
                    int index11 = index10 + 1;
                    super.insert("Tichu Stop %", index10);
                    int i = index11 + 1;
                    super.insert("Partner Tichu %", index11);
                    return;
                }
            }
        }

        /* JADX WARN: Failed to find 'out' block for switch in B:14:0x007a. Please report as an issue. */
        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) StatsActivity.this.getSystemService("layout_inflater");
                v = vi.inflate(R.layout.statsrow, (ViewGroup) null);
            }
            List<Player> players = TGApp.getPlayers();
            TextView tv = (TextView) v.findViewById(R.id.statsName);
            Button expandButton = (Button) v.findViewById(R.id.statsExpandButton);
            String text = (String) super.getItem(position);
            tv.setText(text);
            if (position == 0 || position == players.size() + 1) {
                tv.setTypeface(null, Typeface.BOLD);
                expandButton.setVisibility(View.INVISIBLE);
            } else if (position <= players.size()) {
                tv.setTypeface(null, Typeface.NORMAL);
                expandButton.setVisibility(View.VISIBLE);
                expandButton.setOnClickListener(new PlayerExpandListener(players.get(position - 1)));
            } else {
                tv.setTypeface(null, Typeface.NORMAL);
                expandButton.setVisibility(View.VISIBLE);
                int num = (position - players.size()) - 2;
                switch (num) {
                    case 0:
                        expandButton.setOnClickListener(new RankExpandListener("Win %", true, "WinPct", "NumWins", "NumGames"));
                        break;
                    case WheelScroller.MIN_DELTA_FOR_SCROLLING /* 1 */:
                        expandButton.setOnClickListener(new RankExpandListener("Pts / Hand", true, "PtsPerHand", "NumHands", null));
                        break;
                    case 2:
                        expandButton.setOnClickListener(new RankExpandListener("Card Pts / Hand", true, "CardPtsPerHand", "NumHands", null));
                        break;
                    case 3:
                        expandButton.setOnClickListener(new RankExpandListener("Hands / DW", false, "HandsPerDW", "NumDoubleWins", null));
                        break;
                    case 4:
                        expandButton.setOnClickListener(new RankExpandListener("Tichu %", true, "TichuPct", "NumTichuMade", "NumTichuCalled"));
                        break;
                    case 5:
                        expandButton.setOnClickListener(new RankExpandListener("Grand Tichu %", true, "GTPct", "NumGTMade", "NumGTCalled"));
                        break;
                    case 6:
                        expandButton.setOnClickListener(new RankExpandListener("Tichu Efficiency", true, "TichuEfficiency", "TichuEfficiencyHands", null));
                        break;
                    case 7:
                        expandButton.setOnClickListener(new RankExpandListener("Tichu Stop %", true, "TichuStopPct", "NumTichusStopped", "NumTichusCalledByOpps"));
                        break;
                    case 8:
                        expandButton.setOnClickListener(new RankExpandListener("Partner Tichu %", true, "PartnerTichuPct", "NumTichusMadeByPartner", "NumTichusCalledByPartner"));
                        break;
                    default:
                        throw new RuntimeException("Unknown rank index: " + num);
                }
            }
            return v;
        }

        /* loaded from: classes.dex */
        class PlayerExpandListener implements View.OnClickListener {
            private Player player;

            public PlayerExpandListener(Player player) {
                this.player = player;
            }

            /* JADX WARN: Multi-variable type inference failed */
            /* JADX WARN: Type inference failed for: r2v0, types: [java.lang.String[], java.io.Serializable] */
            /* JADX WARN: Type inference failed for: r3v0, types: [java.lang.String[], java.io.Serializable] */
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                String[] r2 = {"Games", "# Played", "Win %", "Hands", "# Played", "Average Pts / Hand", "Average Card Pts / Hand", "# Double Wins", "Hands / Double Win", "Tichus", "# of Tichus", "% Tichus Made", "Non-Calls", "Tichu Efficiency", "# of Grand Tichus", "% Grand Tichus Made", "# of Tichus Stopped", "Tichu Stop %", "Partner Tichu %"};
                String[] r3 = new String[r2.length];
                r3[1] = String.valueOf(this.player.getNumGames());
                r3[2] = String.format("%.2f", Double.valueOf(this.player.getWinPct()));
                r3[4] = String.valueOf(this.player.getNumHands());
                r3[5] = String.format("%.2f", Double.valueOf(this.player.getPtsPerHand()));
                r3[6] = String.format("%.2f", Double.valueOf(this.player.getCardPtsPerHand()));
                r3[7] = String.valueOf(this.player.getNumDoubleWins());
                r3[8] = String.format("%.2f", Double.valueOf(this.player.getHandsPerDW()));
                r3[10] = String.valueOf(this.player.getNumTichuCalled());
                r3[11] = String.format("%.2f", Double.valueOf(this.player.getTichuPct()));
                r3[12] = String.valueOf(this.player.nonCalls());
                r3[13] = String.format("%.2f", Double.valueOf(this.player.getTichuEfficiency()));
                r3[14] = String.valueOf(this.player.getNumGTCalled());
                r3[15] = String.format("%.2f", Double.valueOf(this.player.getGTPct()));
                r3[16] = String.valueOf(this.player.getNumTichusStopped());
                r3[17] = String.format("%.2f", Double.valueOf(this.player.getTichuStopPct()));
                r3[18] = String.format("%.2f", Double.valueOf(this.player.getPartnerTichuPct()));
                Bundle bundle = new Bundle();
                bundle.putSerializable("title", "Stats for " + this.player.getName());
                bundle.putSerializable("labels", r2);
                bundle.putSerializable("values", r3);
                bundle.putSerializable("playerName", this.player.getName());
                Intent intent = new Intent(StatsActivity.this, (Class<?>) StatsListActivity.class);
                intent.putExtras(bundle);
                StatsActivity.this.startActivity(intent);
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: classes.dex */
        public class RankExpandListener implements View.OnClickListener {
            private Getter extraGetter1;
            private Getter extraGetter2;
            private Getter rankValueGetter;
            private boolean sortDescending;
            private String title;

            public RankExpandListener(String title, boolean sortDescending, String rankValue, String extra1, String extra2) {
                this.title = title;
                this.sortDescending = sortDescending;
                this.rankValueGetter = new Getter(rankValue);
                this.extraGetter1 = new Getter(extra1);
                if (extra2 != null) {
                    this.extraGetter2 = new Getter(extra2);
                }
            }

            /* JADX WARN: Multi-variable type inference failed */
            /* JADX WARN: Type inference failed for: r15v0, types: [java.lang.String[], java.io.Serializable] */
            /* JADX WARN: Type inference failed for: r9v0, types: [java.lang.String[], java.io.Serializable] */
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                String formatStr;
                double log;
                List<Player> players = TGApp.getPlayers();
                List<Player> ps = new ArrayList<>(players);
                String[] r15 = new String[ps.size()];
                Collections.sort(ps, new Comparator<Player>() { // from class: com.tichuguru.StatsActivity.StatsAdapter.RankExpandListener.1
                    @Override // java.util.Comparator
                    public int compare(Player p1, Player p2) {
                        double diff = ((Double) RankExpandListener.this.rankValueGetter.getValue(p1)).doubleValue() - ((Double) RankExpandListener.this.rankValueGetter.getValue(p2)).doubleValue();
                        if (RankExpandListener.this.sortDescending) {
                            diff = -diff;
                        }
                        return (int) Math.signum(diff);
                    }
                });
                String[] r9 = new String[ps.size()];
                for (int i = 0; i < ps.size(); i++) {
                    r9[i] = ps.get(i).getName();
                }
                int digits = 1;
                for (Player p : ps) {
                    if (this.extraGetter2 != null) {
                        log = Math.log10(((Integer) this.extraGetter2.getValue(p)).intValue()) + 1.0d;
                    } else {
                        log = Math.log10(((Integer) this.extraGetter1.getValue(p)).intValue()) + 1.0d;
                    }
                    if (Double.isNaN(log)) {
                        log = 1.0d;
                    }
                    digits = (int) Math.max(digits, log);
                }
                if (this.extraGetter2 != null) {
                    formatStr = String.format("%%.2f%%%% (%%0%dd/%%0%dd)", Integer.valueOf(digits), Integer.valueOf(digits));
                } else {
                    formatStr = String.format("%%.2f (%%0%dd)", Integer.valueOf(digits));
                }
                int index = 0;
                for (Player p2 : ps) {
                    if (this.extraGetter2 != null) {
                        r15[index] = String.format(formatStr, this.rankValueGetter.getValue(p2), this.extraGetter1.getValue(p2), this.extraGetter2.getValue(p2));
                        index++;
                    } else {
                        r15[index] = String.format(formatStr, this.rankValueGetter.getValue(p2), this.extraGetter1.getValue(p2));
                        index++;
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable("title", this.title);
                bundle.putSerializable("labels", r9);
                bundle.putSerializable("values", r15);
                Intent intent = new Intent(StatsActivity.this, (Class<?>) StatsListActivity.class);
                intent.putExtras(bundle);
                StatsActivity.this.startActivity(intent);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class Getter {
        private Method getMethod;

        public Getter(String valName) {
            try {
                this.getMethod = Player.class.getMethod("get" + valName, null);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        public Object getValue(Player p) {
            try {
                return this.getMethod.invoke(p, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
