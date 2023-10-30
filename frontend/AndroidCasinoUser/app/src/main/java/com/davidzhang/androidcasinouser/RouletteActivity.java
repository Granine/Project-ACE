package com.davidzhang.androidcasinouser;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.GameState;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class RouletteActivity extends AppCompatActivity {

    private Socket mSocket;
    private String TAG = "RouletteEvent";
    private CircularWheelView wheelView;

    private boolean currentlyAnimating = false;
    private final Queue<Runnable> requestQueue = new LinkedList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roulette);

        mSocket = SocketHandler.getSocket();

        wheelView = findViewById(R.id.wheelView);

        // Create an instance of AnimationCallback
        CircularWheelView.AnimationCallback animationCallback = new CircularWheelView.AnimationCallback() {
            @Override
            public void onAnimationFinished() {
                currentlyAnimating = false;
                runNextFunction();
            }
        };

        // Set the callback on the CircularWheelView instance
        wheelView.setAnimationCallback(animationCallback);

        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        String roomName = intent.getStringExtra("roomName");

        TextView tvLobbyName = findViewById(R.id.tvLobbyName);
        tvLobbyName.setText("Lobby Name: " + roomName);

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


        mSocket.on("rouletteResult", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args[0] != null) {
                    requestQueue.offer(new Runnable() {
                        @Override
                        public void run() {
                            currentlyAnimating = true;

                            JSONObject gameResult = (JSONObject) args[0];

                            Log.d(TAG, "Roulette Result: " + gameResult.toString());

                            int tableValue = 0;
                            try {
                                tableValue = gameResult.getInt("result");
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }

                            wheelView.spin(tableValue);
                        }
                    });
                    if (currentlyAnimating != true) {
                        runNextFunction();
                    }
                }
                else {
                    Log.e(TAG, "No data sent in rouletteResult signal.");
                }
            }
        });


        mSocket.on("gameEnded", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args[0] != null) {
                    JSONObject results = (JSONObject) args[0];
                    Log.d(TAG, "Game Results: " + results.toString());
                    requestQueue.offer(new Runnable() {
                        @Override
                        public void run() {
                            //TODO: Get results - fix this after I know how they are sent
                            double earnings = 0;
                            try {
                                earnings = results.getDouble(userName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //Send User to Results Popup
                            double finalEarnings = earnings;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showWinningsPopup(finalEarnings);
                                }
                            });

                            mSocket.emit("depositbyname", userName, finalEarnings);

                            //Return to the LobbyActivity
                            finish();
                        }
                    });
                    if (!currentlyAnimating) {
                        runNextFunction();
                    }
                }
                else {
                    Log.e(TAG, "No data sent in gameEnded signal.");
                }
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

    // Add a chat message to the chat UI
    private void addChatMessage(String message) {
        LinearLayout llChatContainer = findViewById(R.id.llChatContainer);
        TextView tvMessage = new TextView(this);
        tvMessage.setText(message);
        llChatContainer.addView(tvMessage);
    }


    private void showWinningsPopup(double winningsValue) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_layout, null);

        TextView winningsValueTextView = popupView.findViewById(R.id.winningsValueTextView);
        Button okButton = popupView.findViewById(R.id.okButton);

        winningsValueTextView.setText("$" + winningsValue); // Display the calculated winnings value

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // Set an elevation to the popup to make it appear above other views
        popupWindow.setElevation(10);

        // Show the popup centered on the screen
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // Dismiss the popup when the "OK" button is clicked
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private void runNextFunction() {
        if (!requestQueue.isEmpty()) {
            Runnable nextFunction = requestQueue.poll();
            nextFunction.run();
        }
    }
}