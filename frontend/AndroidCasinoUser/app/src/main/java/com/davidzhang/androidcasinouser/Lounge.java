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

import java.net.Socket;

public class Lounge extends AppCompatActivity {

    private Button userProfileButton;
    private Button navigateToListButton;
    private String userId;
    private String username;
    private Button signOutButton;

    private String balance;
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
        user = getIntent().getParcelableExtra("user");
        userId = user.getUserId();
        username = user.getUsername();

        userProfileButton = findViewById(R.id.userProfileButton);
        navigateToListButton = findViewById(R.id.navigateToLobbiesButton);


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
                //Intent intent = new Intent(Lounge.this, LobbyListActivity.class);
                //startActivity(intent);
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
}
