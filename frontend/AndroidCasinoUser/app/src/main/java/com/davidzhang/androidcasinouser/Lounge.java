package com.davidzhang.androidcasinouser;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Lounge extends AppCompatActivity {

    private Button userProfileButton;
    private Button navigateToListButton;
    private String username;
    private Button signOutButton;
    private Button createLobbyButton;
    private Socket mSocket;
    private User user;

    private String TAG= "Lounge";

    private GoogleSignInClient mGoogleSignInClient;

    private String[] lobbies = {"Lobby 1", "Lobby 2", "Lobby 3"}; // Example lobby names

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lounge);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent intent = getIntent();
        user = intent.getParcelableExtra("user");

        username = user.getUsername();

        userProfileButton = findViewById(R.id.userProfileButton);
        navigateToListButton = findViewById(R.id.navigateToLobbiesButton);
        createLobbyButton = findViewById(R.id.createLobby);
        // Set the user name to the button
        userProfileButton.setText("Username: " + username); // Replace 'YourUserName' with the actual user name
        userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to UserProfileActivity
                Intent intent = new Intent(Lounge.this, UserProfile.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        signOutButton = findViewById(R.id.logoutButton);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });



        navigateToListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Lounge.this, LobbyListActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        createLobbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Lounge.this, CreateLobby.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                        Log.d(TAG, "Log Out Successfully!");
                    }
                });
        Intent intent = new Intent(Lounge.this, MainActivity.class);
        startActivity(intent);

    }

    private void setupSocketListeners() {
        // Example: Listen for a chat message from the server
        mSocket.on("userAccountDetails", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // Handle the chat message here
                Log.d(TAG, "received user details");
                if (args[0] != null) {
                    // User found in the database
                    JSONObject result = (JSONObject) args[0];
                    Log.d(TAG, "User Found: " + user.toString());
                    try {

                        user.setId(result.getString("_id"));
                        user.setUserId(result.getString("userId"));
                        user.setUsername(result.getString("username"));
                        user.setBalance(result.getInt("balance"));
                        user.setAdmin(result.getBoolean("isAdmin"));
                        user.setChatBanned(result.getBoolean("isChatBanned"));
                        user.setLastRedemptionDate(result.getString("lastRedemptionDate"));


                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    // You can now do further operations, for example, navigate to another activity
                } else {
                    // User not found in the database
                    Log.d(TAG, "User not Found");
                }
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
       mSocket.emit("retrieveAccount", user.getUserId());
    }
}
