package com.tichuguru;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import com.tichuguru.model.Game;
import com.tichuguru.model.Player;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class TGActivity extends TabActivity {
    @Override // android.app.ActivityGroup, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(524288);
        setContentView(R.layout.main);
        TabHost tabHost = getTabHost();
        Intent intent = new Intent().setClass(this, CurHandActivity.class);
        TabHost.TabSpec spec = tabHost.newTabSpec("current").setIndicator("Hand").setContent(intent);
        tabHost.addTab(spec);
        Intent intent2 = new Intent().setClass(this, ScorecardActivity.class);
        TabHost.TabSpec spec2 = tabHost.newTabSpec("scorecard").setIndicator("Scorecard").setContent(intent2);
        tabHost.addTab(spec2);
        Intent intent3 = new Intent().setClass(this, AllGamesActivity.class);
        TabHost.TabSpec spec3 = tabHost.newTabSpec("allgames").setIndicator("All Games").setContent(intent3);
        tabHost.addTab(spec3);
        Intent intent4 = new Intent().setClass(this, StatsActivity.class);
        TabHost.TabSpec spec4 = tabHost.newTabSpec("stats").setIndicator("Stats").setContent(intent4);
        tabHost.addTab(spec4);
    }

    @Override // android.app.ActivityGroup, android.app.Activity
    protected void onResume() {
        super.onResume();
        if (TGApp.getGame() == null) {
            createFirstGame();
        }
    }

    @Override // android.app.ActivityGroup, android.app.Activity
    protected void onPause() {
        TGApp app = (TGApp) getApplication();
        app.savePlayers(null);
        app.saveGames(null);
        super.onPause();
    }

    public void createFirstGame() {
        Game game;
        Game curGame = TGApp.getGame();
        if (curGame == null) {
            List<Player> allPlayers = TGApp.getPlayers();
            List<Player> players = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                if (i < allPlayers.size()) {
                    players.add(allPlayers.get(i));
                } else {
                    players.add(new Player("New Player"));
                }
            }
            game = new Game(players);
        } else {
            game = new Game(curGame);
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable("newGame", game);
        Intent intent = new Intent(this, (Class<?>) NewGameActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
