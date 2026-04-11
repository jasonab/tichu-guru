---
paths:
  - "**/*.kt"
---
# Android Room Project-specific Patterns

## Project-specific patterns

- `HandEntity.from(h, gameId, order)` - Hand entity requires game FK and ordering index
- `HandDao.deleteOrphanHands(gameId, keepIds)` - cleanup after hand list upsert
- `TichuDatabase.getInstance(context)` - double-checked locking singleton via companion object
- `TichuRepository` wraps all DAO calls — Fragments/ViewModel never access DAOs directly
- `db.runInTransaction { ... }` - batch player upsert + game upsert + hand upsert + orphan cleanup

## Examples
When in doubt: ./android-room.examples.md
