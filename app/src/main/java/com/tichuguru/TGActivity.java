package com.tichuguru;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
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
    private FrameLayout fragmentContainer;
    private int navBarInsetBottom = 0;
    TGViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        fragmentContainer = findViewById(R.id.fragment_container);

        View root = findViewById(R.id.main_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            Insets navInsets = windowInsets.getInsets(
                WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.systemGestures());
            v.setPadding(0, statusBars.top, 0, 0);
            navBarInsetBottom = navInsets.bottom;
            bottomNav.setPadding(0, 0, 0, navBarInsetBottom);
            return WindowInsetsCompat.CONSUMED;
        });

        fm.addOnBackStackChangedListener(() -> {
            boolean subScreen = fm.getBackStackEntryCount() > 0;
            bottomNav.setVisibility(subScreen ? View.GONE : View.VISIBLE);
            fragmentContainer.setPadding(0, 0, 0, subScreen ? navBarInsetBottom : 0);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(subScreen);
                if (!subScreen) setTitle(R.string.app_name);
            }
        });

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

    /** Push a sub-screen fragment over the current tab, hiding the BottomNav. */
    public void pushFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
            .hide(activeFragment)
            .add(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onSupportNavigateUp();
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

    public void createFirstGame() {
        List<Player> allPlayers = TGApp.getPlayers();
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            players.add(i < allPlayers.size() ? allPlayers.get(i) : new Player("New Player"));
        }
        Game curGame = TGApp.getGame();
        Game game = curGame == null ? new Game(players) : new Game(curGame);
        pushFragment(NewGameFragment.newInstance(game));
    }
}
