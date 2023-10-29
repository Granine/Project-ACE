const GameManager = require('../GameManager/GameManager');
const ioMock = {
  to: jest.fn().mockImplementation(() => {
      return {
          emit: jest.fn((event, data) => {
              console.log(event, data);
          })
      };
  })
};  // jest's jest.fn() method returns a new mock function



jest.mock('mongodb', () => {
  const mClient = {
    connect: jest.fn().mockImplementation(() => Promise.resolve()), // Mock promise that resolves to undefined
    db: jest.fn().mockReturnThis(),
    collection: jest.fn().mockReturnThis(),
    insertOne: jest.fn(),
    findOne: jest.fn().mockReturnValue(
      {
        lobbyId: 'abc123',
        gameType: 'roulette',
        playerList: ["playera"],
        currentPlayerIndex: 0,
        currentTurn: 0,
        betsPlaced: [],
        gameItems: {
            globalItems: {}, 
            playerItems: {}
        },
        actionHistory: []
      }
    ),
    updateOne: jest.fn()
  }
  return { MongoClient: jest.fn(() => mClient) };
});

describe('GameManager', () => {
  beforeAll(() => {
    // global setup
  });

  afterAll(() => {
    // global tear down
  });

  beforeEach(() => {
    // per test setup
    gameManager = new GameManager(ioMock);
  });

  afterEach(() => {
    // per test tear down
  });

  it('should initialize the data structure correctly', () => {
    expect(gameManager).toBeInstanceOf(GameManager);
    expect(gameManager.io).toBe(ioMock);
    expect(gameManager.gameStore).toBeDefined();
    expect(gameManager.timers).toBeDefined();
  });

  it('should create a new game correctly', async () => {
    await gameManager.startGame('abc123', 'roulette', [], []);
    expect(gameManager.io.to).toHaveBeenCalledWith('abc123');
    expect(gameManager.io.to).toHaveBeenCalledWith('abc123');
  });

  it('should play a turn', async () => {
    await gameManager.playTurn('abc123', {"usera":[1]});
  });

  // Add more tests as needed
});