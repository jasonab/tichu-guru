# Tichu Guru — Improvement Plan

Items are ordered by priority within each section. Completed items are in the archive at the bottom.

---

## Critical

- [ ] **#5 `fallbackToDestructiveMigration()` silently wipes all data on schema change** (`TichuDatabase.java:21`)
  Any future Room schema change will silently delete all user game history with no warning.
  Fix: add explicit Room migrations, or at minimum show a confirmation dialog before
  destruction so users can back up or cancel.

- [ ] **#22 Sub-screens launched as Activities instead of Fragments**
  `NewGameActivity`, `ScoreHandActivity`, and `StatsListActivity` are launched via
  `startActivityForResult` / `startActivity` over the `TGActivity` single-Activity host,
  while the four tab screens are Fragments. This is the root cause of #7 and #23 and
  creates unnatural back-stack behavior (pressing Back exits the app instead of returning
  to the previous tab).
  Fix: convert all three to Fragments; navigate via `FragmentManager` back stack. Eliminates
  `startActivityForResult` entirely and makes #7 and #23 trivial follow-ons.

---

## High

- [x] **#9 Magic number `524288` for window flag** (`TGActivity.java:31`)
  Replaced with `WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS`.

- [ ] **#12 Reflection in `StatsFragment.Getter`** (`StatsFragment.java`)
  `Getter` uses `Player.class.getMethod("get" + valName)` to invoke stat getters by name.
  Any `Player` getter rename silently breaks the ranking screens at runtime with no
  compile-time warning.
  Fix: replace with typed `Function<Player, ?>` lambdas passed directly to `RankExpandListener`.

---

## Medium

- [ ] **#7 `startActivityForResult` deprecated** *(blocked by #22)*
  Used in `CurHandFragment` (`onScoreHand`, `onNewGame`) and `TGActivity`.
  Best resolved by completing #22 (Fragment navigation eliminates all `startActivityForResult`
  call sites). If #22 is deferred, migrate each call site to `ActivityResultLauncher`.

- [ ] **#15 `TichuDatabase.getInstance()` not thread-safe** (`TichuDatabase.java:16`)
  Singleton has no synchronization. Currently harmless because `allowMainThreadQueries()`
  means only one thread ever touches it, but this is a latent race condition that will bite
  if any async DB work is added.
  Fix: add `volatile` + double-checked locking, or use `synchronized`.

- [ ] **#23 `StatsListActivity` renders two unrelated screens** *(blocked by #22)*
  A `playerName` extra switches between a player-detail layout (`statslist`) and a rankings
  layout (`rankinglist`) inside one class. Both code paths are harder to follow and change.
  Fix: split into `PlayerStatsFragment` and `RankingsFragment` as part of #22.

---

## Low / Large Refactors

Do not implement unless explicitly requested.

- [ ] **#16 `allowMainThreadQueries()`** (`TichuDatabase.java:19`)
  All DB I/O blocks the UI thread. Acceptable with small datasets; worth revisiting if the
  app needs to handle large game histories. Requires #15 and ideally #25 to be done first.

- [ ] **#18 Replace `kankan.wheel.widget` with `NumberPicker` or Material `Slider`**
  The embedded scroll-wheel library (`kankan/wheel/widget/`) is unmaintained third-party
  source living under a foreign root package (`kankan.*`) inside `src/main/java`, mixing
  external code with app source and making updates impossible via Gradle.
  Used in `ScoreHandActivity` for score entry and player-out-first selection.
  Fix: replace with standard `NumberPicker` or a Material `Slider`. Removes ~10 files and
  resolves the package pollution. (Supersedes #24.)

- [ ] **#19 Replace `SegmentedControlButton` with `MaterialButtonToggleGroup`**
  `SegmentedControlButton` (`com.tichuguru.ui`) is a custom `RadioButton` subclass used
  for Tichu/Grand Tichu call selection in `curhand.xml`.
  `MaterialButtonToggleGroup` (Material Components) provides the same UX natively with
  theming, accessibility, and state-management support built in.

- [ ] **#25 No repository layer — `TGApp` owns both global state and Room I/O**
  `TGApp` acts as Application class, in-memory state store, and persistence layer.
  This makes it hard to move DB work off the main thread (#16) since there is no
  intermediate layer to put async logic in.
  Fix: introduce a `TichuRepository` in a `repository/` package that owns the Room↔model
  bridge; `TGApp` becomes a thin Application subclass that initialises the repository.

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

- [x] **#6 Persistence relied on `onPause` instead of eager saves at mutation points**
  `TGActivity.onPause()` was the only save point, risking data loss if the process was
  killed before the activity lost focus. Fixed: saves now happen immediately at each
  mutation point (`scoreHand`, `endGame`, `removeHand`, `startGame`, `addPlayer`,
  `clearStats`). Removed `onPause` saves from `TGActivity` and `NewGameActivity`.

- [x] **#8 `getSystemService("layout_inflater")` deprecated**
  Was in `StatsActivity.java`. `StatsActivity` was deleted (tab replaced by `StatsFragment`
  which uses `LayoutInflater.from(context)` correctly). No longer present.

- [x] **#10 `CurHandActivity.clearTichuButtonsNow` is public static mutable**
  `AllGamesFragment` set this flag; `CurHandFragment` polled it in `onResume`.
  Fixed: added `requestClearTichuButtons()` / `getClearTichuButtons()` LiveData event on
  `TGViewModel`. `AllGamesFragment` calls `viewModel.requestClearTichuButtons()`; 
  `CurHandFragment` observes the event in `onViewCreated`.

- [x] **#11 Hardcoded color integers**
  `-256` (yellow) and `-7829368` (gray) replaced with `Color.YELLOW` and `Color.GRAY`
  in `CurHandFragment`, `CurHandActivity`, and `AllGamesFragment`.

- [x] **#13 Dead `Externalizable` code on model classes**
  `Game`, `Hand`, `Player` migrated from `Externalizable` to plain `Serializable`.
  Removed `readExternal`/`writeExternal`/`REVISION` from all three. `serialVersionUID`
  retained on each since `Game` and `Hand` are still passed via `Bundle.putSerializable()`.

- [x] **#14 `CurHandActivity` dead code**
  `TGActivity` hosts `CurHandFragment`; nothing navigated to `CurHandActivity` directly.
  Deleted after #10 removed the last external reference to its static flag.

- [x] **#17 Replace `ListView` with `RecyclerView`**
  `StatsActivity` and `AllGamesActivity` (which used `ListView`) were deleted. All
  remaining list screens use `RecyclerView`. No `ListView` remains.

- [x] **#20 Replace `onPrepareOptionsMenu` with Toolbar + overflow menu**
  Menu items were invisible — `NoActionBar` theme with no Toolbar meant they never showed.
  Fixed: added `Toolbar` to `main.xml`, wired it as support action bar in `TGActivity`,
  migrated `CurHandFragment` to `MenuProvider` with `menu_curhand.xml`.

- [x] **#21 `SegmentedControlButton` orphaned in root package**
  Moved from `com.tichuguru` to `com.tichuguru.ui`; both `curhand.xml` layouts updated.
