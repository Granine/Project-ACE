const MongoClient = require('mongodb').MongoClient;

class Blackjack {

    constructor(db, gameManager) {
        this.gameManager = gameManager;
        this.db = db;
        this.playerScore = 0;
        this.dealerScore = 0;
        this.gameData = {
            playerScore: this.playerScore,
            dealerScore: this.dealerScore,
            gameOver: this.gameManager.gameOver,
            winner: this.gameManager.winner
        };
        this.updateDB();
    }

    // Generate a random card in blackjack
    getRandomCard() {
        let card = Math.floor(Math.random() * 13) + 1;
        return Math.min(card, 10);
    }

    // Blackjack now can have multiple turns, so assume a turn is getting one random card
    play_turn() {
        if (!this.gameManager.gameOver) {
            this.dealerScore += this.getRandomCard();
            this.playerScore += this.getRandomCard();

            // Check the current scores to see if the game is over
            if (this.playerScore > 21 || this.dealerScore >= 17) {
                this.gameManager.gameOver = true;
                if (this.playerScore > 21) {
                    this.gameManager.winner.push("Dealer");
                } else if (this.dealerScore > 21 || this.playerScore > this.dealerScore) {
                    this.gameManager.winner.push("Player");
                } else {
                    this.gameManager.winner.push("Dealer");
                }
                this.gameManager.finish();
            }
            this.gameData = {
                playerScore: this.playerScore,
                dealerScore: this.dealerScore,
                gameOver: this.gameManager.gameOver,
                winner: this.gameManager.winner
            };
            this.updateDB();
        } else {
            console.log("Game is already over! Winner: " + this.gameManager.winner);
        }
    }

    /*get the next player to make a move, directly modify the gameData object
    @param {json} gameData: the gameData object
    @modifies {json} gameData: the gameData object modified
    @return {json} gameData: the gameData object modified
    */
    _nextPlayer(gameData){
        // Update the current player index
        gameData.currentPlayerIndex = (gameData.currentPlayerIndex + 1) % gameData.playerList.length;
        return gameData;
    }
}

module.exports = Blackjack;