# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug    # Build debug APK
./gradlew assembleRelease  # Build release APK
./gradlew build            # Full build (debug + release)
./gradlew clean            # Clean build outputs
```

No tests exist in this project.

## Project Overview

**Tichu Guru** is a scoring and statistics tracking Android app for the card game Tichu (a 4-player partnership trick-taking game). It records game sessions, tracks player stats, and supports CSV export.

- **Language:** Java 17
- **Min SDK:** 30, Compile/Target SDK: 34
- **AGP:** 9.1.0, Gradle: 9.3.1
- **Package:** `com.tichuguru`

## Architecture

The app uses a classic Activity-centric architecture with a **singleton Application class** for global state.

### Global State (`TGApp.java`)
`TGApp` extends `Application` and holds all in-memory app state:
- `curGame` — the active `Game` object
- `games` — list of all historical `Game` objects
- `players` — list of all `Player` objects

Data is saved to binary files via Java serialization (`Externalizable`) on app pause:
- `Games.dat` — serialized game records
- `Players.dat` — serialized player profiles

### Data Models
- **`Game`** — game state, list of `Hand` objects, player assignments, scoring
- **`Hand`** — one hand's scoring: Tichu/Grand Tichu bids, card points, outcomes
- **`Player`** — player profile with cumulative stats (wins, Tichu success rates, etc.)

All models implement `Externalizable` for custom binary serialization.

### Navigation
`TGActivity` is the launcher — a `TabActivity` hosting tabs for the main screens. Activities for specific flows are launched on top.

### Activities
| Activity | Purpose |
|---|---|
| `TGActivity` | Tab host / main entry point |
| `CurHandActivity` | Current hand display and scoring UI |
| `ScorecardActivity` | Scorecard for the current game |
| `AllGamesActivity` | Historical game list |
| `StatsActivity` | Statistics dashboard |
| `StatsListActivity` | Detailed per-player statistics |
| `NewGameActivity` | New game setup (player selection) |
| `ScoreHandActivity` | Hand score entry form |

### Embedded Libraries
- `kankan.wheel.widget.*` — a scroll-wheel picker widget, embedded directly in source (not a Gradle dependency)
- `SegmentedControlButton` — custom view for Tichu/Grand Tichu call selection

## Key Patterns to Follow

- **Saving data:** Call `TGApp` save methods; data persists via `onPause` lifecycle hooks. Don't bypass this.
- **Accessing global state:** Use `TGApp.getCurGame()`, `TGApp.getGames()`, `TGApp.getPlayers()` static accessors.
- **Serialization:** When adding fields to `Game`, `Hand`, or `Player`, update their `writeExternal`/`readExternal` methods and increment any version tracking if present — otherwise saved data will fail to deserialize.
- **No dependency injection, no Fragments, no ViewModel** — this is pre-modern-Android architecture. Keep new code consistent with the existing style.

## Rules (Distilled from Installed Skills)

### Error Handling
- Never swallow exceptions silently. Catch specific exception types; log failures in file I/O and deserialization so broken saves surface rather than silently corrupt state.
- File I/O and `ObjectInputStream` deserialization are the highest-risk failure points — wrap each in try-catch with a meaningful log or user-visible fallback.

### Code Quality
- Use named constants for Tichu game rule values (e.g., bonus scores, point thresholds) instead of magic numbers. Place them as `static final` fields on the relevant model class.
- Prefer early returns over deep nesting in Activity logic. Long `if-else` chains across 3+ levels should be flattened.
- Comments should explain *why* (game rule rationale, edge cases), not restate what the code does.
- YAGNI: don't add configurability or abstractions for hypothetical future game variants.

### Separation of Concerns
- Scoring and game-rule logic belongs in model classes (`Game`, `Hand`), not in Activities. Activities handle UI events and delegate to models.
- Persistence logic (file reads/writes) belongs in `TGApp`, not scattered across Activities.

### Build Verification
- Run `./gradlew assembleDebug` before considering any change complete. Fix all compiler errors and warnings before moving on.
- After any change to `AndroidManifest.xml` or `build.gradle`, do a clean build (`./gradlew clean assembleDebug`) to catch configuration issues.

### Security & Data Safety
- This app writes user game data to local files — never write data to external storage paths accessible to other apps without explicit user action.
- The `WRITE_EXTERNAL_STORAGE` permission is declared for CSV export only; scope all file writes accordingly.
