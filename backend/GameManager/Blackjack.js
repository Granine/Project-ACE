/*
1. Blackjack is a card game played between the House (dealer) and the players. The dealer deals two cards to each player and two to themselves, with one facing up and one facing down.

2. The objective of the game is to beat the dealer's hand without exceeding a total point value of 21. The value of each card is as follows: 2-10 (as it is), Jack, Queen, King (each worth 10 points) and Ace (either 1 or 11, if adding 11 breaks 21, it's counted as 1).

3. After receiving the first two cards, players can choose to "Stand" (keep their current cards), "Hit" (receive another card)

4. Once all players have completed their turns, it's the dealer's turn. The dealer must always hit until they have a total of 17 or higher.

5. If the player's hand exceeds 21 (busting), they automatically lose, no matter what the dealer's hand is.

6. The game continues with players trying to reach a score of 21 or as close to 21 as possible, without going over.

Winning:
1. If the player has Blackjack (an Ace and a 10-point card as their first two cards) and the dealer does not, the player automatically wins

2. If the player has a final sum closer to 21 and it is higher than the dealer’s total, or if the dealer goes over 21 (busts), the player wins.

3. If both, the player and dealer, have the same point value in their hands, it is a tie

4. If the dealer's hand is closer to 21 or the player's hand goes over 21 (bust), the player loses the bet placed.

Unique:
in this version of the game, dealer will be computer and will stop whenever he hit 17+ or bust
*/

const GameAssets = require('./GameAssets'); 

class Blackjack {
    static newGame(gameData){
        let gameDataLocal = JSON.parse(JSON.stringify(gameData))
        
        gameDataLocal.gameItems.globalItems = {
            pokar: GameAssets.getPokar(),
            dealerHand: [],
        };
        // for each player, setup their hand
        for (let i = 0; i < gameDataLocal.playerList.length; i++) {
            gameDataLocal.gameItems.playerItems[gameDataLocal.playerList[i]] = {
                playerHand: [],
                playerState: 0, // 0 = still in game, 1 = hold before or >21
            }
        }
        return gameDataLocal;
    }

    /* get a random card from the deck
    @param {json/object} gameData: the gameData object
    @return {str} card: the card generated, see getPokar() for values
    */

    static _getRandomCard(gameData) {
        //get a random card
        return gameData.gameItems.globalItems.pokar[Math.floor(Math.random() * 52)];
    }

    /* get the value of the hand
    @param {json/object} gameData: the gameData object
    @return {json} handValue: the value of the player and banker hand 
        {"PLAYERNAME": val (int)), "PLAYERNAME": val (int)), ...,  "bankerScore": (int)}
    */
    static _getHandValue(gameData) {
        let playerScore = 0;
        let dealerScore = 0;
        let cardValue = 0;
        let aceCount = 0
        let returnObject = {}
        //get the value of the player hand
        for (let card of gameData.gameItems.globalItems.dealerHand) {
            cardValue = gameAssets.getPokarFaceValue(card);
            // ignore card 10-13 (0)
            if (cardValue >= 9) {
                dealerScore += 10
            } else if (cardValue === 1) {
                aceCount += 1;
            } else {
                dealerScore += cardValue
            }
        }
        // save all ace to be calculated after other other, so can decide if ace should be 1 or 11
        for (let i = 0; i < aceCount; i++) {
            if (dealerScore + 11 > 21) {
                dealerScore += 1;
            } else {
                dealerScore += 11;
            }
        }
        returnObject["dealerScore"] = dealerScore;
        for (let playerId of gameData.playerList) {
            playerScore = 0;
            aceCount = 0;
            //get the value of the banker hand
            for (let card of gameData.gameItems.playerItems[playerId].playerHand) {
                cardValue = gameAssets.getPokarFaceValue(card);
                
                if (cardValue >= 9) {
                    playerScore += 10
                } else if (cardValue === 1) {
                    playerScore += 11
                } else {
                    playerScore += cardValue
                }
            }
             // save all ace to be calculated after other other, so can decide if ace value
            for (let i = 0; i < aceCount; i++) {
                if (playerScore + 11 > 21) {
                    playerScore += 1;
                } else {
                    playerScore += 11;
                }
            }
            returnObject[playerId] = playerScore;
        }
        return returnObject;
    }

