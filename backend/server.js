const express = require('express');
const http = require('http');
const socketIo = require('socket.io');

const app = express();
app.use(express.static(__dirname));
const server = http.createServer(app);
const io = socketIo(server);
const cors = require('cors');
app.use(cors());

const UserStore = require('./UserStore');
const UserAccount = require('./UserAccount'); // Assuming you also export the UserAccount class
const GameLobby = require('./GameLobby');
const GameLobbyStore = require('./GameLobbyStore');
const LiveChat = require('./LiveChat');

const MONGO_CONNECTION_STRING = 'mongodb://localhost:27017'; // Adjust to your MongoDB connection string
const USERDB_NAME = 'UserDB'; // Name of the database
const LOBBYDB_NAME = 'gameLobbyDB'

const userStore = new UserStore(MONGO_CONNECTION_STRING, USERDB_NAME);

(async () => {
    await userStore.connect();
})();

const userAccount = new UserAccount(io, userStore);

const gameLobbyStore = new GameLobbyStore(MONGO_CONNECTION_STRING, LOBBYDB_NAME);
const gameLobbies = {};

(async () => {
    await gameLobbyStore.init();
})();

io.on('connection', (socket) => {
    console.log('A user connected:', socket.id);

    socket.on('retrieveAccount', (userId) => {
        userAccount.retrieveAccount(socket, userId);
    });

    socket.on('updateAccount', (userInfo) => {
        userAccount.updateAccount(socket, userInfo);
    });
    
    socket.on('createAccount', (userInfo) => {
        userAccount.createAccount(socket, userInfo);
    });

    socket.on('updateName', (userId, newname) => {
        userAccount.updateName(socket, userId, newname);
    });

    socket.on('updateAdminStatus', (username, AdminStatus) => {
        userAccount.updateAdminStatus(socket, username, AdminStatus);
    });

    socket.on('updateChatBanned', (username, ChatBannedStatus) => {
        userAccount.updateChatBanned(socket, username, ChatBannedStatus);
    });

    socket.on('updateLastRedemptionDate', (userID, rDate) => {
        userAccount.updateLastRedemptionDate(socket, userID, rDate);
    });

    socket.on('deposit', (userId, amount) => {
        userAccount.deposit(socket, userId, amount);
    });

    socket.on('depositbyname', (username, amount) =>{
        userAccount.depositbyname(socket, username, amount);
    })

    socket.on('withdraw', (userId, amount) => {
        userAccount.withdraw(socket, userId, amount);
    });

    // socket.on('disconnect', () => {
    //     console.log('User disconnected:', socket.id);
    // });

    socket.on('deleteUser', async (userId) => {
       userAccount.deleteUser(socket, userId);
    });
    
    socket.on('deleteAllUsers', async () => {
       userAccount.deleteAllUsers(socket);
    });

    socket.on('createLobby', async (roomName, gameType, maxPlayers, userName) => {
        console.log("Lobby created");
        if(!gameLobbies[roomName]) {
            // Set GameManager = null for now
            const lobby = new GameLobby(roomName, gameType, maxPlayers, null, gameLobbyStore, io);
            await lobby.init(roomName, gameType, maxPlayers);
            gameLobbies[roomName] = lobby;
            // Set bet = 0 initially
            var success = await gameLobbies[roomName].addPlayer(userName, 0, socket);
            if(success === true) {
                gameLobbies[roomName].registerSocketEvents(socket);
            }
        }
        else {
            socket.emit('roomAlreadyExist', "Room already exist");
        }

    });

    socket.on('joinLobby', async (roomName, userName) => {
        console.log("User joined");
        if(gameLobbies[roomName]) {
            // Set bet = 0 initially
            var success = await gameLobbies[roomName].addPlayer(userName, 0, socket);
            if(success === true) {
                gameLobbies[roomName].registerSocketEvents(socket);
            }
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
    console.log('listen to port 3000');
})

