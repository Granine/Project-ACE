# Project-ACE
## Module 1: Live Chats
Purpose: This module is dedicated to the management of chat functionality within the application. It enables users to connect to chat rooms for communication with other players while ensuring vigilant moderation to prevent inappropriate content and misuse.

## LiveChat Class:Represents the chat object for a single room, tracks and manages all message in this room
addMessage(userName: string, message: string): boolean
  Adds a new message to the chat (someone posted a new message).
  Parameters:
    - userName (string): The username of the user sending the message.
    - message (string): The content of the message.
    - Returns: true if the message is added successfully, false otherwise.
notifyUsers(message: Message): void
  Notifies all users in the chat that a new message has been sent, so their interface update to take in the new message
  Parameters:
    - message (Message): The message updated.
    - Returns: None
getMessages(): Message[]
  Retrieves all chat messages for the current LiveChat instance.
Parameters: None
    - Returns: An array of message objects for the chat.
    - Message Class: Internal class to represent message object, no external interface

## Module 2: Users
Database: UserDB
External Component: Google’s Authentication service through their api that authenticates the user through their google account and return relative information like name, GUID, etc
Purpose: This module is responsible for user account management. Users are required to have an account to track usernames and balances.

### UserAccount Class: Manages a single user account.
signIn(googleAccountDetail: GoogleAccountDetail): UserAccountDetails
Description: After the user logs in with their google account, this method is called with the obtained information related to the user.
Parameters:
    - googleAccountDetail (GoogleAccountDetail): An object formulated based on normalized google account information after user logs in to google.
    - Returns: A UserAccountDetails object containing the requested account details needed by the front end to let them join lobbies and show info, if login fails, returns error flags.
updateBalance(userid: string, balance: float): boolean
  Description: Updates the user's account balance with the provided value after a game concludes and the front-end is notified of winnings. (This function is intended for backend use, there will be security measure on top of this function)
  Parameters:
    - userid (string): The user's unique identifier.
    - balance (float): The new account balance.
    - Returns: true if the balance update is successful, false otherwise.
redeemPoints(userid: string): boolean
  Description: Allows users to redeem daily points. Grants 50 points if the user hasn't already claimed them for the day.
  Parameters:
    - userid (string): The user's unique identifier.
    - Returns: true if the points redemption is successful, false if the user has already redeemed points for the day.
removeAccount(userid: string): boolean
  Description: When provokes, will delete user account and wipe all user associated data
Parameters:
    - userid (string): The user's unique identifier.
    - Returns: true if the account is successfully removed, false otherwise.
fetchAccountDetails(userid: string): UserAccountDetails
  Description: Retrieves and provides user account details for display, such as balance and username.
  Parameters:
    - userid (string): The user's unique identifier.
    - Returns: A UserAccountDetails object containing the requested account details needed by the front end to let them join lobbies and show info.
newAccount(googleAccountDetail: GoogleAccountDetail): UserAccountDetails
  Description: Similar as signin(), except that it will create a new user instance in the userStore database. The instance will be initialized with default information and those retrieved from google account object.
  Parameters:
    - googleAccountDetail (GoogleAccountDetai): object formulated based on normalized google account information after user logs in to google.
    - Returns: A UserAccountDetails object containing the requested account details needed by the front end to let them join lobbies and show info, if login fails, returns error flags.
updateAccount(userid: string, newDetails: UserAccountDetails): boolean
  Description: Updates user information, excluding the account balance. This function allows users to modify their account details, such as username, email, or other relevant information.
Parameters:
    - userid (string): The user's unique identifier.
newDetails (UserAccountDetails): An object containing the updated user information, to be updated in the DB.
    - Returns: true if the user's account information is successfully updated, false if the operation fails (e.g., due to invalid data or the user not being found).
banAccount(userName: string): boolean
  Description: Bans a user from chatting in live chat windows. This can be called by a user with admin privileges
  Parameters:
    - userName (string): The user's username.
    - Returns: true if the user's account was successfully banned, and false otherwise

### UserStore Class: Database management and interaction, no external interface

