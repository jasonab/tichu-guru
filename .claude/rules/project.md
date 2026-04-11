# Project Rules

## Architecture

- ViewModel owns all mutations — Fragments call ViewModel methods, ViewModel updates private MutableLiveData and fires DB save. No public `notify*()` or `refresh()` methods.
- Save eagerly at every mutation — `dbScope.launch { repository.save*() }` fire-and-forget. Never defer persistence to `onPause` or `onStop`.
- Business logic in `model/` — scoring, stat calculation, and game rules belong in `Game`/`Hand`/`Player`, not in Fragments or ViewModel.
- Fragment navigation via `TGActivity.pushFragment()` — adds to back stack, hides active tab fragment. Sub-screens (NewGame, ScoreHand, StatsList) are pushed; tab fragments (CurHand, Scorecard, AllGames, Stats) are shown/hidden.
- Fragment result communication via `parentFragmentManager.setFragmentResult()` — sub-screens signal completion back to tab fragments.

## Conventions

- Seat indexing: 0-3, team1 = seats 0+2, team2 = seats 1+3. This permeates all scoring, stat recording, and UI layout.
- Domain objects are mutable classes (not data classes) — `Game`, `Hand`, `Player` use `var` properties mutated in place. Only entities are `data class`.
- `Serializable` for Fragment args — `Game` and `Hand` implement `Serializable` for `Bundle.putSerializable()`. Do not switch to `Parcelable`.

## Testing

- Model-only unit tests — test `Game`, `Hand`, `Player` logic with JUnit. No Android instrumentation tests for model code.
- Test naming: `methodName_condition_expectedResult` or `methodName_expectedResult` (e.g., `tichu_success_addsToCallerTeam`).
- Helper methods in test classes for common setup (e.g., `hand(score1, score2)` factory).

## Examples
When in doubt: ./project.examples.md
