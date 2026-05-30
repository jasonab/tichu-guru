# Tichu Guru — Improvement Plan

Open items only appear in the active sections below. All completed work is in the archive at the bottom, ordered by issue number.

---

## High

- [x] **#76 `deleteGame` and `deletePlayer` never revert player stats** (`TGViewModel.kt`, `Game.kt`)
  `deleteGame` removed a game from the list but left every hand's stat contributions baked into
  each player. `deletePlayer` had the same bug for all games it removed. Added `Game.unrecordStats()`
  which calls `unrecordGame` (if `gameOver`) and `unrecordHand` for every hand for all 4 players.
  `deleteGame` now calls it before removal and calls `savePlayers()`. `deletePlayer` calls it per
  removed game so the other 3 players in that game are also corrected. `ignoreStats` games are
  no-ops. Added 2 tests in `GameTest`.

- [x] **#54 `dbScope` in `TGViewModel` is never cancelled — leaks across ViewModel lifecycle** (`TGViewModel.kt:21`)
  Added `override fun onCleared() { dbScope.cancel() }`. Also added `import kotlinx.coroutines.cancel`.

- [x] **#55 `ListAdapter`/DiffUtil unusable with mutable in-place `Game` objects** (`AllGamesFragment.kt`)
  `areContentsTheSame = false` was a workaround: property comparison would always return `true`
  (same object reference in old and new list), so rebinds would never fire. Dropped `ListAdapter`
  entirely; `GamesAdapter` is now a plain `RecyclerView.Adapter` with a `var games` property
  whose setter calls `notifyDataSetChanged()`. Observer sets `adapter.games = games.reversed()`.

- [x] **#42 Duplicated "Are you sure?" AlertDialog pattern**
  Extracted `internal fun Fragment.confirmAction(message: String, onConfirm: () -> Unit)` as a
  package-level extension in `AllGamesFragment.kt` (alongside `winColor`). All 6 occurrences
  replaced: `CurHandFragment.onEndGame`, `ScorecardFragment.onDeleteHand`,
  `AllGamesFragment.onDeleteGame`, `StatsFragment.onClearStats`,
  `PlayerStatsFragment.onClearStats`, and `PlayerStatsFragment.onDeletePlayer`.
  `AlertDialog` import removed from `ScorecardFragment` and `StatsFragment`.

- [x] **#43 `recordHand` / `unrecordHand` are manual mirrors of each other** (`Player.kt`)
  Extracted `private fun adjustHandStats(hand, seat, addOnFailure, sign)` with a `sign`
  multiplier (`+1` or `-1`). `recordHand` increments `numHands` then delegates with `+1`;
  `unrecordHand` guards against zero, decrements `numHands`, then delegates with `-1`.
  All `++`/`--` and `+=`/`-=` inside the former mirror bodies replaced with `+= sign` /
  `+= sign * value`.

---

## Medium

- [x] **#56 Initial DB load goes through `viewModelScope`, saves go through `dbScope`** (`TGViewModel.kt:37-48`)
  The `init {}` block loads data via `viewModelScope.launch(Dispatchers.IO)` while every save
  uses `dbScope` (`Dispatchers.IO.limitedParallelism(1)`). These are independent dispatchers, so
  a save triggered right after startup can race the load, breaking the single-writer guarantee.
  Fix: route the initial load through `dbScope` too.

- [x] **#57 `ScorecardFragment` creates a new adapter on every game change** (`ScorecardFragment.kt:53`)
  `ScorecardAdapter` created once in `onViewCreated` with `var game: Game?`; setter calls
  `notifyDataSetChanged()`. `refreshDisplay()` now sets `adapter.game = game`. `getItemCount`
  and `onBindViewHolder` guard on null game.

- [x] **#58 `StatsFragment` creates a new adapter on every player-list emission** (`StatsFragment.kt:41-43`)
  `StatsAdapter` created once in `onViewCreated` with `var players: List<Player> = emptyList()`;
  setter calls `notifyDataSetChanged()`. Observer now sets `adapter.players = players`.

- [x] **#59 `deleteLastHand` crashes on empty hand list** (`TGViewModel.kt:85`)
  Added `if (game.hands.isEmpty()) return` guard before `removeHand(game.hands.size - 1)`.

