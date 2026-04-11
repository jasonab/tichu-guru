# Android Room Rules - Examples

## Principles Examples

### Upsert over insert-replace
**Good:**
```kotlin
@Dao
interface GameDao {
    @Upsert
    fun upsert(game: GameEntity): Long
}
```
**Bad:**
```kotlin
@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(game: GameEntity): Long
}
```

### Entity-model round-trip identity
**Good:**
```kotlin
// Entity.from(model) preserves DB id for upsert idempotency
fun from(g: Game) = GameEntity(
    id = g.dbId,       // threads DB identity back
    player0 = g.players[0].dbId,
    ...
)

// toModel() sets model.dbId = id
fun toPlayer(): Player {
    val p = Player(name)
    p.dbId = id        // threads DB identity to domain
    ...
}
```

### Orphan cleanup after collection upsert
```kotlin
// After upserting hands, delete any orphans no longer in the list
val handIds = db.handDao().upsertAll(handEntities)
hands.forEachIndexed { i, h -> if (handIds[i] > 0) h.dbId = handIds[i] }
db.handDao().deleteOrphanHands(gid, hands.map { it.dbId })
```

## Project-specific Examples

### `db.runInTransaction { ... }`
```kotlin
db.runInTransaction {
    val entities = players.map { PlayerEntity.from(it) }
    val ids = db.playerDao().upsertAll(entities)
    players.forEachIndexed { i, p -> if (ids[i] > 0) p.dbId = ids[i] }
    for (g in games) {
        val upsertedId = db.gameDao().upsert(GameEntity.from(g))
        if (upsertedId > 0) g.dbId = upsertedId
        // ... hand upsert + orphan cleanup
    }
}
```
