package com.tichuguru;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.tichuguru.model.Game;
import com.tichuguru.model.Hand;
import com.tichuguru.model.Player;
import java.util.List;

public class ScorecardFragment extends Fragment {
    private TGViewModel viewModel;
    private RecyclerView scorecardList;
    private ScorecardAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.scorecard, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scorecardList = view.findViewById(R.id.scorecardList);
        scorecardList.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ScorecardAdapter();
        scorecardList.setAdapter(adapter);
        view.findViewById(R.id.scorecardDelete).setOnClickListener(v -> onDeleteHand());
        viewModel = new ViewModelProvider(requireActivity()).get(TGViewModel.class);
        viewModel.getCurrentGame().observe(getViewLifecycleOwner(), game -> refreshDisplay());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) refreshDisplay();
    }

    private void refreshDisplay() {
        Game game = TGApp.getGame();
        if (game == null) return;
        View view = requireView();
        List<Player> players = game.getPlayers();
        ((TextView) view.findViewById(R.id.scorecardName1)).setText(players.get(0).getName());
        ((TextView) view.findViewById(R.id.scorecardName2)).setText(players.get(1).getName());
        ((TextView) view.findViewById(R.id.scorecardName3)).setText(players.get(2).getName());
        ((TextView) view.findViewById(R.id.scorecardName4)).setText(players.get(3).getName());
        adapter.notifyDataSetChanged();
    }

    private void onDeleteHand() {
        new AlertDialog.Builder(requireContext())
            .setMessage("Are you sure?")
            .setPositiveButton("Yes", (dialog, which) -> {
                Game game = TGApp.getGame();
                game.removeHand(game.getHands().size() - 1);
                viewModel.notifyGameChanged();
            })
            .setNegativeButton("No", null)
            .show();
    }

    private static class ScorecardAdapter extends RecyclerView.Adapter<ScorecardAdapter.ViewHolder> {

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView score1, score2, total1, total2;
            TextView[] tichus = new TextView[4];

            ViewHolder(View v) {
                super(v);
                score1 = v.findViewById(R.id.scorecardHandScore1);
                score2 = v.findViewById(R.id.scorecardHandScore2);
                total1 = v.findViewById(R.id.scorecardTotalScore1);
                total2 = v.findViewById(R.id.scorecardTotalScore2);
                tichus[0] = v.findViewById(R.id.scorecardTichu1);
                tichus[1] = v.findViewById(R.id.scorecardTichu2);
                tichus[2] = v.findViewById(R.id.scorecardTichu3);
                tichus[3] = v.findViewById(R.id.scorecardTichu4);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.scorecardrow, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Game game = TGApp.getGame();
            Hand hand = game.getHands().get(position);
            int score = hand.getTotalScore1();
            holder.score1.setText((score >= 0 ? "+" : "") + score);
            int score2 = hand.getTotalScore2();
            holder.score2.setText((score2 >= 0 ? "+" : "") + score2);

            for (int i = 0; i < 4; i++) {
                TextView tv = holder.tichus[i];
                if (hand.isTichuFor(i)) tv.setText("T");
                else if (hand.isGrandTichuFor(i)) tv.setText("GT");
                else tv.setText("");
                tv.setTextColor(hand.outFirst() == i ? 0xFF00AA00 : -65536);
            }

            int s1 = 0, s2 = 0;
            for (int i = 0; i <= position; i++) {
                Hand h = game.getHands().get(i);
                s1 += h.getTotalScore1();
                s2 += h.getTotalScore2();
            }
            holder.total1.setText(String.valueOf(s1));
            holder.total2.setText(String.valueOf(s2));
        }

        @Override
        public int getItemCount() {
            Game game = TGApp.getGame();
            return game != null ? game.getHands().size() : 0;
        }
    }
}
