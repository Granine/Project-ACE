package com.davidzhang.androidcasinouser;

import android.app.GameState;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
public class LobbyActivity extends AppCompatActivity {
    private Socket mSocket;
    private String TAG = "LEvent";

    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        mSocket = SocketHandler.getSocket();

        Intent intent = getIntent();
        String roomName = intent.getStringExtra("roomName");
        String gameType = intent.getStringExtra("gameType");
        Boolean gameStarted = intent.getBooleanExtra("gameStarted", false); // default value is false
        String maxPlayer = intent.getStringExtra("maxPlayer");
        int playNumber = intent.getIntExtra("playNumber", 0);
        User currentPlayer = intent.getParcelableExtra("currentUser");

        TextView tvLobbyName = findViewById(R.id.tvLobbyName);
        TextView tvGameType = findViewById(R.id.tvGameType);
        TextView tvPlayersReady = findViewById(R.id.tvPlayersReady);

        tvLobbyName.setText("Lobby Name: " + roomName);
        tvGameType.setText("Game Type: " + gameType);
        tvPlayersReady.setText("Players Ready: " + 0 + "/" + maxPlayer);

        // Button: Leave Lobby
        Button btnLeaveLobby = findViewById(R.id.btnLeaveLobby);
        btnLeaveLobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.emit("leaveLobby");
                finish();
            }
        });

        // Button: Place Bets and Ready Up
        Button btnPlaceBetsReadyUp = findViewById(R.id.btnPlaceBetsReadyUp);
        final EditText etBet = findViewById(R.id.etPlaceBet);
        btnPlaceBetsReadyUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String betText = etBet.getText().toString();

                if (betText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Bet cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        int bet = Integer.parseInt(betText);
                        if(currentPlayer.getBalance() < bet) {
                            Toast.makeText(getApplicationContext(), "Bet cannot be greater than balance", Toast.LENGTH_SHORT).show();
                        } else {
                            mSocket.emit("setBet", roomName, currentPlayer.getUsername(), bet);
                            mSocket.emit("setReady", currentPlayer.getUsername());
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getApplicationContext(), "Invalid bet. Please enter a valid integer", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Update the player ready UI
        mSocket.on("playerReady", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvPlayersReady.setText("Players Ready: " + ++counter + "/" + maxPlayer);
                    }
                });
            }
        });

        mSocket.on("receiveChatMessage", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                Log.e(TAG, data.toString());
                String user = "";
                String text = "";

                try{
                    user = data.getString("user");
                    text = data.getString("text");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                String message = user + " : " + text;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addChatMessage(message);
                    }
                });
            }
        });


        mSocket.on("gameStarted", new Emitter.Listener() {
           @Override
           //ChatGPT usage: No
           public void call(Object... args) {
               Log.d(TAG, "New Game Signal: " + gameType);

               Intent gameIntent = null;
               if (gameType == "roulette") {
                   gameIntent = new Intent(LobbyActivity.this, RouletteActivity.class);
               } else if (gameType == "baccarat") {
                   gameIntent = new Intent(LobbyActivity.this, BaccaratActivity.class);
               } else if (gameType == "blackjack") {
                   gameIntent = new Intent(LobbyActivity.this, BlackJackActivity.class);
               } else {
                   Log.e(TAG, "No matching game type to: " + gameType);
                   return;
               }
               gameIntent.putExtra("userName", currentPlayer.getUsername());
               gameIntent.putExtra("roomName", roomName);
               startActivity(gameIntent);
           }
        });

        // Button: Send for Chat
        Button btnSend = findViewById(R.id.btnSend);
        final EditText etEnterMessage = findViewById(R.id.etEnterMessage);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = etEnterMessage.getText().toString();
                if (!message.isEmpty()) {
                    mSocket.emit("sendChatMessage", message);
                    etEnterMessage.setText(""); // Clear the message input
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //TODO do whatever we may need here get the lobby ready again (Eg resetting users to unready if necessary)
        //counter = 0;
    }

    // Add a chat message to the chat UI
    private void addChatMessage(String message) {
        LinearLayout llChatContainer = findViewById(R.id.llChatContainer);
        TextView tvMessage = new TextView(this);
        tvMessage.setText(message);
        llChatContainer.addView(tvMessage);
    }
}
