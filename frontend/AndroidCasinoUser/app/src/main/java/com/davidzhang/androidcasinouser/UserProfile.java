package com.davidzhang.androidcasinouser;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;



import io.socket.emitter.Emitter;
import io.socket.client.IO;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

import io.socket.client.IO;
import io.socket.emitter.Emitter;
import io.socket.client.Socket;

public class UserProfile extends AppCompatActivity {

    private TextView usernameTextView, balanceTextView;
    private Button redemptionButton, adminButton;
    private boolean isAdmin = false;  // Change this based on your logic to determine if user is an admin

    private Date lastRedemptionDate;
    private User thisuser;
    // Sample variables for the logged-in user's name and balance
    private String TAG = "UserProfile";
    private Socket mSocket;
    private String username = "Retrieving...";  // TODO: Replace with actual user name from your data source
    private String balance = "Retrieving...";     // TODO: Replace with actual user balance from your data source

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Intent intent = getIntent();
        thisuser = intent.getParcelableExtra("user");
        mSocket = SocketHandler.getSocket();
        setupSocketListeners();
        mSocket.emit("retrieveAccount", thisuser.getUserId());

        usernameTextView = findViewById(R.id.usernameTextView);
        balanceTextView = findViewById(R.id.balanceTextView);
        redemptionButton = findViewById(R.id.redemptionButton);
        adminButton = findViewById(R.id.adminButton);

        // Set the username and balance for the logged-in user
        usernameTextView.setText(thisuser.getUsername());
        balanceTextView.setText("Balance: " + thisuser.getBalance());

        redemptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle redemption points logic
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       handleRedemptionPoints();
                    }
                });
            }
        });

        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Admin Panel if the user is an admin
                Intent intent = new Intent(UserProfile.this, AdminPanel.class);
                intent.putExtra("userId", thisuser.getUserId());
                startActivity(intent);
            }
        });
        Log.d(TAG, "LoginID: " + thisuser.getUserId());
        Log.d(TAG, "Defaultid: "+ getResources().getString(R.string.DefaultAdminID));
        // Sample logic to display the Admin button only if the user is an admin
        if (isAdmin || thisuser.getUserId().equals(getResources().getString(R.string.DefaultAdminID))) {
            adminButton.setVisibility(View.VISIBLE);
        }
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
                    JSONObject user = (JSONObject) args[0];
                    Log.d(TAG, "User Found: " + user.toString());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Extracting individual fields from the JSONObject
                                    String username = user.getString("username");
                                    usernameTextView.setText("Username: " + username);
                                    int balance = user.getInt("balance");
                                    balanceTextView.setText("Balance: " + balance);
                                    isAdmin = user.getBoolean("isAdmin");
                                    if (isAdmin || thisuser.getUserId().equals(getResources().getString(R.string.DefaultAdminID))) {
                                        adminButton.setVisibility(View.VISIBLE);
                                    }
                                    lastRedemptionDate = DateHandler.stringToDate(user.getString("lastRedemptionDate"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });






                    // You can now do further operations, for example, navigate to another activity
                } else {
                    // User not found in the database
                    Log.d(TAG, "User not Found, we need to retrieve again");
                }
            }
        }).on("balanceUpdate", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "received new balance details");
                if (args[0] != null) {
                    // User found in the database
                    int newbalance = (int) args[0];

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Extracting individual fields from the JSONObject

                            balanceTextView.setText("Balance: " + newbalance);
                            thisuser.setBalance(newbalance);
                            mSocket.emit("updateLastRedemptionDate", thisuser.getUserId(), DateHandler.dateToString(new Date()));

                        }
                    });
                    // You can now do further operations, for example, navigate to another activity
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UserProfile.this, "Add Points to Balance Failed! Please Try Again!", Toast.LENGTH_LONG).show();

                        }
                    });

                }
            }
        }).on("accountUpdated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // Handle the chat message here
                Log.d(TAG, "received user details");
                if (args[0] != null) {
                    // User found in the database
                    JSONObject user = (JSONObject) args[0];
                    Log.d(TAG, "User Found: " + user.toString());
                    try {
                        thisuser.setId(user.getString("_id"));
                        thisuser.setUserId(user.getString("userId"));
                        thisuser.setUsername(user.getString("username"));
                        thisuser.setBalance(user.getInt("balance"));
                        thisuser.setAdmin(user.getBoolean("isAdmin"));
                        thisuser.setChatBanned(user.getBoolean("isChatBanned"));
                        thisuser.setLastRedemptionDate(user.getString("lastRedemptionDate"));
                        lastRedemptionDate = DateHandler.stringToDate(thisuser.getLastRedemptionDate());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }

                    // You can now do further operations, for example, navigate to another activity
                } else {
                    // User not found in the database
                    Log.d(TAG, "User not Found, we need to create an account");
                    // Handle the case where the user isn't found, for example, prompt the user to sign up
                }
            }
        });
        mSocket.on(io.socket.client.Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "Socket connected");
            }
        });
        mSocket.on(io.socket.client.Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "Socket disconnected");
            }
        });
        mSocket.on(io.socket.client.Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG, "Socket connection error");
            }
        });


    }
    private void handleRedemptionPoints() {
        Log.d(TAG, "lastredemptiondate: " + lastRedemptionDate);
        if (DateHandler.isSameDay(lastRedemptionDate, new Date())){
            Toast.makeText(UserProfile.this, "You have already requested points today! Come back to redeem tomorrow!", Toast.LENGTH_LONG).show();
        }else if (lastRedemptionDate!=null){
            mSocket.emit("deposit", thisuser.getUserId(), 50);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Your code here
        mSocket = SocketHandler.getSocket();
        setupSocketListeners();
        mSocket.emit("retrieveAccount", thisuser.getUserId());
    }
    /*
    @Override
    protected void onStop() {
        super.onStop();
        // Your code here
        Log.d("AdminPanel", "turn off listeners");
        SocketHandler.turnoffAllListeners();
    }
    */

}
