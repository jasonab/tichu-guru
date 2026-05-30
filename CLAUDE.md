# Tichu Guru

Android scoring and statistics app for the card game Tichu (4-player partnership trick-taking).

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew clean assembleDebug    # After AndroidManifest.xml or build.gradle changes
./gradlew lintKotlin             # Kotlin linter
./gradlew formatKotlin           # Auto-fix Kotlin formatting
./gradlew test                   # Unit tests (77 total)
```

IMPORTANT: After any `.kt` change, `./gradlew lintKotlin test` must pass before the change is complete. XML/resource changes are exempt. If lint fails, run `formatKotlin` — never fix manually.

## Tech Stack

- **Language:** Kotlin — **Min SDK:** 30, **Target/Compile SDK:** 36
- **AGP:** 9.2.1, **Gradle:** 9.4.1, **Package:** `com.tichuguru`
- **UI:** AppCompat + Material Components + BottomNavigationView
- **Persistence:** Room 2.8.4 (`tichu.db`, version 1) — KSP for code generation
- **State:** `TGApp` singleton (in-memory) + `TGViewModel` LiveData (Fragment UI)

## File Structure

```
app/src/main/kotlin/com/tichuguru/
├── TGActivity.kt            # Single-Activity host (BottomNav, Toolbar)
├── TGApp.kt                 # Application singleton — in-memory state only
├── TGViewModel.kt           # All mutations + LiveData + DB I/O delegation
├── CurHandFragment.kt       # Tab: current hand, Tichu bids
├── ScorecardFragment.kt     # Tab: scorecard for current game
├── AllGamesFragment.kt      # Tab: historical game list
├── StatsFragment.kt         # Tab: statistics dashboard
├── NewGameFragment.kt       # Sub-screen: new game setup
├── ScoreHandFragment.kt     # Sub-screen: score entry (NumberPicker)
├── PlayerStatsFragment.kt   # Sub-screen: per-player stats (rename/clear/delete)
├── RankingFragment.kt       # Sub-screen: player rankings
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
    ├── GameEntity.kt / GameDao.kt
    ├── HandEntity.kt / HandDao.kt
    └── PlayerEntity.kt / PlayerDao.kt
app/src/test/kotlin/com/tichuguru/model/
├── HandTest.kt / GameTest.kt / PlayerTest.kt  # model unit tests
```

## Co-Change Patterns

| Change type | Files that co-change |
| --- | --- |
| Any meaningful change | Modified file + `IMPROVEMENTS.md` (mark item `[x]` or add new `#N`) |
| DB schema change | Entity `.kt` + DAO `.kt` + `TichuDatabase.kt` (new `Migration`) |
| Model field added/renamed | `model/*.kt` + `db/*Entity.kt` + `db/*Dao.kt` + Fragment callers |
| New Fragment | Fragment `.kt` + layout XML + `TGActivity.kt` (navigation) |
| Gradle/AGP upgrade | `settings.gradle.kts` (AGP version lives here, not `build.gradle.kts`) |

## Coding Rules

See @.claude/rules/project.md, @.claude/rules/languages/kotlin.md, and @.claude/rules/frameworks/android-room.md for project-specific coding rules.

## Repo Etiquette

- Commit directly to `main` — no PR workflow.
- Commit messages: plain lowercase imperative, no prefixes (`fix score hand layout`, not `fix: score hand layout`).
- Tag releases as `vN` (e.g. `v2`) and upload the release APK to the GitHub release.

## Known Issues

See @IMPROVEMENTS.md for the full tracked list.
