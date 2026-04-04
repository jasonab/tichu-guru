# Tichu Guru

Android scoring and statistics app for the card game Tichu (4-player partnership trick-taking).

## Build Commands

```bash
./gradlew assembleDebug    # Build debug APK
./gradlew assembleRelease  # Build release APK
./gradlew clean            # Clean build outputs
```

No tests exist in this project.

IMPORTANT: Run `./gradlew assembleDebug` before considering any change complete. After any change to `AndroidManifest.xml` or `build.gradle`, use `./gradlew clean assembleDebug`.

## Tech Stack

- **Language:** Java 17 — **Min SDK:** 30, **Target SDK:** 34
- **AGP:** 9.1.0, **Gradle:** 9.3.1, **Package:** `com.tichuguru`
- **UI:** AppCompat + Material Components + BottomNavigationView
- **Persistence:** Room database (`tichu.db`, version 1)
- **State:** `TGApp` singleton (in-memory) + `TGViewModel` LiveData (Fragment UI)

## File Structure

```
com.tichuguru/
├── TGActivity.java          # Single-Activity host (BottomNav, Toolbar)
├── TGApp.java               # Application singleton — global state + DB I/O
├── TGViewModel.java         # Shared LiveData for all Fragments
├── CurHandFragment.java     # Tab: current hand, Tichu bids
├── ScorecardFragment.java   # Tab: scorecard for current game
├── AllGamesFragment.java    # Tab: historical game list
├── StatsFragment.java       # Tab: statistics dashboard
├── NewGameFragment.java     # Sub-screen: new game setup
├── ScoreHandFragment.java   # Sub-screen: score entry (uses kankan wheels)
├── StatsListFragment.java   # Sub-screen: per-player stats + rankings
├── ui/
│   └── SegmentedControlButton.java  # Custom RadioButton for Tichu/GT selection
├── model/
│   ├── Game.java            # Game state + Hand list + scoring logic
│   ├── Hand.java            # One hand: bids, card points, outcomes
│   └── Player.java          # Player profile + cumulative stats
└── db/
    ├── TichuDatabase.java   # Room DB — version 1, add Migration for schema changes
    ├── GameEntity/Dao
    ├── HandEntity/Dao
    └── PlayerEntity/Dao

kankan/wheel/widget/         # Embedded scroll-wheel library (not a Gradle dep)
```

## Key Patterns

- **Global state:** `TGApp.getGame()`, `TGApp.getGames()`, `TGApp.getPlayers()`
- **Saving data:** Call `TGApp.saveGames()` / `TGApp.savePlayers()` at each mutation point. NEVER defer to `onPause`.
- **Rule logic** belongs in `model/` classes, not Activities or Fragments.
- **DB schema changes:** increment `version` in `@Database` and add `Migration(n, n+1)` in `TichuDatabase`. Do NOT use `fallbackToDestructiveMigration()` for new versions.
- **Game/Hand inter-screen passing:** `Bundle.putSerializable()` — both implement `Serializable`.
- **Colors:** `Color.YELLOW` / `Color.GRAY` — never raw integer literals.
- **Game rule constants** (bonus scores, thresholds): `static final` on the relevant model class.

## Known Issues

See `IMPROVEMENTS.md` for the full tracked list. Key pending items:
- **#12** — Reflection in `StatsFragment.Getter` should be replaced with typed lambdas
- **#18** — `kankan.wheel.widget` should be replaced with `NumberPicker`
- **#12** — Reflection in `StatsFragment.Getter` should be replaced with typed lambdas