- [x] **#60 Scorecard running totals recomputed O(N²)** (`ScorecardFragment.kt:96-101`)
  Added `cumTotals1`/`cumTotals2: IntArray` computed once in the `game` setter. `onBindViewHolder`
  now indexes directly into these arrays instead of re-summing from 0 each bind.

- [ ] **#61 Per-player stat value array built inside `StatsFragment`** (`StatsFragment.kt:161-206`)
  `PlayerExpandListener.onClick` assembles a 19-element `values` array with hardcoded index
  positions interleaved with section-header gaps. Per project rules, this mapping belongs in a
  `Player` method (e.g. `fun statRows(): List<StatRow>`), leaving the Fragment to render only.
  A reordering currently silently corrupts the display.

- [ ] **#62 Ranking computation inside `RankExpandListener` click handler** (`StatsFragment.kt:209-251`)
  Sort, `log10` digit-width calculation, and format-string assembly are non-trivial business
  logic embedded in a `View.OnClickListener` inside the adapter. Move to the model/ViewModel layer.

- [ ] **#63 `AllGamesFragment.onDeleteGame` triggers "create first game" directly** (`AllGamesFragment.kt:122-130`)
  The Fragment checks `getAllGames().value.isNullOrEmpty()` after deletion and calls
  `createFirstGame()`. This orchestration logic belongs in the ViewModel: emit a state/event the
  Activity observes, rather than the list Fragment reaching into the Activity.

- [x] **#64 `deleteOrphanHands` with an empty `keepIds` list generates invalid SQL** (`HandDao.kt:18-22`)
  Split into a public default interface method `deleteOrphanHands` (guards with `isNotEmpty()`)
  and a private `@Query` method `deleteOrphanHandsInternal`. Any caller now gets the guard for free.

- [ ] **#65 Icon-only buttons lack `contentDescription`** (`allgamesrow.xml`, `statsrow.xml`, scorecard delete)
  TalkBack announces nothing meaningful for delete and expand `ImageButton`s. Add
  `android:contentDescription` to each icon-only interactive view.

---

## Lowm

- [ ] **#66 `SegmentedControlButton` Tichu/GT state conveyed only by color** (`ui/SegmentedControlButton.kt`)
  Selected vs. unselected is distinguished purely by teal vs. gray fill — a colorblind concern
  for the critical Tichu/GT bidding selection. Add a non-color state indicator (e.g. border,
  checkmark, or bold text) as a secondary signal.

- [ ] **#67 `NewGameFragment` uses `try/catch(Exception)` for integer parsing** (`NewGameFragment.kt:127-137`)
  Broad `Exception` catch for control flow is non-idiomatic. Replace with:
  `binding.newGameGameLimit.text.toString().toIntOrNull() ?: /* show error */`.

- [ ] **#68 `NewGameFragment.onRandomizeTeams` uses `Math.random()` and manual swap** (`NewGameFragment.kt:106-116`)
  Use `selectedPlayers.shuffle()` (Kotlin stdlib) instead of Java-idiom `Math.random()`.

- [ ] **#69 `StatsAdapter.getItemCount` uses magic constant `players.size + 11`** (`StatsFragment.kt:159`)
  The `11` (2 headers + 9 rank rows) is undocumented and must stay in sync with `statLabels`
  and rank `when` arms. Extract a named constant or derive it from the structures themselves.

- [ ] **#70 `RankExpandListener.onClick` throws `RuntimeException` for unreachable branch** (`StatsFragment.kt:150-152`)
  Replace `throw RuntimeException("Unknown rank index: $num")` with `error("Unknown rank index: $num")`
  (throws `IllegalStateException` — the idiomatic Kotlin form).

- [ ] **#71 User-facing strings hardcoded throughout, not in `strings.xml`**
  Error messages, dialog text ("Are you sure?", "Yes", "No"), button labels ("Rename", "Delete"),
  and stat labels in `StatsFragment` are all inline string literals. Externalize to `strings.xml`
  to enable localization and consistent TalkBack announcements. Also remove the leftover
  template `Hello World, TGActivity!` entry (`strings.xml:3`).

- [x] **#72 Dead drawable files `wheel_bg.xml` and `wheel_val.xml` still present**
  Deleted both files. Removed dangling `android:background="@drawable/wheel_bg"` from the 3
  `NumberPicker` elements in `scorehand.xml`, and removed the two stale `<public>` entries
  from `public.xml`.

