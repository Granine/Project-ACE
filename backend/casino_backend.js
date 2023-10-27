const express = require('express');
const http = require('http');
const socketIo = require('socket.io');

const app = express();
app.use(express.static(__dirname));
const server = http.createServer(app);
const io = socketIo(server);

const GameLobby = require('./GameLobby');
const GameLobbyStore = require('./GameLobbyStore');
const LiveChat = require('./LiveChat');

const gameLobbyStore = new GameLobbyStore();
const liveChat = new LiveChat(io);

(async () => {
    await gameLobbyStore.init();
})();

const gameLobbies = {};

io.on('connection', (socket) => {
    console.log('User connected');

    socket.on('createLobby', async (roomName, gameType, maxPlayers, userName) => {
        console.log("Lobby created");
        if(!gameLobbies[roomName]) {
            // Set GameManager = null for now
            const lobby = new GameLobby(roomName, gameType, maxPlayers, null, gameLobbyStore, io);
            await lobby.init(roomName, gameType, maxPlayers);
            gameLobbies[roomName] = lobby;
            // Set bet = 0 initially
            gameLobbies[roomName].addPlayer(userName, 0, socket);
            gameLobbies[roomName].registerSocketEvents(socket);
            liveChat.registerSocketEvents(socket, roomName, userName);
        }
        else {
            socket.emit('roomAlreadyExist', "Room already exist");
        }

    });

    socket.on('joinLobby', (roomName, userName) => {
        console.log("User joined");
        if(gameLobbies[roomName]) {
            // Set bet = 0 initially
            gameLobbies[roomName].addPlayer(userName, 0, socket);
            gameLobbies[roomName].registerSocketEvents(socket);
            liveChat.registerSocketEvents(socket, roomName, userName);
        }
        else {
            socket.emit('roomDoesNot', "Room does not exist");
        }
    });

    socket.on('getAllLobby', async () => {
        var myLobbies = await gameLobbyStore.getAllLobby();
        socket.emit('AllLobby', myLobbies);
    });

    socket.on('setBet', async(roomName, userName, bet) => {
        await gameLobbies[roomName].setPlayerBet(roomName, userName, bet);
    })

    socket.on('leaveLobby', () => {
        console.log("User left lobby");
        for(let roomName in gameLobbies) {
            for(let userName in gameLobbies[roomName].players) {
                if(gameLobbies[roomName].players[userName].socketId === socket.id) {
                    gameLobbies[roomName].removePlayer(userName);
                    break;
                }
            }
        }
    });

    socket.on('disconnect', () => {
        console.log("User disconnected");
        for(let roomName in gameLobbies) {
            for(let userName in gameLobbies[roomName].players) {
                if(gameLobbies[roomName].players[userName].socketId === socket.id) {
                    gameLobbies[roomName].removePlayer(userName);
                    break;
                }
            }
        }
    });
});

server.listen(3000, () => {
    console.log('Server started on port 3000');
});
