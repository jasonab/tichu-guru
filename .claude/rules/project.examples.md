# Project Rules - Examples

## Architecture Examples

### ViewModel owns all mutations
**Good:**
```kotlin
// ViewModel: mutate state, save, update LiveData
fun scoreHand(hand: Hand) {
    curGame?.scoreHand(hand)
    saveGames()
    savePlayers()
    _currentGame.value = curGame
    _allGames.value = games
}
```
**Bad:**
```kotlin
// Fragment directly mutating and saving
fun onScoreHand() {
    game.scoreHand(hand)
    TGApp.saveGames()  // wrong: Fragment should not call save
    refreshUI()
}
```

### Save eagerly at every mutation
**Good:**
```kotlin
fun deleteGame(game: Game) {
    games.remove(game)
    dbScope.launch { repository.deleteGame(game) }  // immediate fire-and-forget
    _allGames.value = games
    _currentGame.value = curGame
}
```
**Bad:**
```kotlin
// Deferring save to lifecycle callback
override fun onPause() {
    super.onPause()
    saveAllData()  // wrong: data loss if process killed before onPause
}
```

## Conventions Examples

### Seat indexing
```kotlin
// Team1 = seats 0+2, Team2 = seats 1+3
if (seat == 0 || seat == 2) {
    if (game.score1 > game.score2) numWins++
} else if (game.score1 < game.score2) {
    numWins++
}
```

### Test naming
```kotlin
@Test fun scoreHand_addsTotals() { ... }
@Test fun mercyRule_enabled_gameEndsWhenDiffReachesLimit() { ... }
@Test fun tichu_success_addsToCallerTeam() { ... }
```
