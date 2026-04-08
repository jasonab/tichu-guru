# Tichu Guru — Improvement Plan

Open items only appear in the active sections below. All completed work is in the archive at the bottom, ordered by issue number.

---

## High

- [x] **#33 Weighted children use `match_parent` width instead of `0dp`** (`newgame.xml`, `scorehand.xml`)
  Changed all 9 weighted children (4 Spinners, 1 TextView, 1 EditText in `newgame.xml`;
  3 column `LinearLayout`s in `scorehand.xml`) from `layout_width="match_parent"` to
  `layout_width="0dp"` so weight distribution is correct on all screen sizes.

- [x] **#34 Hardcoded color literal in `scorecardrow.xml`**
  Created `res/values/colors.xml` with `scorecard_divider` (`#ff909090`). Both divider
  `View`s in `scorecardrow.xml` updated to `@color/scorecard_divider`.

- [x] **#35 Text sizes use `dp` instead of `sp`** (`newgame.xml`, `statslistrow.xml`, `scorehand.xml`)
  Fixed 7 occurrences: 3× `18dp` → `18sp` in `newgame.xml`, 2× `18dp` → `18sp` in
  `statslistrow.xml`, 2× `24dp` → `24sp` in `scorehand.xml`.

- [ ] **#42 Duplicated "Are you sure?" AlertDialog pattern**
  The same `AlertDialog.Builder` block (message, Yes/No buttons, dismiss on No) appears in
  `CurHandFragment`, `ScorecardFragment`, `AllGamesFragment`, `StatsListFragment`, and
  `StatsFragment` — 6+ times. Extract to a single helper function, e.g.
  `fun Fragment.confirmAction(message: String, onConfirm: () -> Unit)`.

- [ ] **#43 `recordHand` / `unrecordHand` are manual mirrors of each other** (`Player.kt`)
  Both methods contain ~50 lines of nearly identical conditionals (seat → team check, tichu
  scoring, grand tichu scoring). If one is updated the other must be updated identically.
  Extract a private helper that takes a sign multiplier (`+1` or `-1`) and apply it to both.

- [ ] **#44 `Hand.setOutFirst()` repeats the same block for each of 4 players**
  Sixteen sequential `if` statements handle players 0–3 with identical structure. Replace
  with a loop over `0..3` indexing into the `tichu` and `grandTichu` arrays.

- [ ] **#45 `setPendingHand()` / `setPendingGame()` passed via static setters**
  `ScoreHandFragment` and `NewGameFragment` store their input via `TGApp.setPending*()`
  instead of Fragment `arguments`. On process death this state is lost and cannot be
  restored. Use `newInstance(hand: Hand)` / `newInstance(game: Game)` that pack data into
  `arguments` as `Serializable`.

---

## Medium

- [ ] **#36 `RelativeLayout` root used only to pin a button at the bottom** (`scorecard.xml`, `newgame.xml`, `scorehand.xml`)
  All three use `RelativeLayout` solely for `layout_alignParentBottom` on one button while a
  `LinearLayout`/`ScrollView` fills the rest. A single vertical `LinearLayout` with
  `layout_weight="1"` on the scrollable body achieves the same result with one fewer view level.

- [ ] **#37 Lone `RecyclerView` wrapped in a `LinearLayout`** (`allgames.xml`, `rankinglist.xml`)
  Both files wrap a single `RecyclerView` in a `LinearLayout` that contributes nothing.
  The `RecyclerView` can be the root element directly.

- [ ] **#38 `scorecardrow.xml`: six single-child `LinearLayout` wrappers**
  Each column is a `LinearLayout` containing exactly one `TextView`, existing only to carry
  `layout_weight`. Replace with a single horizontal `LinearLayout` holding six `TextView`s
  each using `layout_width="0dp"` + `layout_weight="1"`, halving the view count.

- [ ] **#46 Magic seat index numbers with no named constant** (`Player.kt`)
  The condition `seat == 0 || seat == 2` (team 1) appears 12+ times with no explanation.
  Extract to `fun isTeam1(seat: Int) = seat % 2 == 0` or a `Seat` enum so the team mapping
  is defined once and the intent is clear at every call site.

- [ ] **#47 Win-color logic duplicated across three fragments**
  `if (team1wins) Color.YELLOW else Color.GRAY` appears in `CurHandFragment`,
  `AllGamesFragment`, and `ScorecardFragment`. Extract to a single extension or top-level
  function, e.g. `fun winColor(team1wins: Boolean): Int`.

