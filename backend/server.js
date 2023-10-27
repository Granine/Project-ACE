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

const MONGO_CONNECTION_STRING = 'mongodb://localhost:27017'; // Adjust to your MongoDB connection string
const DATABASE_NAME = 'UserDB'; // Name of the database

const userStore = new UserStore(MONGO_CONNECTION_STRING, DATABASE_NAME);
userStore.connect();

const userAccount = new UserAccount(io, userStore);


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

    socket.on('disconnect', () => {
        console.log('User disconnected:', socket.id);
    });

    socket.on('deleteUser', async (userId) => {
       userAccount.deleteUser(socket, userId);
    });
    
    socket.on('deleteAllUsers', async () => {
       userAccount.deleteAllUsers(socket);
    });

    

});

server.listen(8081, () => {
    console.log('listen to port 8081');
})

