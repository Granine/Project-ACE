




### Blackjack
flow:
1. startGame(info)->to first player, the first playturn will have the first 2 cards for all player, and one card for dealer
    emit startGame->gameData
    emit playerTurn->gameData, action: to all players, only person with status flag == 1 should make action
        we need to show all action to all player as people need to know what other people did, like if they hit or not.
        the person made action last turn will be labeled as -1 (so one can display: player X choose to hit, etc)
        if a player blows up or hold, he will never be called again, (have flag == 1)
        after everyone blows up or hold, game is over.
    
2. playTurn(info, action) for each player with flag ==1, they can choose "hit" or "stand"
    emit playerTurn->gameData, action: to all players, only person with status flag == 1 should make action
    last person that made any action will be labled with flag == 2
* playTurn(info, action) when the last player finished, the dealer will automatically make their turn, the dealer will hit until >= 17 or >= 21
    game flag gameData.currentPlayerIndex will be set to -1 to indicate game finished
    emit gameOver->gameData, playerAction, gameResult
* if between anyturn, anybody timedout, the game will stand them, and return playerAction, telling everyone this person timedout (2)
    emit timeout->playerAction: only used to show who timeout (only 1 flag with value == 2 will show)
    emit playerTurn->gameData, action: get next player (person timed out will show as 2, as last player)