- [ ] **#48 `GamesAdapter` recreated on every list refresh** (`AllGamesFragment.kt`)
  `refreshList()` reassigns `gamesList.adapter = GamesAdapter()` on every call, discarding
  the previous adapter and its view pool. Create the adapter once in `onViewCreated` and
  call `notifyDataSetChanged()` (or switch to `ListAdapter` with `DiffUtil`) on refresh.

---

## Testing

No tests currently exist in this project. Add in priority order.

- [ ] **#30 Unit tests for `model/` business logic**
  `Game` score calculation, `Hand` bid/outcome logic, and `Player` stat accumulation
  (`recordHand`/`unrecordHand`) are pure logic with no Android dependencies — ideal for
  plain JUnit tests in `src/test/`. Focus on edge cases: double win, mercy rule trigger,
  tichu success/failure, grand tichu, score boundaries.

- [ ] **#31 Unit tests for `db/` entity mappers**
  `GameEntity.from()`/`toGame()`, `HandEntity.from()`/`toHand()`, `PlayerEntity.from()`/`toPlayer()`
  are pure data transforms. Test round-trip fidelity and null/default handling.

- [ ] **#32 Integration tests for Room DAOs**
  Use `androidx.room:room-testing` with an in-memory database to test upsert, orphan deletion,
  and transaction semantics. Requires `src/androidTest/`.

---

## Low

- [ ] **#49 Players sorted twice** (`NewGameFragment.kt`)
  `PlayerDao` already returns players `ORDER BY name`. `NewGameFragment` then calls
  `Collections.sort(allPlayers)` on the same list. The sort in the fragment is redundant
  and should be removed.

- [ ] **#50 `TGViewModel.notify*()` methods are manual sync between two state stores**
  `notifyGameChanged()`, `notifyGamesChanged()`, etc. exist only because `TGApp` and
  `TGViewModel` hold duplicated copies of the same data. Each fragment must remember to call
  the right notify method after every mutation or observers see stale data. Long-term fix:
  LiveData in `TGViewModel` becomes the single source of truth and `TGApp` stops holding
  its own copies.

- [ ] **#39 `layout_marginLeft/Right` instead of `layout_marginStart/End`** (`allgamesrow.xml`)
  RTL-unaware margin attributes. Replace all `marginLeft`/`marginRight` with
  `marginStart`/`marginEnd` throughout layout files.

- [ ] **#40 `android:orientation` on `RelativeLayout`** (`scorecard.xml`)
  `RelativeLayout` ignores the `orientation` attribute entirely — it's a no-op. Remove it.

- [ ] **#41 `textAppearance` references framework style `textAppearanceMedium`** (`statsrow.xml`)
  `?android:attr/textAppearanceMedium` is a pre-Material framework style. Replace with a
  Material text appearance such as `?attr/textAppearanceBody1` for consistency with the app theme.

---

## Completed

- [x] **#1 `Player.equals()` crashes on non-Player input** (`Player.java:363`)
  Unchecked cast with no null/type guard. Fixed: added `instanceof` check.

- [x] **#2 `unrecordHand` corrupts tichu efficiency stats** (`Player.java:151-155`)
  `recordHand` adds ±100 per individual player tichu. `unrecordHand` was subtracting
  `hand.getTichuScore1()`/`getTichuScore2()` — the whole team's tichu sum — so removing
  a hand when both team-1 players called tichu would subtract 200 instead of 100.
  Fixed: now subtracts `(seat == hand.outFirst() ? 100 : -100)`, matching `recordHand`.

- [x] **#3 `TGApp.onCreate()` calls `super.onCreate()` last** (`TGApp.java:30`)
  `super.onCreate()` must be first. Fixed.

- [x] **#4 CSV export removed**
  Broken on Android 11+ (`Environment.getExternalStorageDirectory()` revoked on API 30+).
  Removed entirely: `exportCsv()` from `CurHandActivity`/`CurHandFragment`, `saveCSV()`
  and `CSV_FILE` from `TGApp`, `getCSVHeader()`/`toCSVString()` from `Player`.

- [x] **#5 `fallbackToDestructiveMigration()` silently wipes all data on schema change** (`TichuDatabase.java:21`)
  Reset DB version to 1 (clean baseline). `fallbackToDestructiveMigration()` retained only
  to handle the one-time transition from pre-v1 installs (old versions 3/4); remove it once
  all installs are on v1+. All future version increments must include an explicit
  `Migration(n, n+1)` — no more silent data loss on schema change.

- [x] **#6 Persistence relied on `onPause` instead of eager saves at mutation points**
  `TGActivity.onPause()` was the only save point, risking data loss if the process was
  killed before the activity lost focus. Fixed: saves now happen immediately at each
  mutation point (`scoreHand`, `endGame`, `removeHand`, `startGame`, `addPlayer`,
  `clearStats`). Removed `onPause` saves from `TGActivity` and `NewGameActivity`.

