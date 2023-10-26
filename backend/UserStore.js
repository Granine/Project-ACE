
const { MongoClient } = require('mongodb');

class UserStore {
    constructor(connectionString, dbName) {
        this.client = new MongoClient(connectionString);
        this.dbName = dbName;
    }

    async connect() {
        await this.client.connect();
        this.db = this.client.db(this.dbName);
        this.usersCollection = this.db.collection('users');
        // Create unique index for userId
        //await this.usersCollection.createIndex({ "userId": 1 }, { unique: true });
    
        // Create unique index for username (or name, depending on your schema)
        //await this.usersCollection.createIndex({ "username": 1 }, { unique: true });
    }
    

    async getUser(userId) {
        return await this.usersCollection.findOne({ userId: userId });
    }
    
    async addUser(userDoc) {
        // Check if username already exists
        const existingUser = await this.usersCollection.findOne({ username: userDoc.username });
        if (existingUser) {
            return null
        }
    
        // Generate a unique userId (using MongoDB's ObjectId as an example)
        // If you're using another strategy for userId, adjust this part accordingly
        // userDoc.userId = userDoc._id || new require('mongodb').ObjectId().toString();
    
        // Insert the user
        const result = await this.usersCollection.insertOne(userDoc);
    
        // Fetch and return the inserted user
        return await this.usersCollection.findOne({ _id: result.insertedId });
    }
    

    async updateUser(userId, updateDoc) {
        // Only allow certain fields to be updated
        const result = await this.usersCollection.updateOne({ userId: userId }, { $set: updateDoc });
        // Return the updated document
        return await this.getUser(userId);
    }


    async deleteUser(userId) {
        return await this.usersCollection.deleteOne({ userId: userId });
        
    }


    async deleteAllUsers() {
        return await this.usersCollection.deleteMany({});
    
    }




    async close() {
        await this.client.close();
    }
}

module.exports = UserStore;
