package com.davidzhang.androidcasinouser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class CreateLobby extends AppCompatActivity {

    private EditText roomNameEditText;
    private EditText maxPlayersEditText;
    private Spinner gameTypeSpinner;
    private Button createLobbyButton;
    private Socket mSocket;
    private User user;
    private String TAG = "CreateLobby";

    InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_lobby);
        //SocketHandler.setSocket();
        //SocketHandler.establishConnection();
        Intent intent = getIntent();
        user = intent.getParcelableExtra("user");
        mSocket = SocketHandler.getSocket();
        setupSocketListeners();


        // Initialize UI components
        roomNameEditText = findViewById(R.id.roomNameEditText);
        maxPlayersEditText = findViewById(R.id.maxPlayersEditText);
        maxPlayersEditText.setFilters(new InputFilter[]{filter});
        gameTypeSpinner = findViewById(R.id.gameTypeSpinner);
        createLobbyButton = findViewById(R.id.createLobbyButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.game_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gameTypeSpinner.setAdapter(adapter);

        createLobbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomName = roomNameEditText.getText().toString().trim();
                String gameType = gameTypeSpinner.getSelectedItem().toString();
                String maxPlayers = maxPlayersEditText.getText().toString().trim();

                // Now you can use these details to create a lobby (e.g., send to a server)
                if (!roomName.isEmpty() && !gameType .isEmpty() && Integer.parseInt(maxPlayers) > 0) {
                    //create Lobby
                    mSocket.emit("createLobby", roomName, gameType, maxPlayers, user.getUsername());

                } else {
                    // Notify the user to enter a valid name
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateLobby.this, "Please enter valid info!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void setupSocketListeners() {
        // Example: Listen for a chat message from the server
        mSocket.on("roomAlreadyExist", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                Log.d(TAG, "Room already exist, need to create again");
                if (args[0] != null) {

                    String msg  = (String) args[0];
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateLobby.this, "Room already exist, please use a new name!", Toast.LENGTH_SHORT).show();
                            roomNameEditText.setText("");
                        }
                    });
                } else {
                    // User Create Failed
                    // prompt the user to enter the name again that does not already exist
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateLobby.this, "Error Occurred in Retrieving from LobbylistDB", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).on("lobbyCreated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String msg  = (String) args[0];
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(CreateLobby.this, msg + "\nFind your Lobby in the Lobby list!", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Your code to be executed after the Toast disappears
                                Intent intent = new Intent(CreateLobby.this, Lounge.class);
                                intent.putExtra("user", user);
                                startActivity(intent);
                            }
                        }, 2000);


                    }
                });
            }
        });
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "Socket connected");
            }
        });
        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "Socket disconnected");
            }
        });
        mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG, "Socket connection error");
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        // Your code here
        mSocket = SocketHandler.getSocket();
        setupSocketListeners();
    }
    /*
    @Override
    protected void onStop() {
        super.onStop();
        // Your code here
        SocketHandler.turnoffAllListeners();
    }

     */
}