- [x] **#7 `startActivityForResult` deprecated**
  Eliminated entirely by #22. No `startActivityForResult` call sites remain.

- [x] **#8 `getSystemService("layout_inflater")` deprecated**
  Was in `StatsActivity.java`. `StatsActivity` was deleted (tab replaced by `StatsFragment`
  which uses `LayoutInflater.from(context)` correctly). No longer present.

- [x] **#9 Magic number `524288` for window flag** (`TGActivity.java:31`)
  Replaced with `WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS`.

- [x] **#10 `CurHandActivity.clearTichuButtonsNow` is public static mutable**
  `AllGamesFragment` set this flag; `CurHandFragment` polled it in `onResume`.
  Fixed: added `requestClearTichuButtons()` / `getClearTichuButtons()` LiveData event on
  `TGViewModel`. `AllGamesFragment` calls `viewModel.requestClearTichuButtons()`;
  `CurHandFragment` observes the event in `onViewCreated`.

- [x] **#11 Hardcoded color integers**
  `-256` (yellow) and `-7829368` (gray) replaced with `Color.YELLOW` and `Color.GRAY`
  in `CurHandFragment`, `CurHandActivity`, and `AllGamesFragment`.

- [x] **#12 Reflection in `StatsFragment.Getter`** (`StatsFragment.java`)
  Replaced `Getter` (reflection via `Player.class.getMethod("get" + valName)`) with typed
  `ToDoubleFunction<Player>` / `ToIntFunction<Player>` lambdas. `RankExpandListener` now
  takes method references directly (`Player::getWinPct`, `Player::getNumWins`, etc.).
  Renames to `Player` getters are now caught at compile time.

- [x] **#13 Dead `Externalizable` code on model classes**
  `Game`, `Hand`, `Player` migrated from `Externalizable` to plain `Serializable`.
  Removed `readExternal`/`writeExternal`/`REVISION` from all three. `serialVersionUID`
  retained on each since `Game` and `Hand` are still passed via `Bundle.putSerializable()`.

- [x] **#14 `CurHandActivity` dead code**
  `TGActivity` hosts `CurHandFragment`; nothing navigated to `CurHandActivity` directly.
  Deleted after #10 removed the last external reference to its static flag.

- [x] **#15 `TichuDatabase.getInstance()` not thread-safe** (`TichuDatabase.java:16`)
  Resolved as part of migrating `TichuDatabase` to Kotlin. Kotlin companion object uses
  `@Volatile` + `synchronized(this)` double-checked locking. Java file deleted.

- [x] **#16 `allowMainThreadQueries()`** (`TichuDatabase.java:19`)
  Removed `allowMainThreadQueries()` from `TichuDatabase`. `TGApp` now uses a serialized
  `CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))` for all writes
  (`savePlayers`, `saveGames`, `deleteGame` fire-and-forget via `dbScope.launch`). Startup
  loads use `runBlocking { withContext(Dispatchers.IO) { } }` to keep `onCreate` blocking
  until data is ready without touching the main-thread DB query path.

- [x] **#17 Replace `ListView` with `RecyclerView`**
  `StatsActivity` and `AllGamesActivity` (which used `ListView`) were deleted. All
  remaining list screens use `RecyclerView`. No `ListView` remains.

- [x] **#18 Replace `kankan.wheel.widget` with `NumberPicker`**
  Replaced all three `WheelView` instances in `ScoreHandFragment` with `android.widget.NumberPicker`.
  API mapping: `setViewAdapter` → `setMinValue/setMaxValue/setDisplayedValues`; `addChangingListener` →
  `setOnValueChangedListener`; `getCurrentItem/setCurrentItem` → `getValue/setValue`.
  Score pickers use `setWrapSelectorWheel(false)`; player picker uses `true`.
  Deleted all 15 kankan source files and 2 drawables (`wheel_bg.xml`, `wheel_val.xml`).

- [x] **#19 Migrate `SegmentedControlButton` from Java to Kotlin**
  `SegmentedControlButton.java` deleted and rewritten as
  `app/src/main/kotlin/com/tichuguru/ui/SegmentedControlButton.kt`.
  Remains an `AppCompatRadioButton` subclass with custom `onDraw` (gradient fill +
  centered text + black border rect). `curhand.xml` updated: RadioGroups use
  `layout_width="wrap_content"` / `layout_height="48dp"`; each button is
  `layout_width="48dp"` / `layout_height="match_parent"` so the control sizes correctly
  under `Theme.MaterialComponents`.

