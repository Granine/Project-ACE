const GameLobbyStore = require('./GameLobbyStore');

class GameLobby {
    constructor(roomName, gameType, maxPlayers, gameManager, gameLobbyStore, io) {
        this.roomName = roomName;
        this.gameType = gameType;
        this.maxPlayers = maxPlayers;
        this.gameManager = gameManager;  
        this.gameLobbyStore = gameLobbyStore;
        this.io = io;
        this.gameStarted = false;
        this.players = {};
        this.counter = 0;      
    }

    async init(roomName, gameType, maxPlayers) {
        await this.gameLobbyStore.insertLobby(roomName, gameType, maxPlayers);
    }

    async addPlayer(userName, bet, socket) {
        console.log(this.counter);
        console.log(this.maxPlayers);
        if(this.counter < Number(this.maxPlayers)) {
            this.counter++;

            this.players[userName] = {
                name: userName,
                ready: false,
                // Initialize bet = 0
                bet: bet,
                socketId: socket.id
            };

            this.io.to(this.roomName).emit('newPlayer', userName);

            await this.gameLobbyStore.updateLobby(this.roomName, { players: this.players });
        }
        else {
            socket.emit('PlayerExceedMax', "PlayerExceedMax");
        }
    }

    async removePlayer(userName) {
        console.log(this.counter);
        this.counter--;
        
        this.io.to(this.roomName).emit('playerLeft', userName);

        delete this.players[userName];

        await this.gameLobbyStore.updateLobby(this.roomName, { players: this.players });
        console.log("Remove Player successfully");
    }

    async setPlayerReady(userName) {
        this.players[userName].ready = true;

        this.io.to(this.roomName).emit('playerReady', userName);

        await this.gameLobbyStore.setPlayerReady(this.roomName, userName);

        if (Object.values(this.players).every(p => p.ready)) {
            this.startGame();
        }
    }

    async setPlayerBet(roomName, userName, bet) {
        this.players[userName].bet = bet;

        this.io.to(this.roomName).emit('setBet', userName);
        
        await this.gameLobbyStore.setPlayerBet(this.roomName, userName, bet);
    }

    async startGame() {
        if (this.gameManager) {
            this.gameStarted = true;

            this.io.to(this.roomName).emit('gameStarted');

            const readyPlayers = Object.keys(this.players).filter(userName => this.players[userName].ready);
            const bets = readyPlayers.map(userName => this.players[userName].bet);
            await this.gameManager.startGame(this.roomName, readyPlayers, bets, this.gameType);

            for (let userName of readyPlayers) {
                this.players[userName].ready = false;
            }

            await this.gameLobbyStore.updateLobby(this.roomName, { players: this.players, gameStarted: this.gameStarted });
        } else {
            console.log("GameManager not initialized.");
        }
    }

    async getAllLobby() {
        return await this.gameLobbyStore.getAllLobby();
    }

    async deleteLobby(roomName) {
        await this.gameLobbyStore.deleteLobby(roomName);
    }

    registerSocketEvents(socket) {
        socket.on('setReady', async (userName) => {
            console.log("User ready");
            await this.setPlayerReady(userName);
        });

        socket.join(this.roomName);
    }
}

module.exports = GameLobby;
