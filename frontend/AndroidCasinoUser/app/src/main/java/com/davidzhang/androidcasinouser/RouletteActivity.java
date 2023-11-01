package com.davidzhang.androidcasinouser;

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

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

//ChatGPT usage for this class: Partial - for things related to the wheelView and queueing requests and the popup window, and socket handling
public class RouletteActivity extends AppCompatActivity {

    private Socket mSocket;
    private String TAG = "RouletteEvent";
    private CircularWheelView wheelView;

    private boolean currentlyAnimating = false;
    private final Queue<Runnable> requestQueue = new LinkedList<>();


    @Override
    //ChatGPT usage: Partial - for things related to the wheelView and queueing requests and socket handling
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roulette);

        mSocket = SocketHandler.getSocket();

        wheelView = findViewById(R.id.wheelView);

        // Create an instance of AnimationCallback
        CircularWheelView.AnimationCallback animationCallback = new CircularWheelView.AnimationCallback() {
            @Override
            //ChatGPT usage: Yes
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

        TextView tvLobbyName = findViewById(R.id.lobbyNameLabel);
        tvLobbyName.setText("Lobby Name: " + roomName);

        mSocket.on("receiveChatMessage", new Emitter.Listener() {
            @Override
            //TODO: ChatGPT usage: ASK DINGWEN AND DAVIDZ
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
                    //TODO: ChatGPT usage: ASK DINGWEN AND DAVIDZ
                    public void run() {
                        addChatMessage(message);
                    }
                });
            }
        });

        mSocket.on("gameOver", new Emitter.Listener() {
            @Override
            //ChatGPT usage: Partial - for things related to to the popup window and queueing requests
            public void call(Object... args) {
                if (args[0] != null) {
                    JSONObject gameResults = (JSONObject) args[0];
                    Log.d(TAG, "Game Results: " + gameResults.toString());

                    requestQueue.offer(new Runnable() {
                        @Override
                        //ChatGPT usage: Partial - the call to wheelView.spin
                        public void run() {
                            currentlyAnimating = true;
                            int ballLocation = 0;
                            try {
                                JSONObject gameData = gameResults.getJSONObject("gameData");
                                JSONObject gameItems = gameData.getJSONObject("gameItems");
                                JSONObject globalItems = gameItems.getJSONObject("globalItems");

                                ballLocation = globalItems.getInt("ballLocation");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            wheelView.spin(ballLocation);
                        }
                    });
                    runNextFunction();

                    requestQueue.offer(new Runnable() {
                        @Override
                        //ChatGPT usage: Partial - The call to showWinningsPopup
                        public void run() {
                            double earnings = 0;
                            try {
                                JSONObject gameResult = gameResults.getJSONObject("gameResult");
                                earnings = gameResult.getInt(userName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //Send User to Results Popup
                            double finalEarnings = earnings;
                            runOnUiThread(new Runnable() {
                                @Override
                                //ChatGPT usage: Partial - The call to showWinningsPopup
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
            //TODO: ChatGPT usage: ASK DAVIDZ/DINGWEN
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
    //TODO: ChatGPT usage: ASK DAVIDZ AND DINGWEN
    private void addChatMessage(String message) {
        LinearLayout llChatContainer = findViewById(R.id.llChatContainer);
        TextView tvMessage = new TextView(this);
        tvMessage.setText(message);
        llChatContainer.addView(tvMessage);
    }


    //ChatGPT usage: Yes
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
            //ChatGPT usage: Yes
            public void onClick(View v) {

                popupWindow.dismiss();
            }
        });
    }

    //ChatGPT usage: Yes
    private void runNextFunction() {
        if (!requestQueue.isEmpty()) {
            Runnable nextFunction = requestQueue.poll();
            nextFunction.run();
        }
    }
}