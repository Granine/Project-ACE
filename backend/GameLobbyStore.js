const { MongoClient } = require('mongodb');

class GameLobbyStore {
    constructor(connectionString, dbName) {
        this.client = new MongoClient(connectionString);
        this.database = dbName;
        this.collection = null;
    }

    async init() {
        await this.client.connect();
        
        this.database = this.client.db(this.database);
        this.collection = this.database.collection('lobbies');
    }

    async insertLobby(roomName, gameType, maxPlayers) {
        const lobbyData = {
            roomName: roomName,
            gameType: gameType,
            gameStarted: false,
            maxPlayers: maxPlayers,
            players: {}
        };
        await this.collection.insertOne(lobbyData);
        console.log("Insert lobby successfully");
    }

    async updateLobby(roomName, updateData) {
        await this.collection.updateOne({ roomName: roomName }, { $set: updateData });
        console.log("Update lobby successfully");
    }

    async setPlayerReady(roomName, userName) {
        const key = `players.${userName}.ready`;
        await this.collection.updateOne({ roomName: roomName }, { $set: { [key]: true } });
        console.log("Set ready successfully");
    }

    async getLobby(roomName) {
        return await this.collection.findOne({ roomName: roomName });
    }

    async getAllLobby() {
        return await this.collection.find().toArray();
    }

    async deleteLobby(roomName) {
        await this.collection.deleteOne({ roomName: roomName });
    }

    async setPlayerBet(roomName, userName, bet) {
        const key = `players.${userName}.bet`;
        await this.collection.updateOne({ roomName: roomName }, { $set: { [key]: bet } });
        console.log("Set bet successfully");
    }

    async close() {
        await this.client.close();
    }
}

module.exports = GameLobbyStore;