---

## Testing

Add in priority order.

- [x] **#31 Unit tests for `db/` entity mappers**
  Added `EntityMapperTest.kt` in `src/test/kotlin/com/tichuguru/db/`. Covers:
  `PlayerEntity.from()`, `PlayerEntity.toPlayer()`, and full round-trip (18 fields each);
  `HandEntity.from()` for card scores, bids, and computed tichu scores; `HandEntity.toHand()`
  for card scores and bid restoration; `HandEntity` round-trip; `GameEntity.from()` for
  scores, flags, player IDs, and date. 13 tests total.

- [ ] **#32 Integration tests for Room DAOs**
  Use `androidx.room:room-testing` with an in-memory database to test upsert, orphan deletion,
  and transaction semantics. Requires `src/androidTest/`.

- [x] **#73 `addOnFailure` stat recording in `PlayerTest`** (`model/Player.kt:67-123`)
  Added 5 tests: own tichu fails with `addOnFailure=true` → no penalty; normal mode fails →
  deducts 100; opponent tichu fails with `addOnFailure=true` → team gains 100 via
  `stoppedTichuScore`; normal mode opponent failure → no gain; `unrecordHand` with
  `addOnFailure=true` reverts cleanly.

- [x] **#74 `removeHand` reverting per-player stats end-to-end** (`model/GameTest.kt`)
  Added 4 tests verifying that `scoreHand` + `removeHand` leaves all players with zero
  `numHands`, `cardPoints`, `totalPoints`; and that removing a game-ending hand also reverts
  `numGames` and `numWins` for all players.

- [x] **#75 Untested derived stat helpers in `Player`**
  Added 12 tests to `PlayerTest`: zero-guard and calculated cases for `getCardPtsPerHand`,
  `getTichuEfficiency`, `getGTPct`, `getHandsPerDW` (including the `numDoubleWins==0 → 1000.0`
  sentinel), `nonCalls`, and `getPartnerTichuPct`. Total tests: 114 (was 77).

---

## Completed

- [x] **#47 Win-color logic duplicated across three fragments**
  Extracted `internal fun winColor(team1wins: Boolean): Int` as a package-level function in
  `AllGamesFragment.kt`. `AllGamesFragment` and `CurHandFragment` now call `winColor(team1wins)`
  and `winColor(!team1wins)` instead of inline `if (team1wins) Color.YELLOW else Color.GRAY`.
  (`ScorecardFragment` uses green/red for a different purpose and was unaffected.)

- [x] **#53 Split `StatsListFragment` into `PlayerStatsFragment` + `RankingFragment`**
  `StatsListFragment` used a nullable `player` field as a mode switch, inflating different
  layouts and conditionally wiring buttons based on null/non-null. Split into two focused
  fragments: `PlayerStatsFragment` (non-nullable `lateinit var player`, `statslist.xml`,
  Rename/Clear/Delete buttons) and `RankingFragment` (`rankinglist.xml`, no player state).
  `StatsFragment` updated to push the correct type. `StatsListFragment.kt` deleted.

- [x] **#52 No way to rename an existing player**
  Added "Rename Player" button to `PlayerStatsFragment` (per-player detail screen).
  Shows an `AlertDialog` with an `EditText` pre-filled with the current name.
  Validates: non-empty, not already taken. `TGViewModel.renamePlayer()` mutates
  `player.name` in place, re-sorts the player list, and saves players, games, and all
  LiveData. Bundle key uses stable `dbId` instead of mutable name.

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

- [x] **#30 Unit tests for `model/` business logic**
  `HandTest`, `GameTest`, and `PlayerTest` cover all edge cases: tichu/grand tichu
  success and failure, addOnFailure mode, double win, mercy rule trigger, score
  boundaries, `removeHand` revert, `unrecordHand` inverse, `clearStats`, and all
  stat helper functions. 77 tests total in `src/test/kotlin/com/tichuguru/model/`.

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

