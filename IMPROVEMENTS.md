# Tichu Guru — Improvement Plan

Items are ordered by priority within each section. Completed items are in the archive at the bottom.

---

## Critical

- [x] **#5 `fallbackToDestructiveMigration()` silently wipes all data on schema change** (`TichuDatabase.java:21`)
  Reset DB version to 1 (clean baseline). `fallbackToDestructiveMigration()` retained only
  to handle the one-time transition from pre-v1 installs (old versions 3/4); remove it once
  all installs are on v1+. All future version increments must include an explicit
  `Migration(n, n+1)` — no more silent data loss on schema change.

- [x] **#22 Sub-screens launched as Activities instead of Fragments**
  `NewGameActivity`, `ScoreHandActivity`, and `StatsListActivity` converted to
  `NewGameFragment`, `ScoreHandFragment`, `StatsListFragment`. `TGActivity.pushFragment()`
  adds them over the active tab with `addToBackStack`; a back-stack change listener hides
  the BottomNav and shows a toolbar up-arrow while a sub-screen is active.
  Results communicated via `FragmentResultListener` ("score_hand", "new_game") instead of
  `startActivityForResult`. All three Activity files and manifest entries deleted.

---

## High

- [x] **#9 Magic number `524288` for window flag** (`TGActivity.java:31`)
  Replaced with `WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS`.

- [x] **#12 Reflection in `StatsFragment.Getter`** (`StatsFragment.java`)
  Replaced `Getter` (reflection via `Player.class.getMethod("get" + valName)`) with typed
  `ToDoubleFunction<Player>` / `ToIntFunction<Player>` lambdas. `RankExpandListener` now
  takes method references directly (`Player::getWinPct`, `Player::getNumWins`, etc.).
  Renames to `Player` getters are now caught at compile time.

---

## Medium

- [x] **#7 `startActivityForResult` deprecated**
  Eliminated entirely by #22. No `startActivityForResult` call sites remain.

- [ ] **#15 `TichuDatabase.getInstance()` not thread-safe** (`TichuDatabase.java:16`)
  Singleton has no synchronization. Currently harmless because `allowMainThreadQueries()`
  means only one thread ever touches it, but this is a latent race condition that will bite
  if any async DB work is added.
  Fix: add `volatile` + double-checked locking, or use `synchronized`.

- [x] **#23 `StatsListActivity` renders two unrelated screens**
  `StatsListFragment` preserves the same dual-layout pattern for now (statslist vs
  rankinglist). Splitting into two separate Fragments is deferred — the boundary is clear
  and the class is small enough that it's not urgent.

---

## Kotlin Migration

Convert in order — each tier is independently deployable due to Java/Kotlin interop.
Do not implement unless explicitly requested.

- [x] **#26 Convert `db/` package to Kotlin (Tier 1 — easy wins)**
  All 7 files converted. Entities are Kotlin `data class` with `var` + defaults (Room
  no-arg constructor). `from()` methods are `companion object` functions annotated `@JvmStatic`;
  entity fields annotated `@JvmField` for direct-access interop with `TGApp.java`. DAOs are
  Kotlin interfaces. `TichuDatabase` uses a `companion object` singleton. AGP 9.x has
  built-in Kotlin support (no `kotlin-android` plugin needed); `annotationProcessor` retained
  for Room's compiler.
  Files: `PlayerEntity`, `HandEntity`, `GameEntity`, `PlayerDao`, `HandDao`, `GameDao`,
  `TichuDatabase`.

- [ ] **#27 Convert `model/` package to Kotlin (Tier 2 — highest payoff)**
  All 3 files. `Player` is 393 lines of Java getters/setters — Kotlin properties eliminate
  most of that boilerplate. `Player` implements `Comparable<Player>`; becomes
  `operator fun compareTo`. `Game` and `Hand` are passed as `Serializable` via `Bundle` —
  retain `serialVersionUID` in the companion object.
  Files: `Player`, `Hand`, `Game`.

- [ ] **#28 Convert `TGViewModel` and `TGApp` to Kotlin (Tier 3)**
  `TGViewModel` is idiomatic Kotlin — LiveData, companion object for tag constants.
  `TGApp` uses companion object for static accessors; instance fields become `lateinit var`.
  Files: `TGViewModel`, `TGApp`.

- [ ] **#29 Convert Fragments and `TGActivity` to Kotlin (Tier 4 — do last)**
  8 files. Highest risk due to lifecycle complexity; convert after lower layers are stable.
  Lambda syntax and null safety clean up Fragment boilerplate significantly.
  Files: all `*Fragment.java` classes + `TGActivity`.

---

## Low / Large Refactors

Do not implement unless explicitly requested.

- [ ] **#16 `allowMainThreadQueries()`** (`TichuDatabase.java:19`)
  All DB I/O blocks the UI thread. Acceptable with small datasets; worth revisiting if the
  app needs to handle large game histories. Requires #15 and ideally #25 to be done first.

- [x] **#18 Replace `kankan.wheel.widget` with `NumberPicker`**
  Replaced all three `WheelView` instances in `ScoreHandFragment` with `android.widget.NumberPicker`.
  API mapping: `setViewAdapter` → `setMinValue/setMaxValue/setDisplayedValues`; `addChangingListener` →
  `setOnValueChangedListener`; `getCurrentItem/setCurrentItem` → `getValue/setValue`.
  Score pickers use `setWrapSelectorWheel(false)`; player picker uses `true`.
  Deleted all 15 kankan source files and 2 drawables (`wheel_bg.xml`, `wheel_val.xml`).

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
