package com.tichuguru;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import java.io.File;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.tichuguru.model.Game;
import com.tichuguru.model.Hand;
import com.tichuguru.model.Player;
import java.util.List;
import kankan.wheel.widget.WheelScroller;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

/* loaded from: classes.dex */
public class CurHandActivity extends AppCompatActivity {
    private static final int MENU_END_GAME = 0;
    private static final int MENU_EXPORTCSV = 2;
    private static final int MENU_QUIT = 1;
    public static boolean clearTichuButtonsNow = false;
    RadioGroup grp1;
    RadioGroup grp2;
    RadioGroup grp3;
    RadioGroup grp4;
    TextView name1;
    TextView name2;
    TextView name3;
    TextView name4;
    TextView score1;
    TextView score2;
    Button scoreHandButton;

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.curhand);
        this.score1 = findViewById(R.id.curHandScore1);
        this.score2 = findViewById(R.id.curHandScore2);
        this.name1 = findViewById(R.id.curHandP1);
        this.name2 = findViewById(R.id.curHandP2);
        this.name3 = findViewById(R.id.curHandP3);
        this.name4 = findViewById(R.id.curHandP4);
        this.grp1 = findViewById(R.id.curHandP1RG);
        this.grp2 = findViewById(R.id.curHandP2RG);
        this.grp3 = findViewById(R.id.curHandP3RG);
        this.grp4 = findViewById(R.id.curHandP4RG);

        this.scoreHandButton = findViewById(R.id.curHandScoreHand);
        this.scoreHandButton.setOnClickListener(v -> CurHandActivity.this.onScoreHand());

        Button button = findViewById(R.id.curHandNewGame);
        button.setOnClickListener(v -> CurHandActivity.this.onNewGame());

        if (savedInstanceState != null) {
            this.grp1.check(savedInstanceState.getInt("tichu1", R.id.curHandP1None));
            this.grp2.check(savedInstanceState.getInt("tichu2", R.id.curHandP2None));
            this.grp3.check(savedInstanceState.getInt("tichu3", R.id.curHandP3None));
            this.grp4.check(savedInstanceState.getInt("tichu4", R.id.curHandP4None));
        }
        updateDisplay();
    }

    @Override // android.app.Activity
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("tichu1", this.grp1.getCheckedRadioButtonId());
        outState.putInt("tichu2", this.grp2.getCheckedRadioButtonId());
        outState.putInt("tichu3", this.grp3.getCheckedRadioButtonId());
        outState.putInt("tichu4", this.grp4.getCheckedRadioButtonId());
        super.onSaveInstanceState(outState);
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        if (clearTichuButtonsNow) {
            clearTichuButtonsNow = false;
            clearTichuButtons();
        }
        updateDisplay();
    }

    @Override // android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            clearTichuButtons();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onNewGame() {
        Game curGame = TGApp.getGame();
        Bundle bundle = new Bundle();
        bundle.putSerializable("newGame", new Game(curGame));
        Intent intent = new Intent(this, NewGameActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, 1);
    }

    private void onEndGame() {
        Game game = TGApp.getGame();
        if (game.getScore1() == game.getScore2()) {
            new AlertDialog.Builder(this).setMessage("You can't end the game when the score is tied.").show();
            return;
        }
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case AbstractWheelTextAdapter.TEXT_VIEW_ITEM_RESOURCE /* -1 */:
                    TGApp.getGame().endGame();
                    TGApp app = (TGApp) getApplication();
                    app.saveGames();
                    app.savePlayers();
                    CurHandActivity.this.updateDisplay();
                    return;
                default:
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
    }

    public void onScoreHand() {
        Hand hand = new Hand(TGApp.getGame());
        if (this.grp1.getCheckedRadioButtonId() == R.id.curHandP1GT) {
            hand.setGrandTichuFor(0);
        }
        if (this.grp1.getCheckedRadioButtonId() == R.id.curHandP1T) {
            hand.setTichuFor(0);
        }
        if (this.grp2.getCheckedRadioButtonId() == R.id.curHandP2GT) {
            hand.setGrandTichuFor(1);
        }
        if (this.grp2.getCheckedRadioButtonId() == R.id.curHandP2T) {
            hand.setTichuFor(1);
        }
        if (this.grp3.getCheckedRadioButtonId() == R.id.curHandP3GT) {
            hand.setGrandTichuFor(2);
        }
        if (this.grp3.getCheckedRadioButtonId() == R.id.curHandP3T) {
            hand.setTichuFor(2);
        }
        if (this.grp4.getCheckedRadioButtonId() == R.id.curHandP4GT) {
            hand.setGrandTichuFor(3);
        }
        if (this.grp4.getCheckedRadioButtonId() == R.id.curHandP4T) {
            hand.setTichuFor(3);
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable("newHand", hand);
        Intent intent = new Intent(this, ScoreHandActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, 0);
    }

    private void clearTichuButtons() {
        this.grp1.check(R.id.curHandP1None);
        this.grp2.check(R.id.curHandP2None);
        this.grp3.check(R.id.curHandP3None);
        this.grp4.check(R.id.curHandP4None);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDisplay() {
        Game game = TGApp.getGame();
        if (game != null) {
            this.score1.setText(String.valueOf(game.getScore1()));
            this.score2.setText(String.valueOf(game.getScore2()));
            List<Player> players = game.getPlayers();
            this.name1.setText(players.get(0).getName());
            this.name2.setText(players.get(1).getName());
            this.name3.setText(players.get(2).getName());
            this.name4.setText(players.get(3).getName());
            if (game.isGameOver()) {
                if (game.getScore1() > game.getScore2()) {
                    this.score1.setTextColor(-256);
                    this.name1.setTextColor(-256);
                    this.name3.setTextColor(-256);
                    this.score2.setTextColor(-7829368);
                    this.name2.setTextColor(-7829368);
                    this.name4.setTextColor(-7829368);
                } else {
                    this.score1.setTextColor(-7829368);
                    this.name1.setTextColor(-7829368);
                    this.name3.setTextColor(-7829368);
                    this.score2.setTextColor(-256);
                    this.name2.setTextColor(-256);
                    this.name4.setTextColor(-256);
                }
                this.scoreHandButton.setEnabled(false);
                return;
            }
            this.score1.setTextColor(-7829368);
            this.name1.setTextColor(-7829368);
            this.name3.setTextColor(-7829368);
            this.score2.setTextColor(-7829368);
            this.name2.setTextColor(-7829368);
            this.name4.setTextColor(-7829368);
            this.scoreHandButton.setEnabled(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    private void exportCsv() {
        File tichuDir = new File(Environment.getExternalStorageDirectory(), "TichuGuru");
        if (!tichuDir.exists() && !tichuDir.mkdir()) {
            new AlertDialog.Builder(this).setMessage("Couldn't access /sdcard/TichuGuru.  Do you need to turn off USB storage?").show();
            return;
        }
        TGApp app = (TGApp) getApplication();
        app.saveCSV(new File(tichuDir, TGApp.CSV_FILE));
        new AlertDialog.Builder(this).setMessage("Data saved to /sdcard/TichuGuru.").show();
    }

    @Override // android.app.Activity
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        menu.add(0, MENU_END_GAME, 0, "End Game");
        menu.add(0, MENU_EXPORTCSV, 0, "Export CSV");
        menu.add(0, MENU_QUIT, 0, "Quit");
        return true;
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case MENU_END_GAME:
                onEndGame();
                return true;
            case MENU_QUIT:
                finish();
                return true;
            case MENU_EXPORTCSV:
                exportCsv();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
