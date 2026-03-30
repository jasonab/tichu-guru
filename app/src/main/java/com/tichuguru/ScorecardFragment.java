package com.tichuguru;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.tichuguru.model.Game;
import com.tichuguru.model.Hand;
import com.tichuguru.model.Player;
import java.util.ArrayList;
import java.util.List;

public class ScorecardFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.scorecard, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.scorecardDelete).setOnClickListener(v -> onDeleteHand());
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshDisplay();
    }

    private void refreshDisplay() {
        View view = requireView();
        List<Player> players = TGApp.getGame().getPlayers();
        ((TextView) view.findViewById(R.id.scorecardName1)).setText(players.get(0).getName());
        ((TextView) view.findViewById(R.id.scorecardName2)).setText(players.get(1).getName());
        ((TextView) view.findViewById(R.id.scorecardName3)).setText(players.get(2).getName());
        ((TextView) view.findViewById(R.id.scorecardName4)).setText(players.get(3).getName());
        ((ListView) view.findViewById(R.id.scorecardList))
            .setAdapter(new ScorecardAdapter(requireContext(), R.id.scorecardHandScore1, TGApp.getGame()));
    }

    private void onDeleteHand() {
        new AlertDialog.Builder(requireContext())
            .setMessage("Are you sure?")
            .setPositiveButton("Yes", (dialog, which) -> {
                Game game = TGApp.getGame();
                game.removeHand(game.getHands().size() - 1);
                refreshDisplay();
            })
            .setNegativeButton("No", null)
            .show();
    }

    private class ScorecardAdapter extends ArrayAdapter<Hand> {
        private Game game;

        public ScorecardAdapter(Context context, int textViewResourceId, Game game) {
            super(context, textViewResourceId);
            this.game = game;
            int index = 0;
            for (Hand h : game.getHands()) {
                super.insert(h, index++);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from(requireContext()).inflate(R.layout.scorecardrow, null);
            }
            Hand hand = this.game.getHands().get(position);
            int score = hand.getTotalScore1();
            ((TextView) v.findViewById(R.id.scorecardHandScore1)).setText((score >= 0 ? "+" : "") + score);
            int score2 = hand.getTotalScore2();
            ((TextView) v.findViewById(R.id.scorecardHandScore2)).setText((score2 >= 0 ? "+" : "") + score2);

            List<TextView> tichus = new ArrayList<>();
            tichus.add(v.findViewById(R.id.scorecardTichu1));
            tichus.add(v.findViewById(R.id.scorecardTichu2));
            tichus.add(v.findViewById(R.id.scorecardTichu3));
            tichus.add(v.findViewById(R.id.scorecardTichu4));
            for (int i = 0; i < 4; i++) {
                TextView tv = tichus.get(i);
                if (hand.isTichuFor(i)) tv.setText("T");
                else if (hand.isGrandTichuFor(i)) tv.setText("GT");
                else tv.setText("");
                tv.setTextColor(hand.outFirst() == i ? -1 : -65536);
            }

            int s1 = 0, s2 = 0;
            for (int i = 0; i <= position; i++) {
                Hand h = this.game.getHands().get(i);
                s1 += h.getTotalScore1();
                s2 += h.getTotalScore2();
            }
            ((TextView) v.findViewById(R.id.scorecardTotalScore1)).setText(String.valueOf(s1));
            ((TextView) v.findViewById(R.id.scorecardTotalScore2)).setText(String.valueOf(s2));
            return v;
        }
    }
}
