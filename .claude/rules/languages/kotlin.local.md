---
paths:
  - "**/*.kt"
---
# Kotlin Project-specific Patterns

## Project-specific patterns

- `Color.YELLOW` / `Color.GRAY` - UI color constants, never raw integer literals (except `0xFF00AA00.toInt()` for green)
- `requireNotNull(BundleCompat.getSerializable(args, KEY, Cls::class.java)) { "msg" }` - Fragment argument extraction pattern
- `Fragment.newInstance(...)` with `Bundle().apply { putSerializable(...) }` - Fragment factory pattern
- `dbScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))` - single-writer DB scope in ViewModel
- `Player : Comparable<Player>` - players are sorted alphabetically by name

## Examples
When in doubt: ./kotlin.examples.md
