# Tichu Guru — Improvement Plan

## Status Key
- [ ] Pending
- [x] Done

---

## Bugs

- [x] **#1 `Player.equals()` crashes on non-Player input** (`Player.java:363`)
  Unchecked cast with no null/type guard. Fixed: added `instanceof` check.

- [ ] **#2 `unrecordHand` corrupts tichu efficiency stats** (`Player.java:151-155`)
  `recordHand` adds ±100 per individual player tichu. `unrecordHand` subtracts
  `hand.getTichuScore1()` — the whole team's tichu sum — so removing a hand when
  both team-1 players called tichu subtracts 200 instead of 100.

- [x] **#3 `TGApp.onCreate()` calls `super.onCreate()` last** (`TGApp.java:30`)
  `super.onCreate()` must be first. Fixed.

- [x] **#4 CSV export removed**
  Broken on Android 11+ (`Environment.getExternalStorageDirectory()` revoked on API 30+).
  Removed entirely: `exportCsv()` from `CurHandActivity`/`CurHandFragment`, `saveCSV()`
  and `CSV_FILE` from `TGApp`, `getCSVHeader()`/`toCSVString()` from `Player`.

---

## Data Safety

- [ ] **#5 `fallbackToDestructiveMigration()` silently wipes all data on schema change** (`TichuDatabase.java:21`)
  DB is at version 3. Any future schema change deletes all user data with no warning.
  Fix: add proper Room migrations, or warn the user before destruction.

- [x] **#6 Persistence relied on `onPause` instead of eager saves at mutation points**
  `TGActivity.onPause()` was the only save point, risking data loss if the process was
  killed before the activity lost focus. Fixed: saves now happen immediately at each
  mutation point (`scoreHand`, `endGame`, `removeHand`, `startGame`, `addPlayer`,
  `clearStats`). Removed `onPause` saves from `TGActivity` and `NewGameActivity`.

---

## Deprecated / Broken APIs

- [ ] **#7 `startActivityForResult` deprecated**
  Used in `CurHandActivity`, `CurHandFragment`, `TGActivity`.
  Fix: migrate to `registerForActivityResult` / `ActivityResultLauncher`.

- [ ] **#8 `getSystemService("layout_inflater")` deprecated** (`StatsActivity.java:123`)
  Fix: use `LayoutInflater.from(context)`.

- [ ] **#9 Magic number `524288` for window flag** (`TGActivity.java:31`)
  Fix: use `WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS`.

---

## Code Quality

- [ ] **#10 `CurHandActivity.clearTichuButtonsNow` is public static mutable** (`CurHandActivity.java:29`)
  `CurHandFragment` reads and resets this field from its host Activity's static state.
  Fix: expose a `clearButtons` LiveData event on `TGViewModel` instead.

- [ ] **#11 Hardcoded color integers** (`CurHandFragment.java:164-165`, `CurHandActivity.java:183-195`)
  `-256` (yellow) and `-7829368` (gray) should be `@ColorRes` entries in `colors.xml`,
  referenced via `ContextCompat.getColor()`.

- [ ] **#12 Reflection in `StatsActivity.Getter`** (`StatsActivity.java:302-315`)
  Works but breaks silently on any `Player` getter rename. No compile-time safety.
  Fix: replace with typed `Function<Player, ?>` lambdas.

- [x] **#13 Dead `Externalizable` code on model classes**
  `Game`, `Hand`, `Player` migrated from `Externalizable` to plain `Serializable`.
  Removed `readExternal`/`writeExternal`/`REVISION` from all three. `serialVersionUID`
  retained on each since `Game` and `Hand` are still passed via `Bundle.putSerializable()`
  between activities.

- [ ] **#14 `CurHandActivity` appears to be dead code**
  `TGActivity` hosts `CurHandFragment`; nothing navigates to `CurHandActivity` directly.
  Verify, then remove to avoid maintaining duplicate logic.

---

## Architecture (Low Priority)

- [ ] **#15 `TichuDatabase.getInstance()` not thread-safe** (`TichuDatabase.java:16`)
  No synchronization on the singleton. Harmless while `allowMainThreadQueries()` is in use,
  but should be fixed (double-checked locking or `volatile`) before any async work is added.

- [ ] **#16 `allowMainThreadQueries()`** (`TichuDatabase.java:19`)
  All DB I/O blocks the UI thread. Acceptable with small datasets; worth revisiting if
  the app needs to handle many games.

---

## UI Modernization (from tichu-guru-patterns.md)

These are larger refactors. Do not implement unless explicitly requested.

- [ ] **#17 Replace `ListView` with `RecyclerView`**
  Used in `StatsActivity`, `AllGamesActivity`, `StatsListActivity`. `RecyclerView` is the
  modern replacement — better performance, view holder pattern enforced.

- [ ] **#18 Replace `kankan.wheel.widget` with `NumberPicker` or Material `Slider`**
  The embedded scroll-wheel library (`kankan/wheel/widget/`) is unmaintained third-party
  source copied into the repo. Used in `ScoreHandActivity` for score and player selection.
  Fix: replace with standard `NumberPicker` or a Material component.

- [ ] **#19 Replace `SegmentedControlButton` with `MaterialButtonToggleGroup`**
  `SegmentedControlButton` is a custom view used for Tichu/Grand Tichu call selection.
  `MaterialButtonToggleGroup` (from Material Components) provides the same UX natively.

- [ ] **#20 Replace `onPrepareOptionsMenu` with Toolbar + overflow menu**
  `CurHandFragment` and `CurHandActivity` use the legacy options menu API.
  Fix: add a `Toolbar` to the layout and use `MenuProvider` or `onCreateOptionsMenu` on
  the Toolbar directly.
