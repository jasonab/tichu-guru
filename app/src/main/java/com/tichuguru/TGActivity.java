package com.tichuguru;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tichuguru.model.Game;
import com.tichuguru.model.Player;
import java.util.ArrayList;
import java.util.List;

public class TGActivity extends AppCompatActivity {
    private CurHandFragment curHandFragment;
    private ScorecardFragment scorecardFragment;
    private AllGamesFragment allGamesFragment;
    private StatsFragment statsFragment;
    private Fragment activeFragment;
    private BottomNavigationView bottomNav;
    TGViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(524288);
        setContentView(R.layout.main);

        FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState == null) {
            curHandFragment = new CurHandFragment();
            scorecardFragment = new ScorecardFragment();
            allGamesFragment = new AllGamesFragment();
            statsFragment = new StatsFragment();
            fm.beginTransaction()
                .add(R.id.fragment_container, statsFragment, "stats").hide(statsFragment)
                .add(R.id.fragment_container, allGamesFragment, "allgames").hide(allGamesFragment)
                .add(R.id.fragment_container, scorecardFragment, "scorecard").hide(scorecardFragment)
                .add(R.id.fragment_container, curHandFragment, "hand")
                .commit();
        } else {
            curHandFragment = (CurHandFragment) fm.findFragmentByTag("hand");
            scorecardFragment = (ScorecardFragment) fm.findFragmentByTag("scorecard");
            allGamesFragment = (AllGamesFragment) fm.findFragmentByTag("allgames");
            statsFragment = (StatsFragment) fm.findFragmentByTag("stats");
        }
        activeFragment = curHandFragment;

        viewModel = new ViewModelProvider(this).get(TGViewModel.class);
        viewModel.sync();

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment target;
            if (id == R.id.nav_hand)          target = curHandFragment;
            else if (id == R.id.nav_scorecard) target = scorecardFragment;
            else if (id == R.id.nav_allgames)  target = allGamesFragment;
            else                               target = statsFragment;
            if (target != activeFragment) switchFragment(target);
            return true;
        });
    }

    private void switchFragment(Fragment target) {
        getSupportFragmentManager().beginTransaction()
            .hide(activeFragment)
            .show(target)
            .commit();
        activeFragment = target;
    }

    public void navigateToTab(int tabIndex) {
        int menuId;
        switch (tabIndex) {
            case 1:  menuId = R.id.nav_scorecard; break;
            case 2:  menuId = R.id.nav_allgames;  break;
            case 3:  menuId = R.id.nav_stats;      break;
            default: menuId = R.id.nav_hand;       break;
        }
        bottomNav.setSelectedItemId(menuId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.sync();
        if (TGApp.getGame() == null) {
            createFirstGame();
        }
    }

    @Override
    protected void onPause() {
        TGApp app = (TGApp) getApplication();
        app.savePlayers();
        app.saveGames();
        super.onPause();
    }

    public void createFirstGame() {
        Game curGame = TGApp.getGame();
        List<Player> allPlayers = TGApp.getPlayers();
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            players.add(i < allPlayers.size() ? allPlayers.get(i) : new Player("New Player"));
        }
        Game game = curGame == null ? new Game(players) : new Game(curGame);
        Bundle bundle = new Bundle();
        bundle.putSerializable("newGame", game);
        Intent intent = new Intent(this, NewGameActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
