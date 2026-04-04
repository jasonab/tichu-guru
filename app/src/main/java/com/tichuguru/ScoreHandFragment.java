package com.tichuguru;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.tichuguru.model.Hand;
import com.tichuguru.model.Player;
import java.util.List;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

public class ScoreHandFragment extends Fragment {
    private Hand hand;
    private WheelView outFirst;
    private WheelView score1;
    private WheelView score2;
    private Integer[] scores;
    private TextView total1;
    private TextView total2;

    public static ScoreHandFragment newInstance(Hand hand) {
        ScoreHandFragment f = new ScoreHandFragment();
        Bundle args = new Bundle();
        args.putSerializable("newHand", hand);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.scorehand, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Score Hand");

        hand = (Hand) requireArguments().getSerializable("newHand");
        total1 = view.findViewById(R.id.scoreHandTotal1);
        total2 = view.findViewById(R.id.scoreHandTotal2);

        scores = new Integer[32];
        for (int i = 0; i < 31; i++) scores[i] = (i * 5) - 25;
        scores[31] = 200;

        OnWheelChangedListener changeListener = (wheel, oldValue, newValue) -> {
            if (wheel != outFirst) {
                WheelView other = (wheel == score1) ? score2 : score1;
                int val = scores[newValue];
                int otherVal = val == 200 ? 0 : 100 - val;
                int otherRow = (otherVal + 25) / 5;
                if (val != 0 || other.getCurrentItem() != 31) {
                    other.setCurrentItem(otherRow);
                }
            }
            updateHandScore();
        };

        score1 = view.findViewById(R.id.scoreHandScore1);
        score1.setViewAdapter(new ArrayWheelAdapter<>(requireContext(), scores));
        score1.addChangingListener(changeListener);

        score2 = view.findViewById(R.id.scoreHandScore2);
        score2.setViewAdapter(new ArrayWheelAdapter<>(requireContext(), scores));
        score2.addChangingListener(changeListener);

        List<Player> players = TGApp.getGame().getPlayers();
        String[] names = new String[4];
        for (int i = 0; i < 4; i++) names[i] = players.get(i).getName();

        outFirst = view.findViewById(R.id.scoreHandOutFirst);
        outFirst.setViewAdapter(new ArrayWheelAdapter<>(requireContext(), names));
        outFirst.addChangingListener(changeListener);

        view.findViewById(R.id.scoreHandSave).setOnClickListener(v -> onSave());

        ((TextView) view.findViewById(R.id.scoreHandName1)).setText(players.get(0).getName());
        ((TextView) view.findViewById(R.id.scoreHandName2)).setText(players.get(1).getName());
        ((TextView) view.findViewById(R.id.scoreHandName3)).setText(players.get(2).getName());
        ((TextView) view.findViewById(R.id.scoreHandName4)).setText(players.get(3).getName());

        score1.setCurrentItem(15);
        score2.setCurrentItem(15);
        for (int i = 0; i < 4; i++) {
            if (hand.isTichuFor(i) || hand.isGrandTichuFor(i)) {
                outFirst.setCurrentItem(i);
                break;
            }
        }
        updateHandScore();
    }

    private void updateHandScore() {
        hand.setCardScore1(scores[score1.getCurrentItem()]);
        hand.setCardScore2(scores[score2.getCurrentItem()]);
        hand.setOutFirst(outFirst.getCurrentItem());
        total1.setText(String.valueOf(hand.getTotalScore1()));
        total2.setText(String.valueOf(hand.getTotalScore2()));
    }

    private void onSave() {
        TGApp app = (TGApp) requireActivity().getApplication();
        TGApp.getGame().scoreHand(hand);
        app.saveGames();
        app.savePlayers();
        getParentFragmentManager().setFragmentResult("score_hand", new Bundle());
        getParentFragmentManager().popBackStack();
    }
}
