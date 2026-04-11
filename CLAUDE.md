# Tichu Guru

Android scoring and statistics app for the card game Tichu (4-player partnership trick-taking).

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew clean assembleDebug    # After AndroidManifest.xml or build.gradle changes
./gradlew lintKotlin             # Kotlin linter
./gradlew test                   # Unit tests (77 total)
```

IMPORTANT: After every code change, run `./gradlew lintKotlin` and `./gradlew test`. Both must pass before a change is complete.

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
├── TGApp.kt                 # Application singleton — in-memory state only
├── TGViewModel.kt           # All mutations + LiveData + DB I/O delegation
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
app/src/test/kotlin/com/tichuguru/model/
    HandTest.kt / GameTest.kt / PlayerTest.kt   # model unit tests
app/proguard-rules.pro                          # R8 keep rules for release builds
```

## Architecture Rules

- **`TGViewModel` owns all mutations** — Fragments call ViewModel methods. LiveData is private and set inside each mutation. No public `notify*()` methods.
- **`TGApp` is thin** — `companion object` with `@JvmStatic` accessors for global state (`TGApp.getGame()`, `TGApp.getGames()`, `TGApp.getPlayers()`). DB I/O lives in `TGViewModel`.
- **Save eagerly** — `TGViewModel` calls `saveGames()` / `savePlayers()` (fire-and-forget via `dbScope.launch`) at every mutation. NEVER defer to `onPause`.
- **Rule logic in `model/`** — business logic belongs in `Game`/`Hand`/`Player`, not Fragments.
- **Fragment args via Bundle** — `Game` and `Hand` implement `Serializable`. Use `Bundle.putSerializable()` / `BundleCompat.getSerializable()` and `Fragment.arguments`. Never static setters.

## Kotlin Conventions

- **Null assertions:** `requireNotNull(x) { "message" }` or `checkNotNull(x) { "message" }`. Never bare `!!`.
- **Colors:** `Color.YELLOW` / `Color.GRAY` — never raw integer literals.
- **Constants:** `const val` in the relevant model class `companion object`.
- **Boolean names:** `gameOver`, not `isGameOver` — avoids getter naming clash.
- **Backing properties:** expose as `val foo: T get() = _foo`, never as `fun foo(): T`. The linter requires a matching property, not a function.

## Room DB Conventions

- Use `@Upsert` instead of `@Insert(onConflict = REPLACE)`.
- `Entity.from(model)` includes `id = model.dbId`; `entity.toModel()` sets `model.dbId = id`.
- Schema changes: increment `version` in `@Database` and add `Migration(n, n+1)` in `TichuDatabase`. Do NOT use `fallbackToDestructiveMigration()`.

## Co-Change Patterns

| Change type | Files that co-change |
|---|---|
| Any meaningful change | Modified file + `IMPROVEMENTS.md` (mark item `[x]` or add new `#N`) |
| DB schema change | Entity `.kt` + DAO `.kt` + `TichuDatabase.kt` (new `Migration`) |
| Model field added/renamed | `model/*.kt` + `db/*Entity.kt` + `db/*Dao.kt` + Fragment callers |
| New Fragment | Fragment `.kt` + layout XML + `TGActivity.kt` (navigation) |

## Repo Etiquette

- Commit directly to `main` — no PR workflow.
- Commit messages: plain lowercase imperative, no prefixes (`fix score hand layout`, not `fix: score hand layout`).
- Tag releases as `vN` (e.g. `v2`) and upload the release APK to the GitHub release.

## Known Issues

See @IMPROVEMENTS.md for the full tracked list.
