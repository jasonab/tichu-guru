---
name: tichu-guru-patterns
description: Coding patterns extracted from tichu-guru Android app repository
version: 4.0.0
source: local-git-analysis
analyzed_commits: 200
---

# Tichu Guru Patterns

## Commit Conventions

This project uses **plain lowercase imperative messages** — no conventional commit prefixes:

```
fix notifyAll
remove so many literal color references
split up stats fragment
add player rename
use better for loops
create isTeamOne method
refactor hand to remove redundancies
```

Meta commits (CLAUDE.md, IMPROVEMENTS.md only) may capitalize: `Update IMPROVEMENTS.md`.
Keep messages short. Describe what changed, not why.

## Build Workflow

**Always run after every `.kt` file change** (also enforced by the PostToolUse hook):

```bash
./gradlew lintKotlin   # If this fails, run formatKotlin — never fix by hand
./gradlew test         # All 77 tests must pass
```

```bash
./gradlew assembleDebug          # After any Kotlin/layout change
./gradlew clean assembleDebug    # After AndroidManifest.xml or build.gradle changes
./gradlew assembleRelease        # Release build — see /release skill
```

## File Co-Change Patterns

Derived from 200 commits. Files that almost always change together:

| Change type | Files that co-change |
|---|---|
| Any meaningful change | Modified file + `IMPROVEMENTS.md` (mark item `[x]` or add new `#N`) |
| DB schema change | Entity `.kt` + DAO `.kt` + `TichuDatabase.kt` (new `Migration`) |
| Model field added/renamed | `model/*.kt` + `db/*Entity.kt` + `db/*Dao.kt` + Fragment callers |
| `Hand.kt` change | Almost always + `HandTest.kt`; often + `HandEntity.kt` |
| New Fragment | Fragment `.kt` + layout XML + `TGActivity.kt` (navigation) |
| Gradle upgrade | `settings.gradle.kts` (AGP version lives here, not `build.gradle.kts`) |

**Hottest files** (changed most frequently in recent history):
- `model/Hand.kt` — most volatile model; scoring logic evolves often
- `CurHandFragment.kt` — UI counterpart to Hand changes
- `IMPROVEMENTS.md` — changes with every commit

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
├── StatsFragment.kt             # Tab: statistics dashboard (pushes sub-screens)
├── NewGameFragment.kt           # Sub-screen: new game setup
├── ScoreHandFragment.kt         # Sub-screen: score entry (NumberPicker)
├── PlayerStatsFragment.kt       # Sub-screen: per-player stats (rename/clear/delete)
├── RankingFragment.kt           # Sub-screen: player rankings
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

Note: `StatsListFragment` was split into `PlayerStatsFragment` + `RankingFragment` (#53). Do not recreate `StatsListFragment`.

## Architecture Rules

- **`TGViewModel` owns all mutations** — Fragments call ViewModel methods. LiveData is private and set inside each mutation method. No public `notify*()` methods.
- **`TGApp` is thin** — holds `companion object` with `@JvmStatic` accessors for global state. DB I/O lives in `TGViewModel`.
- **Save eagerly** — `TGViewModel` calls `saveGames()` / `savePlayers()` (fire-and-forget via `dbScope.launch`) at every mutation. Never defer to `onPause`.
- **Rule logic in `model/`** — business logic belongs in `Game`/`Hand`/`Player`, not Fragments.
- **Fragment args via Bundle** — `Game` and `Hand` implement `Serializable`. Pass via `Bundle.putSerializable()` / `BundleCompat.getSerializable()`. Use `Fragment.arguments`, never static setters.

## Kotlin Conventions

- **Null assertions:** `requireNotNull(x) { "message" }` or `checkNotNull(x) { "message" }`. Never bare `!!`.
- **Colors:** `Color.YELLOW` / `Color.GRAY` — never raw integer literals. Use `@color/` for layout-defined colors.
- **Game rule constants:** `const val` in the relevant model class `companion object`.
- **Boolean property names:** use `gameOver`, not `isGameOver` — avoids getter naming clash.
- **Seat helpers:** use `isTeam1(seat: Int) = seat % 2 == 0` instead of repeating `seat == 0 || seat == 2`. Defined as a private top-level fun in `Player.kt`.
- **Loop over seats:** when the same logic applies to all 4 players, loop `0..3` with a helper rather than 4 repeated `if` blocks.

## Room DB Conventions

- Use `@Upsert` instead of `@Insert(onConflict = REPLACE)`.
- `Entity.from(model)` includes `id = model.dbId` so upserts are idempotent.
- `toModel()` sets `model.dbId = id` to thread DB identity back to domain.
- After upserting child hands: call `deleteOrphanHands(gameId, keepIds)`.
- Schema changes: increment `version` + add `Migration(n, n+1)`. Never `fallbackToDestructiveMigration()` for new versions.
- All DB I/O is async via `CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))`.
- **DB migration guard hook fires automatically** when editing `*Entity.kt`, `*Dao.kt`, or `TichuDatabase.kt`. Heed the warning.

## Testing

JUnit unit tests exist for all `model/` business logic (77 tests, all passing):

| File | What's covered |
|---|---|
| `HandTest.kt` | Card scoring, tichu/GT success/failure, addOnFailure mode, 125/-25 scoring |
| `GameTest.kt` | Score accumulation, game-end at limit, tied scores, mercy rule, double win, `removeHand` |
| `PlayerTest.kt` | Stat tracking per seat/team, `recordHand`/`unrecordHand` inverse, `clearStats`, stat helpers |

Pending (add in this order per IMPROVEMENTS.md):
- **#31** — Unit tests for `db/` entity round-trip mappers
- **#32** — Room DAO integration tests (`room-testing`, in-memory DB, `src/androidTest/`)

## IMPROVEMENTS.md Discipline

- Every meaningful change marks at least one item `[x]` in `IMPROVEMENTS.md`.
- New items get the next `#N` number with concise description and file references.
- Sections: **High → Testing → Completed**. Move completed items to Completed with a summary of what was done.

## Available Skills

- `/release` — bump versionCode/Name, `assembleRelease`, tag, `gh release create` with APK
- `/adb-install` — `assembleDebug` + `adb install -r` to connected device
- `/tichu-guru-patterns` — this file (project patterns reference)
