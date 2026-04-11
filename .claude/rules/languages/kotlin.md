---
paths:
  - "**/*.kt"
---
# Kotlin Rules

## Principles

- Explicit null handling (requireNotNull with message, checkNotNull with message, no bare `!!`)
- Boolean property naming without `is` prefix (avoids Kotlin getter clash, e.g. `gameOver` not `isGameOver`)
- Backing property as val getter (expose `val foo: T get() = _foo`, never `fun foo(): T` — linter enforces matching property)
- Constants in companion objects (`const val` in relevant model class companion, not top-level or object singletons)

## Examples
When in doubt: ./kotlin.examples.md