- [x] **#20 Replace `onPrepareOptionsMenu` with Toolbar + overflow menu**
  Menu items were invisible — `NoActionBar` theme with no Toolbar meant they never showed.
  Fixed: added `Toolbar` to `main.xml`, wired it as support action bar in `TGActivity`,
  migrated `CurHandFragment` to `MenuProvider` with `menu_curhand.xml`.

- [x] **#21 `SegmentedControlButton` orphaned in root package**
  Moved from `com.tichuguru` to `com.tichuguru.ui`; both `curhand.xml` layouts updated.

- [x] **#22 Sub-screens launched as Activities instead of Fragments**
  `NewGameActivity`, `ScoreHandActivity`, and `StatsListActivity` converted to
  `NewGameFragment`, `ScoreHandFragment`, `StatsListFragment`. `TGActivity.pushFragment()`
  adds them over the active tab with `addToBackStack`; a back-stack change listener hides
  the BottomNav and shows a toolbar up-arrow while a sub-screen is active.
  Results communicated via `FragmentResultListener` ("score_hand", "new_game") instead of
  `startActivityForResult`. All three Activity files and manifest entries deleted.

- [x] **#23 `StatsListActivity` renders two unrelated screens**
  `StatsListFragment` preserves the same dual-layout pattern for now (statslist vs
  rankinglist). Splitting into two separate Fragments is deferred — the boundary is clear
  and the class is small enough that it's not urgent.

- [x] **#25 No repository layer — `TGApp` owns both global state and Room I/O**
  Introduced `TichuRepository` in `repository/` package. Owns all DB I/O:
  `loadPlayers`, `savePlayers`, `loadGames`, `saveGames`, `deleteGame`.
  `TGApp` is now a thin Application subclass: initializes the repository in `onCreate()`,
  holds in-memory state (`curGame`, `games`, `players`, `pendingGame`, `pendingHand`),
  and exposes `@JvmStatic` accessors + delegates `savePlayers`/`saveGames`/`deleteGame`
  to the repository. Unblocks async DB work (#16).

- [x] **#26 Convert `db/` package to Kotlin**
  All 7 files converted. Entities are Kotlin `data class` with `var` + defaults (Room
  no-arg constructor). `from()` methods are `companion object` functions annotated `@JvmStatic`;
  entity fields annotated `@JvmField` for direct-access interop with `TGApp.java`. DAOs are
  Kotlin interfaces. `TichuDatabase` uses a `companion object` singleton.
  Files: `PlayerEntity`, `HandEntity`, `GameEntity`, `PlayerDao`, `HandDao`, `GameDao`, `TichuDatabase`.

- [x] **#27 Convert `model/` package to Kotlin**
  All 3 files. `Player` is 393 lines of Java getters/setters — Kotlin properties eliminate
  most of that boilerplate. `Player` implements `Comparable<Player>`; becomes
  `operator fun compareTo`. `Game` and `Hand` are passed as `Serializable` via `Bundle` —
  retain `serialVersionUID` in the companion object.
  Files: `Player`, `Hand`, `Game`.

- [x] **#28 Convert `TGViewModel` and `TGApp` to Kotlin**
  `TGViewModel` is idiomatic Kotlin — LiveData, companion object for tag constants.
  `TGApp` uses companion object for `@JvmStatic` accessors; all state is instance fields
  backed by `lateinit var db` and `companion object { lateinit var instance }`.
  `saveGames` uses `deleteOrphanHands` (instead of delete-all + re-insert) now that `Hand`
  carries a `dbId` field for DB identity.
  Files: `TGViewModel`, `TGApp`.

- [x] **#29 Convert Fragments and `TGActivity` to Kotlin**
  All 8 files converted. Lambda syntax replaces anonymous inner classes (`OnClickListener`,
  `OnItemSelectedListener`, `RankExpandListener` etc.). Kotlin null safety enforced with `!!`
  at TGApp accessor call sites. `inner class` used where outer capture is needed (e.g.
  `GamesAdapter` → `viewModel`). `ToDoubleFunction<Player>` / `ToIntFunction<Player>` replaced
  with Kotlin function types `(Player) -> Double` / `(Player) -> Int`.
  `Hand.CARD_SCORE_OPTIONS` changed from `Array<Int>` to `IntArray` (literal `intArrayOf`) to
  avoid a D8 stack-map-table bug in AGP 9.1.0 with Kotlin-compiled lambda array initializers.
  Files: `TGActivity`, `CurHandFragment`, `ScorecardFragment`, `AllGamesFragment`,
  `StatsFragment`, `NewGameFragment`, `ScoreHandFragment`, `StatsListFragment`.