- [x] **#36 `RelativeLayout` root used only to pin a button at the bottom** (`scorecard.xml`, `newgame.xml`, `scorehand.xml`)
  Converted all three roots to vertical `LinearLayout`. Scrollable body gets
  `layout_height="0dp"` + `layout_weight="1"`; button(s) placed after it. Removed all
  `layout_alignParentBottom` and `layout_above` attributes. Also took the opportunity to
  remove the spurious `android:orientation` attribute from `scorecard.xml` (closes #40).

- [x] **#37 Lone `RecyclerView` wrapped in a `LinearLayout`** (`allgames.xml`, `rankinglist.xml`)
  `LinearLayout` wrapper removed; `RecyclerView` is now the root element in both files.

- [x] **#38 `scorecardrow.xml`: six single-child `LinearLayout` wrappers**
  Removed the four single-child wrappers (tichu columns); their `TextView`s now sit directly
  in the root with `layout_width="0dp"` + `layout_weight="1"`. The two score/total columns
  keep their inner `LinearLayout` (needed for the divider) but are also fixed to `0dp` width.
  View count reduced from 16 to 12.

- [x] **#39 `layout_marginLeft/Right` instead of `layout_marginStart/End`** (`allgamesrow.xml`)
  Replaced all 5 occurrences across `allgamesrow.xml`, `statslistrow.xml`, and `statsrow.xml`.

- [x] **#40 `android:orientation` on `RelativeLayout`** (`scorecard.xml`)
  Resolved by #36 — `RelativeLayout` replaced with `LinearLayout`, so the attribute is now meaningful rather than a no-op.

- [x] **#41 `textAppearance` references framework style `textAppearanceMedium`** (`statsrow.xml`)
  Replaced `?android:attr/textAppearanceMedium` with `?attr/textAppearanceBody1`.

- [x] **#44 `Hand.setOutFirst()` repeats the same block for each of 4 players**
  Replaced 16 sequential `if` statements with a loop over `0..3` plus a private
  `applyBidPoints(isTeam1, made, points)` helper covering both scoring modes.

- [x] **#45 `setPendingHand()` / `setPendingGame()` passed via static setters**
  `Game` and `Hand` given `Serializable` interface. `ScoreHandFragment.newInstance()` and
  `NewGameFragment.newInstance()` now pack their argument into `Bundle` via `putSerializable`
  and read it back in `onViewCreated` via `requireArguments()`. `pendingGame` / `pendingHand`
  fields and all four accessors removed from `TGApp`.

- [x] **#46 Magic seat index numbers with no named constant** (`Player.kt`)
  Added private top-level `fun isTeam1(seat: Int) = seat % 2 == 0` in `Player.kt`.
  All 4 occurrences of `seat == 0 || seat == 2` replaced with `isTeam1(seat)`.

- [x] **#48 `GamesAdapter` recreated on every list refresh** (`AllGamesFragment.kt`)
  Adapter created once in `onViewCreated` with a mutable `games` property. The LiveData
  observer now updates `adapter.games` and calls `notifyDataSetChanged()` instead of
  replacing the adapter.

- [x] **#49 Players sorted twice** (`NewGameFragment.kt`)
  Replaced `allPlayers.add()` + `Collections.sort()` + `indexOf()` with a single
  `indexOfFirst { it.name > newPlayer.name }` to find the insertion point, then
  `allPlayers.add(insertIndex, newPlayer)` and `spinAdapter.insert(name, insertIndex)`.
  Removed unused `java.util.Collections` import.

- [x] **#50 `TGViewModel.notify*()` methods are manual sync between two state stores**
  Made `games`, `players`, and `curGame` private. Removed all public `notify*()` and `sync()`
  methods — each mutation now updates LiveData directly. Removed redundant external callers:
  `viewModel.sync()` from `TGActivity.onResume()` and both `viewModel.notifyGameChanged()`
  calls from `CurHandFragment` fragment result listeners. `TGActivity.createFirstGame()`
  now reads state through LiveData instead of the former public fields.

- [x] **#51 Replace bare `!!` with `requireNotNull` / `checkNotNull`**
  Replaced all `!!` usages across 5 files: `NewGameFragment` (2), `ScoreHandFragment` (2),
  `StatsListFragment` (3), `StatsFragment` (1), `CurHandFragment` (12). Fragment bundle args
  use `requireNotNull(x) { "x arg missing" }`; internal-state assertions use `checkNotNull(x)
  { "x not bound" }`. `StatsFragment` `adapter == null || adapter!!.itemCount` simplified to
  `adapter?.itemCount`. `CurHandFragment.onScoreHand` captures local `g1–g4` vals to avoid
  repeated assertions. `onSaveInstanceState` uses `it` (the let receiver) for grp1.
