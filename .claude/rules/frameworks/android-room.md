---
paths:
  - "**/*.kt"
---
# Android Room Rules

## Principles

- Upsert over insert-replace (`@Upsert` instead of `@Insert(onConflict = REPLACE)`)
- Entity-model round-trip identity (`Entity.from(model)` includes `id = model.dbId`, `toModel()` sets `model.dbId = id`)
- Orphan cleanup after collection upsert (`deleteOrphanHands` with `NOT IN (:keepIds)` after upserting child list)
- No destructive migration (increment `version` + add `Migration(n, n+1)`, never `fallbackToDestructiveMigration()`)

## Examples
When in doubt: ./android-room.examples.md
