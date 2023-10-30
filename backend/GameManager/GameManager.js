
const { info } = require('console');
const socketio = require('socket.io');
const Baccarat = require('./Baccarat'); 
const Blackjack = require('./Blackjack');
const Roulette = require('./Roulette');
const EventEmitter = require('events');
const { MongoClient } = require('mongodb');
const { get } = require('http');

/*
playerList structure: 
[
    playerId (str)
]
*/

/*
playerAction structure: 
{
    playerId: [status (int), action (any) (will be processed by game object), action2, ...],
    # status 0: not turn (no actions attached), 1: action chosen, 2: default action (no actions attached)
}
*/

/* betsPlaced, duplicate bet is allowed, ie betting two roulette numbers
{
    playerId: [{betOnWhat: val (str), amount: amount (float)}]
}
*/

/* gameResult
{
    playerId: [{winOnWhat (str), amount (float)}]
}
*/

/*gameItems
{
    globalItems: {
        ITEM_NAME: ITEM_VALUE (any),
    },
    playerItems: {
        PLAYER_NAME: {
            ITEM_NAME: ITEM_VALUE (any),
        },
    },
}

/*gameData structure: must be fully JSON serializable
{
    // unique id for the lobby
    lobbyId: lobbyId (string),          
    // blackjack, baccarat, roulette
    gameType: gameType (string),        
    // list of players in the game, see above for structure
    playerList: playerList (list),      
    // index of the current player in playerList >=0, <= len(playerList)
    currentPlayerIndex: 0 (int),        
    // current turn number >=0
    currentTurn: 0 (int),              
    // dictionary of bets placed by players, see above for structure
    betsPlaced: betsPlaced (dictionary),            
    // dictionary of game objects, see above for structure
    gameItems: defaultGameObjects (dictionary),     
    // list of "userActions", is the list of actions taken by players, 
    actionHistory: [] (list)                        
};
*/

/* 
@requirement: lobbyId to be unique
@requirement: gameType be one of the supported types (see above)
@requirement: playerId in side playerList if unique
@requirement: gameData should not be modified outside of the gameManager class
*/
class GameManager extends EventEmitter {
    /* create a new game manager
    the game manager controls all games being currently player
    it utilizes a database for storing game information and will communicate with the frontend
    @param {socketio} io: the socketio object
    gameData is the main data structure (fixed format dictionary) that will be used as
        main communication variable between functions
    */
    constructor(io) {
        super();
        this.io = io;
        this.gameStore = new GameStore();
        this.timers = {};
    }

    /* create/reset the timer for one single game
    *   @param {dictionary} gameData
    *   @return (int) 1 for success, -1 for fail
    */
    _resetTimer(gameData) {
        // reset the timer, if one exists
        if (this.timers[gameData.lobbyName]) {
            clearTimeout(this.timers[gameData.lobbyName]);
        }

        let defaultAction = this._getDefaultAction(gameData);
 
        // Implement actual timeout
        this.timers[gameData.lobbyName] = setTimeout(() => {
            this._timeoutTurn(gameData, defaultAction);
        }, 15000); // 15 seconds
    }

    /* based on player action, get the consequence
    *   @param {dictionary} gameData
    *   @param {dictionary} playerAction
    *  @return {dictionary} gameData
    */
    _getActionResult(gameData, playerAction) {
        let gameType = gameData.gameType;
        if (gameType == 'blackjack') {
            gameData = Blackjack.playTurn(gameData, playerAction);
        }
        else if (gameType == 'baccarat') {
            gameData = Baccarat.playTurn(gameData, playerAction);
        }
        else if (gameType == 'roulette'){
            gameData = Roulette.playTurn(gameData, playerAction);
        }
        
        return gameData;
    }

    /* based on game status, calculate the winning and modify gameData
    *   @param {dictionary} gameData
        @modify {dictionary} gameData
    * @return {dictionary} gameResult
    *   return 0 on error, like if game not over
    */
    _calculateWinning(gameData) {
        if (gameData.currentPlayerIndex !== -1) {
            return 0;
        }
        let gameType = gameData.gameType;
        if (gameType == 'blackjack') {
            gameData = Blackjack.calculateWinning(gameData);
        }
        else if (gameType == 'baccarat') {
            gameData = Baccarat.calculateWinning(gameData);
        }
        else if (gameType == 'roulette') {
            gameData = Roulette.calculateWinning(gameData);
        }
        
        return gameData;
    }

    /* get the default action for the current player
        gameData.currentTurn will be updated
        gameData.currentPlayerIndex will be updated to next player, or -1 if game is over
    *   @param {dictionary} gameData
    *   @return {dictionary} defaultAction
    */
    _getDefaultAction(gameData) {
        let defaultAction = [];
        for (let i = 0; i < gameData.playerList.length; i++) {
            let userDefaultAction = {};
            userDefaultAction[gameData.playerList[i].playerId] = [1];
            defaultAction.push(userDefaultAction);
        }
        return defaultAction;
    }


