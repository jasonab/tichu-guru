package com.tichuguru;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import com.tichuguru.model.Game;
import com.tichuguru.model.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/* loaded from: classes.dex */
public class NewGameActivity extends AppCompatActivity {
    private CheckBox addOnFailedTichuCB;
    private boolean addingPlayer = false;
    private CheckBox affectStatsCB;
    private Game game;
    private EditText gameLimit;
    private CheckBox mercyRuleCB;
    private List<Spinner> nameSpinners;
    private ArrayAdapter<String> spinAdapter;

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newgame);
        Bundle data = getIntent().getExtras();
        this.game = (Game) data.getSerializable("newGame");
        this.gameLimit = findViewById(R.id.newGameGameLimit);
        this.addOnFailedTichuCB = findViewById(R.id.newGameAddOnFailedTichu);
        this.affectStatsCB = findViewById(R.id.newGameAffectsStats);
        this.mercyRuleCB = findViewById(R.id.newGameMercyRule);
        Button button = findViewById(R.id.newGameRandomizeTeams);
        button.setOnClickListener(v -> NewGameActivity.this.onRandomizeTeams());
        Button button2 = findViewById(R.id.newGameStart);
        // from class: com.tichuguru.NewGameActivity.2
// android.view.View.OnClickListener
        button2.setOnClickListener(v -> NewGameActivity.this.onStartGame());
        this.nameSpinners = new ArrayList();
        this.nameSpinners.add(findViewById(R.id.newGameName1));
        this.nameSpinners.add(findViewById(R.id.newGameName2));
        this.nameSpinners.add(findViewById(R.id.newGameName3));
        this.nameSpinners.add(findViewById(R.id.newGameName4));
        List<Player> allPlayers = TGApp.getPlayers();
        List<String> choices = new ArrayList<>();
        for (int i = 0; i < allPlayers.size(); i++) {
            choices.add(allPlayers.get(i).getName());
        }
        choices.add("New Player");
        this.spinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, choices);
        for (int i2 = 0; i2 < 4; i2++) {
            Spinner spin = this.nameSpinners.get(i2);
            spin.setOnItemSelectedListener(new PlayerSelectedListener(i2));
            spin.setAdapter(this.spinAdapter);
        }
        updateNameSpinners();
        this.affectStatsCB.setChecked(true);
        this.addOnFailedTichuCB.setChecked(this.game.isAddOnFailure());
        this.mercyRuleCB.setChecked(this.game.isMercyRule());
    }

    @Override // android.app.Activity
    protected void onPause() {
        TGApp app = (TGApp) getApplication();
        app.savePlayers(null);
        super.onPause();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRandomizeTeams() {
        Random rand = new Random();
        List<Player> players = this.game.getPlayers();
        for (int i = 3; i >= 1; i--) {
            int j = rand.nextInt(i + 1);
            if (j != i) {
                Player p = players.get(i);
                players.set(i, players.get(j));
                players.set(j, p);
            }
        }
        updateNameSpinners();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onStartGame() {
        int limit;
        List<Player> players = this.game.getPlayers();
        for (int i = 0; i < 4; i++) {
            Player p = players.get(i);
            for (int j = i + 1; j < 4; j++) {
                if (p.getName().equals(players.get(j).getName())) {
                    new AlertDialog.Builder(this).setMessage("You selected the same player twice").show();
                    return;
                }
            }
        }
        boolean badParse = false;
        try {
            limit = Integer.parseInt(this.gameLimit.getText().toString());
        } catch (Exception e) {
            badParse = true;
            limit = 0;
        }
        if (badParse || limit < 1) {
            new AlertDialog.Builder(this).setMessage("Enter a valid game limit").show();
            return;
        }
        this.game.setGameLimit(Integer.valueOf(this.gameLimit.getText().toString()).intValue());
        this.game.setAddOnFailure(this.addOnFailedTichuCB.isChecked());
        this.game.setIgnoreStats(!this.affectStatsCB.isChecked());
        this.game.setMercyRule(this.mercyRuleCB.isChecked());
        List<Game> games = TGApp.getGames();
        games.add(this.game);
        TGApp.setGame(this.game);
        setResult(-1);
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateNameSpinners() {
        List<Player> players = this.game.getPlayers();
        for (int i = 0; i < 4; i++) {
            Spinner spin = this.nameSpinners.get(i);
            Player p = players.get(i);
            int index = -1;
            int j = 0;
            while (true) {
                if (j < this.spinAdapter.getCount()) {
                    if (!this.spinAdapter.getItem(j).equals(p.getName())) {
                        j++;
                    } else {
                        index = j;
                        break;
                    }
                }
            }
            spin.setSelection(index);
        }
    }

    /* loaded from: classes.dex */
    private class PlayerSelectedListener implements AdapterView.OnItemSelectedListener {
        private int playerNum;

        public PlayerSelectedListener(int playerNum) {
            this.playerNum = playerNum;
        }

        @Override // android.widget.AdapterView.OnItemSelectedListener
        public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long id) {
            List<Player> allPlayers = TGApp.getPlayers();
            if (index < allPlayers.size()) {
                NewGameActivity.this.game.setPlayer(this.playerNum, allPlayers.get(index));
            } else if (!NewGameActivity.this.addingPlayer) {
                NewGameActivity.this.addingPlayer = true;
                getNewPlayerName();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void getNewPlayerName() {
            AlertDialog.Builder alert = new AlertDialog.Builder(NewGameActivity.this);
            alert.setTitle("New Player");
            alert.setMessage("Enter the new player's name");
            final EditText input = new EditText(NewGameActivity.this);
            alert.setView(input);
            alert.setCancelable(false);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() { // from class: com.tichuguru.NewGameActivity.PlayerSelectedListener.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int whichButton) {
                    Editable value = input.getText();
                    String name = value.toString();
                    if (name.length() == 0) {
                        NewGameActivity.this.runOnUiThread(new Runnable() { // from class: com.tichuguru.NewGameActivity.PlayerSelectedListener.1.1
                            @Override // java.lang.Runnable
                            public void run() {
                                PlayerSelectedListener.this.getNewPlayerName();
                            }
                        });
                        return;
                    }
                    Player newPlayer = null;
                    List<Player> allPlayers = TGApp.getPlayers();
                    Iterator<Player> it = allPlayers.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        Player p = it.next();
                        if (name.equals(p.getName())) {
                            AlertDialog.Builder errAlert = new AlertDialog.Builder(NewGameActivity.this);
                            errAlert.setMessage("That player already exists!");
                            errAlert.show();
                            newPlayer = p;
                            break;
                        }
                    }
                    if (newPlayer == null) {
                        newPlayer = new Player(name);
                        allPlayers.add(newPlayer);
                        Collections.sort(allPlayers);
                        NewGameActivity.this.spinAdapter.insert(newPlayer.getName(), allPlayers.indexOf(newPlayer));
                    }
                    NewGameActivity.this.game.setPlayer(PlayerSelectedListener.this.playerNum, newPlayer);
                    NewGameActivity.this.addingPlayer = false;
                    NewGameActivity.this.runOnUiThread(new Runnable() { // from class: com.tichuguru.NewGameActivity.PlayerSelectedListener.1.2
                        @Override // java.lang.Runnable
                        public void run() {
                            NewGameActivity.this.updateNameSpinners();
                        }
                    });
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.tichuguru.NewGameActivity.PlayerSelectedListener.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int whichButton) {
                    NewGameActivity.this.addingPlayer = false;
                }
            });
            alert.show();
        }

        @Override // android.widget.AdapterView.OnItemSelectedListener
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
}