## Module 3: Game Lobbies
Purpose: This module allows players to create and join game lobbies, where they place bets and ready up for games.

### GameLobby Class: An instance of a game lobby
addUser(userName: string, roomName: string): boolean
  Description: Adds a new user to the game lobby.
  Parameters:
  userName (string): The username of the user to be added.
    - roomName (string): The room name for the lobby
    - Returns: true if the user is successfully added, false otherwise
    - Notification: Notifies all users currently in the room using the room's socket.
leaveRoom(userName: string, roomName: string): boolean
  Description: Called when a user leaves the Game Room. If all players have left, the GameLobby is automatically closed and removed (so no cleanup function interface is required)
  Parameters:
    - userName (string): The username of the user leaving the room.
    - roomName (string): The room name for the lobby
    - Returns: true if room is the user is successfully removed from the room, false otherwise
    - Notification: Notifies all users in the room using the room's socket.
setReady(userName: string, bet: float, betType: string, roomName: string):boolean
  Description: Called when a user is ready for the game and has placed a valid bet.
  Parameters:
    - userName (string): The username of the user getting ready.
    - bet (float): The amount of the bet.
    - betType (string): The type of bet.
    - roomName (string): The room name for the lobby
    - Returns: true if the user is set to ready, false otherwise
Notification: Notifies all users in the room using the room's socket.
  Description: Notifies users that the game has started. This function interfaces with the front-end.

### GameStore Class: Manages all the gamelobby objects
addLobby(userName: string, gameType: string, maxPlayers: int): boolean
  Description: Creates a new lobby with the provided information. This function initializes a lobby with the selected game type and assigns the first user.
  Parameters:
    - userName (string): The username of the user who is creating the new lobby.
    - gameType (string): One of the supported games in the casino
    - maxPlayers (int): the max number of player the room can have
    - Returns: true if the room is created and the user is successfully added, false otherwise
getLobbies(count: number, seed: string, filter: object, startFrom: number): GameLobby[]
  Description: Returns a list of game lobbies based on the specified criteria.
  Parameters:
    - count (number): The number of lobbies to retrieve.
    - seed (string): A seed string for randomization or filtering.
    - filter (object): An object specifying filtering criteria (if needed).
    - startFrom (number): The starting point or offset for retrieving lobbies.
    - Returns: An array of GameLobby objects, representing game lobbies that match the criteria, each containing information regarding the lobby.
getLobby(roomID: string): GameLobby | null
  Description: Retrieves a specific lobby based on its unique roomID.
  Parameters:
    - roomID (string): The unique identifier of the lobby to retrieve.
    - Returns: A GameLobby object representing the lobby if found, or null if the lobby with the specified roomID does not exist or error.

## Module 4: Games
External Component: A Random Number Generator (RNG) that securely generates random numbers. We will likely use Random.org’s RNG service through its api
Purpose: This module governs the overall gameplay experience. Once all players are ready in the game lobby, it orchestrates the seamless transition of players into the chosen game.

### GameManager Class: Manages the flow of a single game.
Before defining these interfaces, we will define how users will be passed into the game.
constructor(players: Player[], gameType: string)
  Description: The constructor is called by the GameLobby automatically when it's time to start a game. It initializes the   GameManager class with a list of players.
  Parameters:
    - players (Player[]): An array of player objects representing participants in the game, including user information such as userName, bet amount etc.
    - gameType (String): the game type to be played in this room.
playTurn(UserID: string, action: string[])
  Description: Called by the front end when a user responds and plays their turn after being prompted. The action       parameter represents the user's choice on the current turn.
  Parameters:
    - UserID: The user that made the move this turn.
    - action (String[]): the actions user took.
startGame(players: Player[])
  Description: This function is called by the GameLobby class when it's time to start the game. It initiates the game, and   typically, the constructor of the GameManager class is automatically invoked to set up the game.
  Parameter
    -  players (Player[]): An array of player objects representing participants in the game, also contain user information like user ID, bets placed for this game etc.
    - Card Class: A sample object to represent one game asset, in this case, a playing card.
