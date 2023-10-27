package com.davidzhang.androidcasinouser;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class CreateAccount extends AppCompatActivity {

    private EditText accountNameEditText;
    private Socket mSocket;
    private String userId;
    private String TAG = "CreateAccount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        //SocketHandler.setSocket();
        //SocketHandler.establishConnection();
        mSocket = SocketHandler.getSocket();
        setupSocketListeners();

        Intent intent = getIntent();
        userId = intent.getStringExtra("USER_ID_KEY");
        // Initialize UI components
        accountNameEditText = findViewById(R.id.accountNameEditText);

        findViewById(R.id.createAccountButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the button click
                String accountName = accountNameEditText.getText().toString().trim();
                if (!accountName.isEmpty()) {
                    // TODO: Save the account name and proceed
                    JSONObject accountData = new JSONObject();
                    try {
                        accountData.put("userId", userId);
                        accountData.put("username", accountName);
                        accountData.put("balance", 100);
                        accountData.put("isAdmin", false);
                        accountData.put("isChatBanned", false);
                        accountData.put("lastRedemptionDate",  DateHandler.dateToString(new Date()));

                        // Emit the event to the server
                        mSocket.emit("createAccount", accountData);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Handle the error
                    }
                } else {
                    // Notify the user to enter a valid name
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateAccount.this, "Please enter a valid name!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void setupSocketListeners() {
        // Example: Listen for a chat message from the server
        mSocket.on("accountCreated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // Handle the chat message here
                Log.d(TAG, "received new created user details");
                if (args[0] != null) {
                    // User found in the database
                    JSONObject user = (JSONObject) args[0];
                    Log.d(TAG, "New user: " + user.toString());
                    // You can now do further operations, for example, navigate to another activity
                    try {
                        User currentUser = new User();
                        currentUser.setId(user.getString("_id"));
                        currentUser.setUserId(user.getString("userId"));
                        currentUser.setUsername(user.getString("username"));
                        currentUser.setBalance(user.getInt("balance"));
                        currentUser.setAdmin(user.getBoolean("isAdmin"));
                        currentUser.setChatBanned(user.getBoolean("isChatBanned"));
                        currentUser.setLastRedemptionDate(user.getString("lastRedemptionDate"));

                        Intent intent = new Intent(CreateAccount.this, Lounge.class);
                        intent.putExtra("user", currentUser);
                        startActivity(intent);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // User Create Failed
                    // prompt the user to enter the name again that does not already exist
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateAccount.this, "Username already exist, please enter a new one!", Toast.LENGTH_SHORT).show();
                        }
                    });
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
