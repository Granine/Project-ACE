/*
1. The game is played between two hands, the player and the banker. 

2. Each round of play (known as a baccarat coup) has three possible outcomes: player (player has the higher score), banker, and tie.

3. Cards two through nine are face value, while a ten, jack, queen, and king are worth zero, and an ace is worth one point.

4. The value of a hand in baccarat is determined by adding up all the values of the individual cards.  

5. If the sum of the two cards is a two-digit number, the first digit is dropped. For example, a hand with a seven and an eight is worth five (the rightmost digit of the total of 15).

6. The player and the banker each start with two cards. 

7. If either the player or the banker or both achieves a total of 8 or 9 at this stage, the coup is finished and the result is announced. This is known as a “natural” win.

8. If neither hand has a total of 8 or 9, the drawing rules (the tableau) are applied to determine whether the player should receive a third card.

9. 
Baccarat rules call for the Player to always go first. The Banker’s hand depends on the Player’s hand. Here are the rules:
    If the Player’s hand is a total of 8 or 9 points, it’s a natural win and no additional cards will be drawn. The Player’s hand will stand.
    The Player’s hand always stands on point totals of 6 or 7 as well.
    On any total from 0 to 5, the Player draws a third card – unless the Banker has 8 or 9. In that case, the Banker wins, and there are no further cards drawn.
There is a special set of rules in play when the Banker draws a third card. There’s no need to memorize these rules, but you can review them beside.

10. 
When Banker’s first two cards total:

    0 – 1 – 2 : Bank draws unless Player shows a natural 8 or 9.

    3 : Bank draws if Player’s third card is 0 – 1 – 2 – 3 – 4 – 5 – 6 – 7 or 9.
    Bank stands when Player’s third card is 8.

    4 : Bank draws if Player’s third card is 2 – 3 – 4 – 5 – 6 – 7.
    Bank stands when Player’s third card is 0 – 1 – 8 – 9.

    5 : Bank draws if Player’s third card is 4 – 5 – 6 – 7.
    Bank stands when Player’s third card is 0 – 1 – 2 – 3 – 8 – 9.

    6 : Bank draws if Player’s third card is 6 – 7.
    Bank stands when Player’s third card is 0 – 1 – 2 – 3 – 4 – 5 – 8 – 9.

    7 : Stands.

    8 – 9 : Natural Numbers – neither hand draws a third card.

11. The deck is re-shuffled at the end of each coup. 

12. The game continues until all players have finished betting, after which the total amount of money on the table is counted to determine the winner.

winning:
if the banker wins, he will receive total bet -5%.
if player wins, he will receive total bet 
*/
const gameAssets = require('./GameAssets');
const GameAssets = require('./GameAssets'); 

class Baccarat {
    static newGame(gameData){
        let gameDataLocal = JSON.parse(JSON.stringify(gameData))
        
        gameDataLocal.gameItems.globalItems = {
            pokar: GameAssets.getPokar(),
            playerHand: [],
            bankerHand: [],
        };
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

    static _getHandValue(gameData) {
        let playerScore = 0;
        let bankerScore = 0;
        let cardValue = 0;
        //get the value of the player hand
        for (let card of gameData.gameItems.globalItems.playerHand) {
            cardValue = gameAssets.getPokarFaceValue(card);
            
            if (cardValue <= 9) {
                playerScore += cardValue
            }
        }
        //get the value of the banker hand
        for (let card of gameData.gameItems.globalItems.bankerHand) {
            cardValue = gameAssets.getPokarFaceValue(card);
            
            if (cardValue <= 9) {
                playerScore += cardValue
            }
        }
        return {
            playerScore: playerScore,
            bankerScore: bankerScore,
        }
    }

    
    static playTurn(gameData) {
        let gameDataLocal = JSON.parse(JSON.stringify(gameData))
        // take turn passing 2 card to each player
        gameDataLocal.gameItems.globalItems.playerHand.append(this._getRandomCard(gameDataLocal));
        gameDataLocal.gameItems.globalItems.bankerHand.append(this._getRandomCard(gameDataLocal));
        gameDataLocal.gameItems.globalItems.playerHand.append(this._getRandomCard(gameDataLocal));
        gameDataLocal.gameItems.globalItems.bankerHand.append(this._getRandomCard(gameDataLocal));
        
        // check if more card should be given
        let handValue = this._getHandValue(gameDataLocal);
        let gameOver = 0;
        let thirdPlayerCard = 0;
        if (handValue.playerScore >= 8 || handValue.bankerScore >= 8) {
            gameOver = 1;
        }
        // player draw if <=5
        if (gameOver !== 0 && handValue.playerScore <= 5) {
            thirdPlayerCard = this._getRandomCard(gameDataLocal);
            gameDataLocal.gameItems.globalItems.playerHand.append(thirdPlayerCard);
        }
        // banker draw if <=2
        if (gameOver !== 0 && handValue.bankerScore <= 2) {
            gameDataLocal.gameItems.globalItems.bankerHand.append(this._getRandomCard(gameDataLocal));
        }
        // banker draw if current score is 3 and player 0-7 or 9
        else if (gameOver !== 0 
            && handValue.bankerScore === 3 
            && (handValue.playerScore <= 7 || handValue.playerScore === 9)
            ) {
            gameDataLocal.gameItems.globalItems.bankerHand.append(this._getRandomCard(gameDataLocal));
        }
        // banker draw if current score is 4 and player 2-7
        else if (gameOver !== 0 
            && handValue.bankerScore === 4 
            && (handValue.playerScore <= 7 && handValue.playerScore >= 2)
            ) {
            gameDataLocal.gameItems.globalItems.bankerHand.append(this._getRandomCard(gameDataLocal));
        }
        // banker draw if current score is 5 and player 4-7
        else if (gameOver !== 0 
            && handValue.bankerScore === 5 
            && (handValue.playerScore <= 7 && handValue.playerScore >= 4)
            ) {
            gameDataLocal.gameItems.globalItems.bankerHand.append(this._getRandomCard(gameDataLocal));
        }
        // banker draw if current score is 6 and player 6-7
        else if (gameOver !== 0 
            && handValue.bankerScore === 6 
            && (handValue.playerScore <= 7 && handValue.playerScore >= 6)
            ) {
            gameDataLocal.gameItems.globalItems.bankerHand.append(this._getRandomCard(gameDataLocal));
        }
        
        // game over
        gameDataLocal.gameItems.currentPlayerIndex = -1; // game over
    }

    static calculateWinning(gameData){
        // check game is over
        if(gameData.currentPlayerIndex !== -1){
            return 0;
        }
        let gameDataLocal = JSON.parse(JSON.stringify(gameData))
        let handValue = this._getHandValue(gameDataLocal);

    }
}

module.exports = Baccarat;