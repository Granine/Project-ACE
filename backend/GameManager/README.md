## GameManager

## Roulette
### flow: 
1. startGame(info)->to first player, the first playturn will have the first 2 cards for all player, and one card for dealer
    1. 1 second delay
    2. emit startGame->{}
    3. emit playerTurn->gameData, action: to all players, only person with status flag == 1 should make
    4. emit gameOver->gameData, playerAction

### bets
- red, black, odd, even, low, high: return 1:2 if win, 1:0 if loss
- firstDozen, secondDozen, thirdDozen, firstColumn, secondColumn, thirdColumn: return 1:3 if win, 1:0 if loss
- {single numbers like 0, 1, 2...} return 1:36 if win, 1:0 if loss

## Baccarat
### flow: 
1. startGame(info)->to first player, the first playturn will have the first 2 cards for all player, and one card for dealer
    1. 1 second delay
    2. emit startGame->{}
    3. emit playerTurn->gameData, action: to all players, only person with status flag == 1 should make
    4. emit gameOver->gameData, playerAction

### bets
- player, banker: return 1:2 if win, 1:0 if loss
- tie: return 1:8 if win, 1:0 if loss


## Blackjack
### flow:
1. startGame(info)->to first player, the first playturn will have the first 2 cards for all player, and one card for dealer
    1. 1 second delay
    2. emit startGame->{}
    3. emit playerTurn->gameData, action: to all players, only person with status flag == 1 should make action
        gameData.currentPlayerIndex will be set to 0 to indicate first player
        we need to show all action to all player as people need to know what other people did, like if they hit or not. 
        the person made action last turn will be labeled as 2 (so one can display: player X choose to hit, etc)
        if a player blows up or hold, he will never be called again, (have flag == 1)
        after everyone blows up or hold, game is over.
2. playTurn(info, action) for each player with flag ==1, they can choose "hit" or "stand"
    1. emit playerTurn->gameData, action: to all players, only person with status flag == 1 should make action
        last person that made any action will be labled with flag == 2
        gameData.currentPlayerIndex will always be set to current player, so playerAction[gameData.playerId[gameData.currentPlayerIndex]][0] will always return 1
        playTurn(info, action) when the last player finished, the dealer will automatically make their turn the dealer will hit until >= 17 or >= 21
    game flag gameData.currentPlayerIndex will be set to -1 to indicate game finished
    2. * emit gameOver->gameData, playerAction, gameResult if game is over (no one left to hit), 

* if between anyturn, anybody timedout, the game will stand them, and return playerAction, telling everyone this person timedout (2)
    emit timeout->playerAction: only used to show who timeout (only 1 flag with value == 2 will show)
    emit playerTurn->gameData, action: get next player (person timed out will show as 2, as last player)

### bets
- "self" (a bet on self), return 1:2 if win, 1:0 if loss, 1:1 if tie