    /* play a turn of the game
    for BJ, this updates the player and banker hand of global items
    @param {json} gameData: the gameData object
        return 0 on error
    */
    static playTurn(gameData, action) {
        let gameDataLocal = JSON.parse(JSON.stringify(gameData))
        // check if more card should be given
        let handValue = this._getHandValue(gameDataLocal);
        let gameOver = 0; // assume game over
        let newAction = {}
        let playerHand = []
        let dealerHand = gameDataLocal.gameItems.globalItems.dealerHand
        // if round 1
        if (gameDataLocal.currentTurn === 0) {
            // deal cards to all player
            for (let playerId of gameDataLocal.playerList) {
                playerHand = gameDataLocal.gameItems.playerItems[playerId].playerHand
                playerHand.append(this._getRandomCard(gameDataLocal));
                playerHand.append(this._getRandomCard(gameDataLocal));
            }
            // deal cards to dealer
            dealerHand.append(this._getRandomCard(gameDataLocal));
        } else {
            // for each player in action list, check if their status is 1 (made action)
            for (let playerId of gameDataLocal.playerList) {
                if (action[playerId][0] === 1) {
                    // if they hit
                    if (action[playerId][1] === "hit") {
                        playerHand = gameDataLocal.gameItems.playerItems[playerId].playerHand
                        playerHand.append(this._getRandomCard(gameDataLocal));
                        handValue = this._getHandValue(gameDataLocal);
                        // if >21, update player state to 1
                        if (handValue[playerId] > 21) {
                            gameDataLocal.gameItems.playerItems[playerId].playerState = 1;
                        }
                    } else if (action[playerId][1] === "stand") {
                        gameDataLocal.gameItems.playerItems[playerId].playerState = 1;
                    }
                }
            }
        }
        // get next player, and determine if game is over
        gameDataLocal = this._getNextPlayer(gameDataLocal);
        // update actions table accordingly
        newAction = this._updatePlayerAction(gameDataLocal, action);
        // if user all made action, dealer will play
        if (gameDataLocal.currentPlayerIndex === -1){
            // dealer will play
            while (handValue.dealerScore < 17) {
                dealerHand.append(this._getRandomCard(gameDataLocal));
                handValue = this._getHandValue(gameDataLocal);
            }
        }

        return gameDataLocal;
    }

    static calculateWinning(gameData){
        // check game is over
        if(gameData.currentPlayerIndex !== -1){
            return 0;
        }
        let gameDataLocal = JSON.parse(JSON.stringify(gameData))
        let handValue = this._getHandValue(gameDataLocal);
        // prepare returning object
        let gameResult = {}
        for (let i = 0; i < gameDataLocal.playerList.length; i++) {
            gameResult[gameDataLocal.playerList[i]] = "";
        }
        // get bets placed by each player
        for (let i = 0; i < gameDataLocal.playerList.length; i++) {
            let playerIdValue = gameDataLocal.playerList[i];
            let playerBets = gameDataLocal.betsPlaced[playerIdValue];
            let winningAmount = 0;
            // for each bet placed by player, calculate each winning amount, ignore no win
            for (let j = 0; j < playerBets.length; j++) {
                let betType = playerBets[j].betOnWhat;
                let betValue = playerBets[j].amount;
                
                winningAmount += this._didBetWin(betType, handValue, betValue);
            }
            gameResult[playerIdValue] = winningAmount;
        }
        return gameResult;
    }

    /* after user placed bet, played a turn, wining amount will be calculated according to the rules
    @param {json} playerBet: the bet placed by the player
    @param {int} rouletteNumber: the number generated by the roulette
    @return {int} winningAmount: the amount of money the player wins
        or 0 on error
    */
    static _didBetWin(betType, handValue, betValue){
        let winningAmount = -betValue;
        if (betType === "self") {
            if (handValue.playerScore > handValue.bankerScore) {
                winningAmount += betValue * 2;
            } else if (handValue.playerScore === handValue.bankerScore) {
                winningAmount += betValue;
            }
        } 
        return winningAmount;
    }

    /**
    *get the next player to make a move, directly modify the gameData object
    *@param {json} gameData: the gameData object
    *@modifies {json} gameData: the gameData object modified
    *@return {json} gameData: the gameData object modified
    * if no player avaliable, update currentPlayerIndex to -1
    */
    static _getNextPlayer(gameData){
        let playerId = "";
        let currentPlayerIndex = gameData.currentPlayerIndex;
        let playerCount = gameData.playerList.length;
        
        // Check players starting from next index to end of list
        for (let i = currentPlayerIndex + 1; i < playerCount; i++) {
            playerId = gameData.playerList[i];
            if (gameData.gameItems.playerItems[playerId].playerState === 0) {
                gameData.currentPlayerIndex = i;
                return gameData;
            }
        }

        // If no player found, wrap back to start of list
        for (let i = 0; i < currentPlayerIndex; i++) {
            playerId = gameData.playerList[i];
            if (gameData.gameItems.playerItems[playerId].playerState === 0) {
                gameData.currentPlayerIndex = i;
                return gameData;
            }
        }

        // If still no player found, set currentPlayerIndex to -1
        gameData.currentPlayerIndex = -1;
        return gameData;
    }

    /*
    *update the player action, directly modify the gameData object
    * 1. find the current player in action with status == 2, update it to 0
    * 2. find the current player in action with status == 1, update it to 2
    * 3. find the current player with currentPlayerIndex, update it to 1
    *@param {json} gameData: the gameData object
    *@param {json} action: the action object
    *@modifies {json} gameData: the gameData object modified
    *@return {json} gameData: the gameData object modified
        return 0 on error
    */
    static _updatePlayerAction(gameData, action){
        // check game not over 
        
        let playerId = "";
        let currentPlayerIndex = gameData.currentPlayerIndex;
        
        // update last player to normal state
        for (let playerId of Object.keys(action)) {
            if (action[playerId][0] === 2) {
                action[playerId][0] = 0;
                break;
            }
        }
        // update current player to last player
        for (let playerId of Object.keys(action)) {
            if (action[playerId][0] === 1) {
                action[playerId][0] = 2;
                break;
            }
        }
        // if game is not over, assign next player to current player
        if(gameData.currentPlayerIndex === -1){
             // update current player status to 1
            playerId = gameData.playerList[currentPlayerIndex];
            action[playerId].playerState = 1;
        }
        
       
        return action;
    }
}

module.exports = Blackjack;