    /* create a new game by creating a new gameData object
    *  @param (string) lobbyId
    *  @param (string) gameType
    *  @param {dictionary} playerList
    *  @emit {dictionary} gameData back to caller
    */
    async startGame(lobbyId, gameType, playerList, betsPlaced) {
        // prepare default user items structure
        // defaultItems = {eachUser: {}}
        let defaultItems = {};
        for (let i = 0; i < playerList.length; i++) {
            defaultItems.playerList[i].playerId = {};
        }
        // setup game object with default value
        let gameData = {
            lobbyId: lobbyId,
            gameType: gameType,
            playerList: playerList,
            currentPlayerIndex: 0,
            currentTurn: 0,
            betsPlaced: betsPlaced,
            gameItems: {
                globalItems: {}, 
                playerItems: defaultItems
            },
            actionHistory: []
        };
        // setup new game based on game type
        if (gameType == "blackjack") {
            gameData = Blackjack.newGame(gameData);
        } else if (gameType == "baccarat") {
            gameData = Baccarat.newGame(gameData);
        } else if (gameType == "roulette") {
            gameData = Roulette.newGame(gameData);
        }
        // complete game creation, will prepare the game settings, but no action will be performed
        this.gameStore.newGame(gameData);
        this._delay(1000);
        this.io.to(gameData.lobbyId).emit('gameStarted', {});
        
        let defaultAction = this._getDefaultAction(gameData);
        // play first step of the game, often setup the game for first action
        this._getActionResult(gameData, defaultAction);

        this.gameStore.updateGame(gameData);
        this._resetTimer(gameData);
    }

    /* handler in case a time out occurs
    *   @param {dictionary} gameData
    *   @param {dictionary} action
    *   @emit {dictionary} gameData back to caller
    *   @emit {dictionary} action back to caller
    */
    async _timeoutTurn(gameData, action) {
        this.io.to(gameData.lobbyId).emit("timeout", {"playerAction": action});
        this.playTurn(gameData, action);
    }

    /* play a single turn in the game
    *   @param {dictionary} lobbyId
    *   @param {dictionary} action
    *  @emit {dictionary} gameData back to caller
    * @emit {dictionary} action back to caller
    */
    async playTurn(lobbyId, action) {
        let gameData = await this.gameStore.getGame(lobbyId);
        let gameResult = {};
        // reset the timer, if one exists
        this._resetTimer(gameData);
        // get action
        gameData = this._getActionResult(gameData, action);
        // update the game data in the database
        this.gameStore.updateGame(gameData);
        // Notify all players whose turn it is
        if (this._checkGameOver(gameData)) {
            console.log("rouletteTable: " + gameData.gameItems.globalItems.rouletteTable);
            gameResult = this._calculateWinning(gameData);
            // game is over
            this.io.to(gameData.lobbyId).emit('gameOver', {
                "gameData": gameData, 
                "playerAction": action,
                "gameResult": gameResult
            });
            
        } else {
            //game not over
            this.io.to(gameData.lobbyId).emit('playerTurn', {
                "gameData": gameData, 
                "playerAction": action
            });
        }
        
    }

    /* check if the game is over
    *   @param {dictionary} gameData
    *   @return {boolean} true if game is over, false if not
    */
    _checkGameOver(gameData) {
        // if there is no more player index, game is over
        return gameData.currentPlayerIndex == -1;
    }
    
    _delay(duration) {
        return new Promise(resolve => setTimeout(resolve, duration));
    }
}

// Basic outline for GameStore:
class GameStore {
    constructor() {
        this.client = new MongoClient('mongodb://localhost:27017', { useNewUrlParser: true, useUnifiedTopology: true });
        this.client.connect().then(() => {
            this.db = this.client.db('casinoApp');
            this.games = this.db.collection('games');
        });
    }

    /* create a new game by creating a new gameData object
    *  @param {dictionary} gameData
    *  @return {dictionary} gameData
    */
    async newGame(gameData) {
        return await this.games.insertOne(gameData);
    }

    /* using a lobbyName, set the gameData object
    *   @param (string) lobbyName
    *   @return {dictionary} gameData
    */
    async getGame(lobbyName) {
        return await this.games.findOne({ lobbyName: lobbyName });
    }
    /* save/update the gameData object, replacing the existing information
    *   @param {dictionary} gameData
    *   @return {dictionary} gameData
    */
    async updateGame(gameData) {
        return await this.games.updateOne(
            { lobbyName: gameData.lobbyId }, 
            { $set: gameData }
        );
    }
}

module.exports = GameManager;