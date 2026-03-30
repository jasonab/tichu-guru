package com.tichuguru;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.tichuguru.model.Game;
import com.tichuguru.model.Hand;
import com.tichuguru.model.Player;
import java.util.List;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

/* loaded from: classes.dex */
public class ScoreHandActivity extends Activity {
    private Hand hand;
    private WheelView outFirst;
    private WheelView score1;
    private WheelView score2;
    private Integer[] scores;
    private TextView total1;
    private TextView total2;

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scorehand);
        Bundle data = getIntent().getExtras();
        this.hand = (Hand) data.getSerializable("newHand");
        this.total1 = (TextView) findViewById(R.id.scoreHandTotal1);
        this.total2 = (TextView) findViewById(R.id.scoreHandTotal2);
        this.scores = new Integer[32];
        for (int i = 0; i < 31; i++) {
            this.scores[i] = Integer.valueOf((i * 5) - 25);
        }
        this.scores[31] = 200;
        OnWheelChangedListener changeListener = new OnWheelChangedListener() { // from class: com.tichuguru.ScoreHandActivity.1
            @Override // kankan.wheel.widget.OnWheelChangedListener
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                WheelView other;
                if (wheel != ScoreHandActivity.this.outFirst) {
                    if (wheel != ScoreHandActivity.this.score1) {
                        other = ScoreHandActivity.this.score1;
                    } else {
                        other = ScoreHandActivity.this.score2;
                    }
                    int val = ScoreHandActivity.this.scores[newValue].intValue();
                    int otherVal = val == 200 ? 0 : 100 - val;
                    int otherRow = (otherVal + 25) / 5;
                    if (val != 0 || other.getCurrentItem() != 31) {
                        other.setCurrentItem(otherRow);
                    }
                }
                ScoreHandActivity.this.updateHandScore();
            }
        };
        this.score1 = (WheelView) findViewById(R.id.scoreHandScore1);
        this.score1.setViewAdapter(new ArrayWheelAdapter(this, this.scores));
        this.score1.addChangingListener(changeListener);
        this.score2 = (WheelView) findViewById(R.id.scoreHandScore2);
        this.score2.setViewAdapter(new ArrayWheelAdapter(this, this.scores));
        this.score2.addChangingListener(changeListener);
        String[] names = new String[4];
        List<Player> players = TGApp.getGame().getPlayers();
        for (int i2 = 0; i2 < players.size(); i2++) {
            names[i2] = players.get(i2).getName();
        }
        this.outFirst = (WheelView) findViewById(R.id.scoreHandOutFirst);
        this.outFirst.setViewAdapter(new ArrayWheelAdapter(this, names));
        this.outFirst.addChangingListener(changeListener);
        Button button = (Button) findViewById(R.id.scoreHandSave);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.tichuguru.ScoreHandActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                ScoreHandActivity.this.onSave();
            }
        });
        TextView name = (TextView) findViewById(R.id.scoreHandName1);
        name.setText(players.get(0).getName());
        TextView name2 = (TextView) findViewById(R.id.scoreHandName2);
        name2.setText(players.get(1).getName());
        TextView name3 = (TextView) findViewById(R.id.scoreHandName3);
        name3.setText(players.get(2).getName());
        TextView name4 = (TextView) findViewById(R.id.scoreHandName4);
        name4.setText(players.get(3).getName());
        this.score1.setCurrentItem(15);
        this.score2.setCurrentItem(15);
        for (int i3 = 0; i3 < 4; i3++) {
            if (this.hand.isTichuFor(i3) || this.hand.isGrandTichuFor(i3)) {
                this.outFirst.setCurrentItem(i3);
                break;
            }
        }
        updateHandScore();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateHandScore() {
        this.hand.setCardScore1(this.scores[this.score1.getCurrentItem()]);
        this.hand.setCardScore2(this.scores[this.score2.getCurrentItem()]);
        this.hand.setOutFirst(this.outFirst.getCurrentItem());
        this.total1.setText(String.valueOf(this.hand.getTotalScore1()));
        this.total2.setText(String.valueOf(this.hand.getTotalScore2()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSave() {
        Game game = TGApp.getGame();
        game.scoreHand(this.hand);
        setResult(-1);
        finish();
    }
}
