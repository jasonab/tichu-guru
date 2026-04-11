---
name: tichu-guru-patterns
description: Coding patterns extracted from tichu-guru Android app repository
version: 3.0.0
source: local-git-analysis
analyzed_commits: 50+
---

# Tichu Guru Patterns

## Commit Conventions

This project uses **plain lowercase imperative messages** — no conventional commit prefixes:

```
move to upsert
convert viewmodel to kotlin
rename boolean fields
fix score hand layout
remove activities for fragments
use flag constant
```

Keep messages short. Describe what changed, not why.

## Build Workflow

**Always run after every change:**

```bash
./gradlew assembleDebug          # after any Kotlin/layout change
./gradlew clean assembleDebug    # after AndroidManifest.xml or build.gradle changes
./gradlew test                   # ALWAYS run after any change — all 77 tests must pass
```

The build and tests must pass before any change is considered complete.

## File Co-Change Patterns

These files typically change together — update all of them when touching one:

| Change type | Files that co-change |
|---|---|
| Any meaningful change | The modified file + `IMPROVEMENTS.md` (mark item `[x]` or update status) |
| DB schema change | Entity `.kt` + DAO `.kt` + `TichuDatabase.kt` (new `Migration`) |
| Model field added/renamed | `model/*.kt` + `db/*Entity.kt` + `db/*Dao.kt` + Fragment callers |
| New Fragment | Fragment `.kt` + layout `res/layout/*.xml` + `TGActivity.kt` (navigation) |
| Gradle upgrade | `build.gradle.kts` + `settings.gradle.kts` + `gradle/wrapper/gradle-wrapper.properties` |

## Code Architecture

All source is Kotlin — no Java files remain.

```
app/src/main/kotlin/com/tichuguru/
├── TGActivity.kt                # Single-Activity host (BottomNav, Toolbar)
├── TGApp.kt                     # Application singleton — in-memory state only
├── TGViewModel.kt               # All mutations + LiveData + DB I/O delegation
├── CurHandFragment.kt           # Tab: current hand, Tichu bids
├── ScorecardFragment.kt         # Tab: scorecard for current game
├── AllGamesFragment.kt          # Tab: historical game list
├── StatsFragment.kt             # Tab: statistics dashboard
├── NewGameFragment.kt           # Sub-screen: new game setup
├── ScoreHandFragment.kt         # Sub-screen: score entry (NumberPicker)
├── StatsListFragment.kt         # Sub-screen: per-player stats + rankings
├── ui/
│   └── SegmentedControlButton.kt  # Custom RadioButton for Tichu/GT selection
├── model/
│   ├── Game.kt                  # Game state + Hand list + scoring logic
│   ├── Hand.kt                  # One hand: bids, card points, outcomes
│   └── Player.kt                # Player profile + cumulative stats
├── repository/
│   └── TichuRepository.kt       # All Room I/O (load/save players, games, hands)
└── db/
    ├── TichuDatabase.kt         # Room singleton — companion object, version 1
    ├── GameEntity.kt / GameDao.kt
    ├── HandEntity.kt / HandDao.kt
    └── PlayerEntity.kt / PlayerDao.kt

app/src/test/kotlin/com/tichuguru/model/
├── HandTest.kt                  # 20 tests: scoring, tichu/GT, addOnFailure mode
├── GameTest.kt                  # 14 tests: accumulation, game-end, mercy rule, removeHand
└── PlayerTest.kt                # 43 tests: stat tracking, recordHand/unrecordHand, clearStats
```

## Architecture Rules

- **`TGViewModel` owns all mutations** — Fragments call ViewModel methods (`viewModel.scoreHand()`,
  `viewModel.addGame()`, etc.). The ViewModel updates LiveData directly after each mutation.
  There are no public `notify*()` methods — LiveData is private and set inside each mutation method.
- **`TGApp` is thin** — holds `companion object` with `@JvmStatic` accessors for global state
  (`TGApp.getGame()`, `TGApp.getGames()`, `TGApp.getPlayers()`). DB I/O lives in `TGViewModel`.
- **Save eagerly** — `TGViewModel` calls `saveGames()` / `savePlayers()` (fire-and-forget via
  `dbScope.launch`) at every mutation. Never defer to `onPause`.
- **Rule logic in `model/`** — business logic belongs in `Game`/`Hand`/`Player`, not Fragments.
- **Fragment args via Bundle** — `Game` and `Hand` implement `Serializable`. Pass them via
  `Bundle.putSerializable()` / `BundleCompat.getSerializable()`. Use `Fragment.arguments`, never static setters.

## Kotlin Conventions

- **Null assertions:** `requireNotNull(x) { "message" }` or `checkNotNull(x) { "message" }`. Never bare `!!`.
- **Colors:** `Color.YELLOW` / `Color.GRAY` — never raw integer literals.
- **Game rule constants:** `const val` in the relevant model class `companion object`.
- **Boolean property names:** use `gameOver`, not `isGameOver` — avoids getter naming clash.
- **`@JvmStatic` in companion objects** for accessors called from any interop context:
  ```kotlin
  companion object {
      @JvmStatic fun getGame(): Game? = instance.curGame
      @JvmField val SOME_CONST = 42
  }
  ```

## Room DB Conventions

- Use `@Upsert` instead of `@Insert(onConflict = REPLACE)`.
- Entity `from()` includes `id = model.dbId` so upserts are idempotent.
- `toModel()` sets `model.dbId = id` to thread DB identity back to the domain object.
- For child collections (hands within a game), use `deleteOrphanHands` after upsert:
  ```sql
  DELETE FROM hands WHERE gameId = :gameId AND id NOT IN (:keepIds)
  ```
- Schema changes: increment `version` in `@Database` and add `Migration(n, n+1)`.
  Do **not** use `fallbackToDestructiveMigration()` for new versions.
- All DB I/O is async via `CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))`.

## IMPROVEMENTS.md Discipline

- Every meaningful change marks at least one item `[x]` in `IMPROVEMENTS.md`.
- New work items get the next `#N` number with a concise description and file references.
- Sections: **High → Medium → Testing → Low → Completed**.
- Once done, update the description to describe what was actually done.

## Testing

JUnit unit tests exist for all `model/` business logic (77 tests total, all passing):

| File | Coverage |
|---|---|
| `HandTest.kt` | `otherCardScore`, `cardScoreIndex`, card score totals, tichu/GT success/failure, addOnFailure mode, multiple bids |
| `GameTest.kt` | Score accumulation, game-end at limit, tied scores, mercy rule (incl. negative scores from GT failures), double win, `removeHand`, `ignoreStats` |
| `PlayerTest.kt` | Card points per seat/team, double win, tichu/GT called+made/failed, efficiency hands, opp/partner tichu tracking, `unrecordHand` inverse, `clearStats`, stat helpers |

Tracked remaining test items in `IMPROVEMENTS.md`:

- **#31** — Unit tests for `db/` entity round-trip mappers (`src/test/`)
- **#32** — Integration tests for Room DAOs (`room-testing`, in-memory DB, `src/androidTest/`)
