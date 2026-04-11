# Kotlin Rules - Examples

## Principles Examples

### Explicit null handling
**Good:**
```kotlin
hand = requireNotNull(BundleCompat.getSerializable(requireArguments(), ARG_HAND, Hand::class.java)) { "hand arg missing" }
```
**Bad:**
```kotlin
hand = BundleCompat.getSerializable(requireArguments(), ARG_HAND, Hand::class.java)!!
```

### Boolean property naming without `is` prefix
**Good:**
```kotlin
class Game(
    var gameOver: Boolean = false,
    var mercyRule: Boolean = false,
    var ignoreStats: Boolean = false,
    var addOnFailure: Boolean = false,
)
```
**Bad:**
```kotlin
class Game(
    var isGameOver: Boolean = false,
    var isMercyRule: Boolean = false,
)
```

### Backing property as val getter
**Good:**
```kotlin
private var _cardScore1: Int = 0
val cardScore1: Int get() = _cardScore1
```
**Bad:**
```kotlin
private var _cardScore1: Int = 0
fun cardScore1(): Int = _cardScore1
```

## Project-specific Examples

### `dbScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))`
```kotlin
// Single-writer scope prevents concurrent DB writes
private val dbScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

private fun saveGames() {
    dbScope.launch { repository.saveGames(players, games) }
}
```

### `Fragment.newInstance(...)` with `Bundle().apply { putSerializable(...) }`
```kotlin
companion object {
    private const val ARG_HAND = "hand"
    private const val ARG_PLAYER_NAMES = "playerNames"

    fun newInstance(hand: Hand, playerNames: Array<String>): ScoreHandFragment =
        ScoreHandFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_HAND, hand)
                putStringArray(ARG_PLAYER_NAMES, playerNames)
            }
        }
}
```
