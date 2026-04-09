package com.tichuguru

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tichuguru.model.Game
import com.tichuguru.model.Player

class TGActivity : AppCompatActivity() {
    private lateinit var curHandFragment: CurHandFragment
    private lateinit var scorecardFragment: ScorecardFragment
    private lateinit var allGamesFragment: AllGamesFragment
    private lateinit var statsFragment: StatsFragment
    private lateinit var activeFragment: Fragment
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fragmentContainer: FrameLayout
    private var navBarInsetBottom = 0
    lateinit var viewModel: TGViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        setContentView(R.layout.main)

        setSupportActionBar(findViewById(R.id.toolbar))

        val fm = supportFragmentManager
        if (savedInstanceState == null) {
            curHandFragment = CurHandFragment()
            scorecardFragment = ScorecardFragment()
            allGamesFragment = AllGamesFragment()
            statsFragment = StatsFragment()
            fm.beginTransaction()
                .add(R.id.fragment_container, statsFragment, "stats").hide(statsFragment)
                .add(R.id.fragment_container, allGamesFragment, "allgames").hide(allGamesFragment)
                .add(R.id.fragment_container, scorecardFragment, "scorecard").hide(scorecardFragment)
                .add(R.id.fragment_container, curHandFragment, "hand")
                .commit()
        } else {
            curHandFragment  = fm.findFragmentByTag("hand")      as CurHandFragment
            scorecardFragment = fm.findFragmentByTag("scorecard") as ScorecardFragment
            allGamesFragment  = fm.findFragmentByTag("allgames")  as AllGamesFragment
            statsFragment     = fm.findFragmentByTag("stats")     as StatsFragment
        }
        activeFragment = curHandFragment

        viewModel = ViewModelProvider(this)[TGViewModel::class.java]
        viewModel.getInitialized().observe(this) { initialized ->
            if (initialized && viewModel.getCurrentGame().value == null) createFirstGame()
        }

        bottomNav = findViewById(R.id.bottom_nav)
        fragmentContainer = findViewById(R.id.fragment_container)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root)) { v, windowInsets ->
            val statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navInsets  = windowInsets.getInsets(
                WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.systemGestures())
            v.setPadding(0, statusBars.top, 0, 0)
            navBarInsetBottom = navInsets.bottom
            bottomNav.setPadding(0, 0, 0, navBarInsetBottom)
            WindowInsetsCompat.CONSUMED
        }

        fm.addOnBackStackChangedListener {
            val subScreen = fm.backStackEntryCount > 0
            bottomNav.visibility = if (subScreen) View.GONE else View.VISIBLE
            fragmentContainer.setPadding(0, 0, 0, if (subScreen) navBarInsetBottom else 0)
            supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(subScreen)
                if (!subScreen) setTitle(R.string.app_name)
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            val target: Fragment = when (item.itemId) {
                R.id.nav_hand      -> curHandFragment
                R.id.nav_scorecard -> scorecardFragment
                R.id.nav_allgames  -> allGamesFragment
                else               -> statsFragment
            }
            if (target != activeFragment) switchFragment(target)
            true
        }
    }

    fun pushFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .add(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            return true
        }
        return super.onSupportNavigateUp()
    }

    private fun switchFragment(target: Fragment) {
        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(target)
            .commit()
        activeFragment = target
    }

    fun navigateToTab(tabIndex: Int) {
        bottomNav.selectedItemId = when (tabIndex) {
            1    -> R.id.nav_scorecard
            2    -> R.id.nav_allgames
            3    -> R.id.nav_stats
            else -> R.id.nav_hand
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isInitialized) {
            viewModel.sync()
            if (viewModel.getCurrentGame().value == null) createFirstGame()
        }
    }

    fun createFirstGame() {
        val allPlayers = viewModel.players
        val players = List(4) { i -> if (i < allPlayers.size) allPlayers[i] else Player("New Player") }
        val curGame = viewModel.curGame
        pushFragment(NewGameFragment.newInstance(if (curGame == null) Game(players) else Game(curGame), allPlayers))
    }
}
