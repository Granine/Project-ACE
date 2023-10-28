package com.davidzhang.androidcasinouser;

public class Lobby {
    private String roomName;
    private String gameType;
    private Boolean gameStarted;
    private String maxPlayer;
    private int playNumber;
    private User currentUser;

    public Lobby(String roomName, String gameType, Boolean gameStarted, String maxPlayer, int playNumber, User currentUser) {
        this.roomName = roomName;
        this.gameType = gameType;
        this.gameStarted = gameStarted;
        this.maxPlayer = maxPlayer;
        this.playNumber = playNumber;
        this.currentUser = currentUser;
    }

    public String getName() {
        return roomName;
    }

    public String getGameType() {
        return gameType;
    }

    public Boolean getGameStarted() {return gameStarted;}

    public String getMaxPlayer() {return maxPlayer;}

    public int getPlayNumber() {return playNumber;}

    public User getCurrentUser() {
        return currentUser;
    }
}
