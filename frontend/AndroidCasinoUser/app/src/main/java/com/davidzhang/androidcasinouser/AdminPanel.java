package com.davidzhang.androidcasinouser;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class AdminPanel extends AppCompatActivity {

    private EditText editUserNameEditText, pointValueEditText;
    private Button banFromChatButton, setAsAdminButton, addPointsButton;

    private String TAG = "AdminPanel";
    private Socket mSocket;

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
        setContentView(R.layout.activity_admin_panel);
        mSocket = SocketHandler.getSocket();
        setupSocketListeners();
        editUserNameEditText = findViewById(R.id.editUserNameEditText);
        pointValueEditText = findViewById(R.id.pointValueEditText);
        pointValueEditText.setFilters(new InputFilter[]{filter});
        banFromChatButton = findViewById(R.id.banFromChatButton);
        setAsAdminButton = findViewById(R.id.setAsAdminButton);
        addPointsButton = findViewById(R.id.addPointsButton);

        banFromChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle ban from chat logic
                mSocket.emit("updateChatBanned", editUserNameEditText.getText().toString(), true);
            }
        });

        setAsAdminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle set as admin logic
                mSocket.emit("updateAdminStatus", editUserNameEditText.getText().toString(), true);
            }
        });

        addPointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle adding points logic
                String value = pointValueEditText.getText().toString();
                int intValue;
                try {
                    intValue = Integer.parseInt(value);
                    Log.d(TAG, "Entered user Name: " + editUserNameEditText.getText().toString());
                    mSocket.emit("depositbyname", editUserNameEditText.getText().toString(), intValue);
                } catch (NumberFormatException e) {
                    // Handle error, such as showing a message to the user
                }

            }
        });
    }


    private void setupSocketListeners() {
        // Example: Listen for a chat message from the server
        mSocket.on("balanceUpdate", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "received new balance details");
                if (args[0] != null) {
                    // User found in the database
                    int newbalance = (int) args[0];

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(AdminPanel.this, "Updated Balance for " + editUserNameEditText.getText().toString() + " is "+ newbalance, Toast.LENGTH_LONG).show();
                        }
                    });
                    // You can now do further operations, for example, navigate to another activity
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(AdminPanel.this, "Add Balance for " + editUserNameEditText.getText().toString() + " Failed! Please Try Again!", Toast.LENGTH_LONG).show();

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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AdminPanel.this, "Updated User Info Successfully for " + editUserNameEditText.getText().toString(), Toast.LENGTH_LONG).show();

                        }
                    });

                    // You can now do further operations, for example, navigate to another activity
                } else {
                    // User not found in the database
                    Log.d(TAG, "Edited User Failed! Please Try Again");

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
