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

- **Language:** Kotlin — **Min SDK:** 30, **Target/Compile SDK:** 36
- **AGP:** 9.1.0, **Gradle:** 9.3.1, **Package:** `com.tichuguru`
- **UI:** AppCompat + Material Components + BottomNavigationView
- **Persistence:** Room 2.8.4 (`tichu.db`, version 1) — KSP for code generation
- **State:** `TGApp` singleton (in-memory) + `TGViewModel` LiveData (Fragment UI)

## File Structure

```
app/src/main/kotlin/com/tichuguru/
├── TGActivity.kt            # Single-Activity host (BottomNav, Toolbar)
├── TGApp.kt                 # Application singleton — global state + DB I/O
├── TGViewModel.kt           # Shared LiveData for all Fragments
├── CurHandFragment.kt       # Tab: current hand, Tichu bids
├── ScorecardFragment.kt     # Tab: scorecard for current game
├── AllGamesFragment.kt      # Tab: historical game list
├── StatsFragment.kt         # Tab: statistics dashboard
├── NewGameFragment.kt       # Sub-screen: new game setup
├── ScoreHandFragment.kt     # Sub-screen: score entry (NumberPicker)
├── StatsListFragment.kt     # Sub-screen: per-player stats + rankings
├── ui/
│   └── SegmentedControlButton.kt  # Custom RadioButton for Tichu/GT selection
├── model/
│   ├── Game.kt              # Game state + Hand list + scoring logic
│   ├── Hand.kt              # One hand: bids, card points, outcomes
│   └── Player.kt            # Player profile + cumulative stats
├── repository/
│   └── TichuRepository.kt   # All Room I/O (load/save players, games, hands)
└── db/
    ├── TichuDatabase.kt     # Room singleton — version 1, add Migration for schema changes
    ├── GameEntity/Dao
    ├── HandEntity/Dao
    └── PlayerEntity/Dao
app/proguard-rules.pro       # R8 keep rules for release builds
```

## Key Patterns

- **Global state:** `TGApp.getGame()`, `TGApp.getGames()`, `TGApp.getPlayers()`
- **Saving data:** Call `TGApp.saveGames()` / `TGApp.savePlayers()` at each mutation point. NEVER defer to `onPause`.
- **Rule logic** belongs in `model/` classes, not Fragments.
- **DB schema changes:** increment `version` in `@Database` and add `Migration(n, n+1)` in `TichuDatabase`. Do NOT use `fallbackToDestructiveMigration()` for new versions.
- **Fragment data passing:** `Bundle.putSerializable()` via `BundleCompat.getSerializable()` — `Game` and `Hand` implement `Serializable`. Always use Fragment `arguments`, never static setters.
- **Null assertions:** Use `requireNotNull(x) { "message" }` or `checkNotNull(x) { "message" }`. Never use bare `!!`.
- **Colors:** `Color.YELLOW` / `Color.GRAY` — never raw integer literals.
- **Game rule constants** (bonus scores, thresholds): `const val` in the relevant model class `companion object`.

## Repo Etiquette

- Commit directly to `main` — no PR workflow.
- Tag releases as `vN` (e.g. `v2`) and upload the release APK to the GitHub release.

## Known Issues

See `IMPROVEMENTS.md` for the full tracked list.
