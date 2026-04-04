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
import com.tichuguru.model.Player;
import java.util.List;

public class StatsListFragment extends Fragment {
    private Player player;

    public static StatsListFragment newInstance(String title, String[] labels, String[] values, String playerName) {
        StatsListFragment f = new StatsListFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putSerializable("labels", labels);
        args.putSerializable("values", values);
        if (playerName != null) args.putString("playerName", playerName);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String playerName = requireArguments().getString("playerName");
        player = playerName != null ? TGApp.getPlayer(playerName) : null;
        return inflater.inflate(player == null ? R.layout.rankinglist : R.layout.statslist, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = requireArguments();
        requireActivity().setTitle(args.getString("title"));

        String[] labels = (String[]) args.getSerializable("labels");
        String[] values = (String[]) args.getSerializable("values");

        RecyclerView statsList = view.findViewById(R.id.statsList);
        statsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        statsList.setAdapter(new StatsAdapter(labels, values));

        if (player != null) {
            view.findViewById(R.id.statsClearPlayerStats).setOnClickListener(v -> onClearStats());
            view.findViewById(R.id.statsDelPlayer).setOnClickListener(v -> onDeletePlayer());
        }
    }

    private void onClearStats() {
        new AlertDialog.Builder(requireContext())
            .setMessage("Are you sure?")
            .setPositiveButton("Yes", (dialog, which) -> {
                player.clearStats();
                ((TGApp) requireActivity().getApplication()).savePlayers();
                new ViewModelProvider(requireActivity()).get(TGViewModel.class).notifyPlayersChanged();
                getParentFragmentManager().popBackStack();
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void onDeletePlayer() {
        new AlertDialog.Builder(requireContext())
            .setMessage("Are you sure?")
            .setPositiveButton("Yes", (dialog, which) -> {
                boolean updateSharedGame = false;
                List<Game> games = TGApp.getGames();
                for (int i = games.size() - 1; i >= 0; i--) {
                    Game game = games.get(i);
                    if (game.containsPlayer(player)) {
                        games.remove(i);
                        if (game == TGApp.getGame()) updateSharedGame = true;
                    }
                }
                if (updateSharedGame) {
                    TGApp.setGame(games.isEmpty() ? null : games.get(games.size() - 1));
                }
                TGApp.getPlayers().remove(player);
                TGApp app = (TGApp) requireActivity().getApplication();
                app.saveGames();
                app.savePlayers();
                TGViewModel vm = new ViewModelProvider(requireActivity()).get(TGViewModel.class);
                vm.notifyGamesChanged();
                vm.notifyPlayersChanged();
                getParentFragmentManager().popBackStack();
            })
            .setNegativeButton("No", null)
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

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.statslistrow, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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
        public int getItemCount() { return labels.length; }
    }
}
