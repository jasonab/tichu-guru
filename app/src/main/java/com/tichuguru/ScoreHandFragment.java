package com.tichuguru;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.tichuguru.model.Hand;
import com.tichuguru.model.Player;
import java.util.List;

public class ScoreHandFragment extends Fragment {
    private Hand hand;
    private NumberPicker outFirst;
    private NumberPicker score1;
    private NumberPicker score2;
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

        String[] scoreLabels = new String[Hand.CARD_SCORE_OPTIONS.length];
        for (int i = 0; i < Hand.CARD_SCORE_OPTIONS.length; i++) {
            scoreLabels[i] = String.valueOf(Hand.CARD_SCORE_OPTIONS[i]);
        }

        NumberPicker.OnValueChangeListener changeListener = (picker, oldVal, newVal) -> {
            if (picker != outFirst) {
                NumberPicker other = (picker == score1) ? score2 : score1;
                int val = Hand.CARD_SCORE_OPTIONS[newVal];
                int otherVal = Hand.otherCardScore(val);
                if (val != 0 || other.getValue() != Hand.cardScoreIndex(200)) {
                    other.setValue(Hand.cardScoreIndex(otherVal));
                }
            }
            updateHandScore();
        };

        score1 = view.findViewById(R.id.scoreHandScore1);
        score1.setMinValue(0);
        score1.setMaxValue(Hand.CARD_SCORE_OPTIONS.length - 1);
        score1.setDisplayedValues(scoreLabels);
        score1.setWrapSelectorWheel(false);
        score1.setOnValueChangedListener(changeListener);

        score2 = view.findViewById(R.id.scoreHandScore2);
        score2.setMinValue(0);
        score2.setMaxValue(Hand.CARD_SCORE_OPTIONS.length - 1);
        score2.setDisplayedValues(scoreLabels);
        score2.setWrapSelectorWheel(false);
        score2.setOnValueChangedListener(changeListener);

        List<Player> players = TGApp.getGame().getPlayers();
        String[] names = new String[4];
        for (int i = 0; i < 4; i++) names[i] = players.get(i).getName();

        outFirst = view.findViewById(R.id.scoreHandOutFirst);
        outFirst.setMinValue(0);
        outFirst.setMaxValue(3);
        outFirst.setDisplayedValues(names);
        outFirst.setWrapSelectorWheel(true);
        outFirst.setOnValueChangedListener(changeListener);

        view.findViewById(R.id.scoreHandSave).setOnClickListener(v -> onSave());

        ((TextView) view.findViewById(R.id.scoreHandName1)).setText(players.get(0).getName());
        ((TextView) view.findViewById(R.id.scoreHandName2)).setText(players.get(1).getName());
        ((TextView) view.findViewById(R.id.scoreHandName3)).setText(players.get(2).getName());
        ((TextView) view.findViewById(R.id.scoreHandName4)).setText(players.get(3).getName());

        score1.setValue(Hand.cardScoreIndex(50));
        score2.setValue(Hand.cardScoreIndex(50));
        for (int i = 0; i < 4; i++) {
            if (hand.isTichuFor(i) || hand.isGrandTichuFor(i)) {
                outFirst.setValue(i);
                break;
            }
        }
        updateHandScore();
    }

    private void updateHandScore() {
        hand.setCardScore1(Hand.CARD_SCORE_OPTIONS[score1.getValue()]);
        hand.setCardScore2(Hand.CARD_SCORE_OPTIONS[score2.getValue()]);
        hand.setOutFirst(outFirst.getValue());
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
