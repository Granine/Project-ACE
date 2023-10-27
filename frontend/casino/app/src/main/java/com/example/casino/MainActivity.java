package com.example.casino;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    private RecyclerView lobbiesRecyclerView;
    private List<Lobby> lobbyList = new ArrayList<>();
    private LobbyAdapter lobbyAdapter;
    private Socket mSocket;
    private JSONArray lobbies;
    private String TAG = "Event";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SocketHandler.setSocket();
        SocketHandler.establishConnection();
        mSocket = SocketHandler.getSocket();
//        mSocket.emit("createLobby", "TestRoom", "qq", "4", "User1");


        lobbiesRecyclerView = findViewById(R.id.lobbiesRecyclerView);
        lobbiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSocket.emit("getAllLobby");
        mSocket.on("AllLobby", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                lobbies = (JSONArray) args[0];
                Log.e(TAG, lobbies.toString());
                // Handle the new message
                for (int i = 0; i < lobbies.length(); i++) {
                    try {
                        JSONObject jsonObject = lobbies.getJSONObject(i);

                        String roomName = jsonObject.getString("roomName");  // Extracting a string
                        String gameType = jsonObject.getString("gameType");
                        Boolean gameStarted = jsonObject.getBoolean("gameStarted");
                        String maxPlayers = jsonObject.getString("maxPlayers");
                        int playerNumber = jsonObject.getJSONObject("players").length();
                        // Set currentUser = "Me" for now
                        lobbyList.add(new Lobby(roomName, gameType, gameStarted, maxPlayers, playerNumber, "Me"));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // This ensures that the adapter is set after the lobbyList is populated.
                        lobbyAdapter = new LobbyAdapter(MainActivity.this, lobbyList);
                        lobbiesRecyclerView.setAdapter(lobbyAdapter);
                    }
                });
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
        if(mSocket != null) {
            mSocket.disconnect();
        }
    }
}