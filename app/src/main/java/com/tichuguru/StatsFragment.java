package com.tichuguru;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.tichuguru.model.Player;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatsFragment extends Fragment {
    private StatsAdapter adapter;
    private TGViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.stats, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.statsClearAll).setOnClickListener(v -> onClearStats());
        viewModel = new ViewModelProvider(requireActivity()).get(TGViewModel.class);
        viewModel.getAllPlayers().observe(getViewLifecycleOwner(), players -> {
            if (adapter == null || adapter.getCount() != players.size() + 11) {
                adapter = new StatsAdapter(requireContext(), R.id.statsName);
                ((ListView) requireView().findViewById(R.id.statsList)).setAdapter(adapter);
            }
        });
    }

    private void onClearStats() {
        new AlertDialog.Builder(requireContext())
            .setMessage("Are you sure?")
            .setPositiveButton("Yes", (dialog, which) -> {
                for (Player p : TGApp.getPlayers()) p.clearStats();
                adapter.notifyDataSetChanged();
            })
            .setNegativeButton("No", null)
            .show();
    }

    private class StatsAdapter extends ArrayAdapter<String> {
        public StatsAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            super.insert("Players", 0);
            int index = 1;
            for (Player p : TGApp.getPlayers()) super.insert(p.getName(), index++);
            super.insert("Rankings", index++);
            super.insert("Win %", index++);
            super.insert("Pts / Hand", index++);
            super.insert("Card Pts / Hand", index++);
            super.insert("Hands / Double Win", index++);
            super.insert("Tichu %", index++);
            super.insert("Grand Tichu %", index++);
            super.insert("Tichu Efficiency", index++);
            super.insert("Tichu Stop %", index++);
            super.insert("Partner Tichu %", index++);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from(requireContext()).inflate(R.layout.statsrow, null);
            }
            List<Player> players = TGApp.getPlayers();
            TextView tv = v.findViewById(R.id.statsName);
            Button expandButton = v.findViewById(R.id.statsExpandButton);
            tv.setText((String) getItem(position));

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
                    case 0: expandButton.setOnClickListener(new RankExpandListener("Win %", true, "WinPct", "NumWins", "NumGames")); break;
                    case 1: expandButton.setOnClickListener(new RankExpandListener("Pts / Hand", true, "PtsPerHand", "NumHands", null)); break;
                    case 2: expandButton.setOnClickListener(new RankExpandListener("Card Pts / Hand", true, "CardPtsPerHand", "NumHands", null)); break;
                    case 3: expandButton.setOnClickListener(new RankExpandListener("Hands / DW", false, "HandsPerDW", "NumDoubleWins", null)); break;
                    case 4: expandButton.setOnClickListener(new RankExpandListener("Tichu %", true, "TichuPct", "NumTichuMade", "NumTichuCalled")); break;
                    case 5: expandButton.setOnClickListener(new RankExpandListener("Grand Tichu %", true, "GTPct", "NumGTMade", "NumGTCalled")); break;
                    case 6: expandButton.setOnClickListener(new RankExpandListener("Tichu Efficiency", true, "TichuEfficiency", "TichuEfficiencyHands", null)); break;
                    case 7: expandButton.setOnClickListener(new RankExpandListener("Tichu Stop %", true, "TichuStopPct", "NumTichusStopped", "NumTichusCalledByOpps")); break;
                    case 8: expandButton.setOnClickListener(new RankExpandListener("Partner Tichu %", true, "PartnerTichuPct", "NumTichusMadeByPartner", "NumTichusCalledByPartner")); break;
                    default: throw new RuntimeException("Unknown rank index: " + num);
                }
            }
            return v;
        }

        class PlayerExpandListener implements View.OnClickListener {
            private final Player player;
            PlayerExpandListener(Player player) { this.player = player; }

            @Override
            public void onClick(View v) {
                String[] labels = {"Games", "# Played", "Win %", "Hands", "# Played",
                    "Average Pts / Hand", "Average Card Pts / Hand", "# Double Wins",
                    "Hands / Double Win", "Tichus", "# of Tichus", "% Tichus Made",
                    "Non-Calls", "Tichu Efficiency", "# of Grand Tichus",
                    "% Grand Tichus Made", "# of Tichus Stopped", "Tichu Stop %",
                    "Partner Tichu %"};
                String[] values = new String[labels.length];
                values[1]  = String.valueOf(player.getNumGames());
                values[2]  = String.format("%.2f", player.getWinPct());
                values[4]  = String.valueOf(player.getNumHands());
                values[5]  = String.format("%.2f", player.getPtsPerHand());
                values[6]  = String.format("%.2f", player.getCardPtsPerHand());
                values[7]  = String.valueOf(player.getNumDoubleWins());
                values[8]  = String.format("%.2f", player.getHandsPerDW());
                values[10] = String.valueOf(player.getNumTichuCalled());
                values[11] = String.format("%.2f", player.getTichuPct());
                values[12] = String.valueOf(player.nonCalls());
                values[13] = String.format("%.2f", player.getTichuEfficiency());
                values[14] = String.valueOf(player.getNumGTCalled());
                values[15] = String.format("%.2f", player.getGTPct());
                values[16] = String.valueOf(player.getNumTichusStopped());
                values[17] = String.format("%.2f", player.getTichuStopPct());
                values[18] = String.format("%.2f", player.getPartnerTichuPct());

                Bundle bundle = new Bundle();
                bundle.putSerializable("title", "Stats for " + player.getName());
                bundle.putSerializable("labels", labels);
                bundle.putSerializable("values", values);
                bundle.putSerializable("playerName", player.getName());
                Intent intent = new Intent(requireActivity(), StatsListActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }

        class RankExpandListener implements View.OnClickListener {
            private final Getter rankValueGetter, extraGetter1;
            private final Getter extraGetter2;
            private final boolean sortDescending;
            private final String title;

            RankExpandListener(String title, boolean sortDescending, String rankValue, String extra1, String extra2) {
                this.title = title;
                this.sortDescending = sortDescending;
                this.rankValueGetter = new Getter(rankValue);
                this.extraGetter1 = new Getter(extra1);
                this.extraGetter2 = extra2 != null ? new Getter(extra2) : null;
            }

            @Override
            public void onClick(View v) {
                List<Player> players = new ArrayList<>(TGApp.getPlayers());
                Collections.sort(players, (p1, p2) -> {
                    double diff = (Double) rankValueGetter.getValue(p1) - (Double) rankValueGetter.getValue(p2);
                    if (sortDescending) diff = -diff;
                    return (int) Math.signum(diff);
                });

                int digits = 1;
                for (Player p : players) {
                    Getter g = extraGetter2 != null ? extraGetter2 : extraGetter1;
                    double log = Math.log10((Integer) g.getValue(p)) + 1.0;
                    if (!Double.isNaN(log)) digits = (int) Math.max(digits, log);
                }
                String fmt = extraGetter2 != null
                    ? String.format("%%.2f%%%% (%%0%dd/%%0%dd)", digits, digits)
                    : String.format("%%.2f (%%0%dd)", digits);

                String[] names = new String[players.size()];
                String[] values = new String[players.size()];
                for (int i = 0; i < players.size(); i++) {
                    Player p = players.get(i);
                    names[i] = p.getName();
                    values[i] = extraGetter2 != null
                        ? String.format(fmt, rankValueGetter.getValue(p), extraGetter1.getValue(p), extraGetter2.getValue(p))
                        : String.format(fmt, rankValueGetter.getValue(p), extraGetter1.getValue(p));
                }

                Bundle bundle = new Bundle();
                bundle.putSerializable("title", title);
                bundle.putSerializable("labels", names);
                bundle.putSerializable("values", values);
                Intent intent = new Intent(requireActivity(), StatsListActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    }

    private static class Getter {
        private final Method getMethod;

        Getter(String valName) {
            try {
                this.getMethod = Player.class.getMethod("get" + valName);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        Object getValue(Player p) {
            try {
                return getMethod.invoke(p);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
