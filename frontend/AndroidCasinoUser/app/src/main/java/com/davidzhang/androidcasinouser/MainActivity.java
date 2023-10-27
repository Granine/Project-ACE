package com.davidzhang.androidcasinouser;



import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import io.socket.client.IO;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    final static String TAG = "MainActivity";
    private Socket mSocket;

    private String loginuserid;
    private String username;

    private String balance;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       //connect the socket
        SocketHandler.setSocket();
        SocketHandler.establishConnection();
        mSocket = SocketHandler.getSocket();
        setupSocketListeners();

        //set up sign button
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    private void signIn() {
       Intent signInIntent = mGoogleSignInClient.getSignInIntent();
       resultLaucher.launch(signInIntent);
        //startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    ActivityResultLauncher<Intent> resultLaucher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){
                Intent intent = result.getData();
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
                handleSignInResult(task);
            }
        }
    });

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            checkUserinDB(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            checkUserinDB(null);
        }
    }
    /*
    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        checkUserinDB(account);
    }
    */

    private void checkUserinDB(GoogleSignInAccount account) {
        if (account == null){
            Log.d(TAG, "There is no user signed in!");
        }else{
            Log.d(TAG, "Pref Name: " + account.getDisplayName());
            Log.d(TAG, "Userid: " + account.getId());
            //String username = account.getDisplayName();
            loginuserid = account.getId();
            // Send token to your back-end

            Log.d(TAG, "Socket status: " + mSocket.connected());
            //if (mSocket != null && mSocket.connected()) {
            Log.d(TAG, "Socket is connected " + mSocket);
            mSocket.emit("retrieveAccount", loginuserid);

            //}else{
               // Log.d(TAG, "Socket is not connected");
           // }
            //Intent LoginIntent = new Intent(MainActivity.this, LoginDetails.class);
            //LoginIntent.putExtra("accountName", "Logged in: " + account.getGivenName() + " "+ account.getFamilyName());
            //startActivity(LoginIntent);


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
                    try {
                        User currentUser = new User();
                        currentUser.setId(user.getString("_id"));
                        currentUser.setUserId(user.getString("userId"));
                        currentUser.setUsername(user.getString("username"));
                        currentUser.setBalance(user.getInt("balance"));
                        currentUser.setAdmin(user.getBoolean("isAdmin"));
                        currentUser.setChatBanned(user.getBoolean("isChatBanned"));
                        currentUser.setLastRedemptionDate(user.getString("lastRedemptionDate"));

                        Intent intent = new Intent(MainActivity.this, Lounge.class);
                        intent.putExtra("user", currentUser);
                        startActivity(intent);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    // You can now do further operations, for example, navigate to another activity
                } else {
                    // User not found in the database
                    Log.d(TAG, "User not Found, we need to create an account");
                    Intent intent = new Intent(MainActivity.this, CreateAccount.class);
                    intent.putExtra("USER_ID_KEY", loginuserid);  // 'userId' is the variable that contains the user-id you want to pass
                    startActivity(intent);
                    // Handle the case where the user isn't found, for example, prompt the user to sign up
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
    protected void onDestroy() {
        super.onDestroy();
        SocketHandler.closeConnection();
    }
/*
    @Override
    protected void onStop() {
        super.onStop();
        // Your code here
        SocketHandler.turnoffAllListeners();
    }

 */
@Override
    protected void onStart() {
    super.onStart();
    // Your code here
    mSocket = SocketHandler.getSocket();
    setupSocketListeners();
}



}