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
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

//ChatGPT usage for this class: Partial - things related to queueing requests and popup windows, and socket handling
public class BaccaratActivity extends AppCompatActivity {
    private Socket mSocket;
    private String TAG = "BaccaratEvent";
    private boolean currentlyAnimating = false;
    private final Queue<Runnable> requestQueue = new LinkedList<>();
    private TextView turnIndicator, playerScoreLabel, dealerScoreLabel, lobbyName;
    private int[] playerCardVals = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private int[] dealerCardVals = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private Map<String, Integer> cardValues = new HashMap<>() {{
        put("ace", 1); //or 11
        put("two", 2);
        put("three", 3);
        put("four", 4);
        put("five", 5);
        put("six", 6);
        put("seven", 7);
        put("eight", 8);
        put("nine", 9);
        put("ten", 0);
        put("jack", 0);
        put("queen", 0);
        put("king", 0);
    }};
    private TextView[] playerCardItems = new TextView[6];
    private TextView[] dealerCardItems = new TextView[6];
    private int dealerCardIdx = 0;
    private int playerCardIdx = 0;

    @Override
    //ChatGPT usage: Partial - things related to queueing requests and popup windows, and socket handling
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baccarat);

        mSocket = SocketHandler.getSocket();

        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        String roomName = intent.getStringExtra("roomName");

        TextView tvLobbyName = findViewById(R.id.tvLobbyName);
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

        //TODO MAKE SURE THIS ALIGNS WITH GUAN'S BACKEND
        mSocket.on("gameResults", new Emitter.Listener() {
            @Override
            //ChatGPT usage: partial - for things related to queueing requests.
            public void call(Object... args) {
                if (args[0] != null) {
                    requestQueue.offer(new Runnable() {
                        @Override
                        //ChatGPT usage: No
                        public void run() {
                            new Thread(() -> {
                                currentlyAnimating = true;

                                JSONObject gameResult = (JSONObject) args[0];
                                Log.d(TAG, "Baccarat Result: " + gameResult.toString());

                                //TODO MAKE SURE THIS WORKS WITH GUAN'S BACKEND - WILL NEED CHANGES
                                JSONArray tableCards;
                                JSONArray dealerCards;
                                try {
                                    tableCards = gameResult.getJSONArray("PlayerCards");
                                    dealerCards = gameResult.getJSONArray("DealerCards");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }

                                //TODO DEAL OUT CARDS: 1 TO PLAYER, 1 TO DEALER, 1 TO PLAYER, 1 TO DEALER, REST OF PLAYERS, REST OF DEALERS
                                String val;
                                String suit;

                                /**
                                 try {
                                    val = tableCards[0].getString("value"); // fix this
                                    suit = tableCards[0].getString("suit");
                                 } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                 }
                                 dealNewPlayerCard(val, suit);
                                 updateScores()
                                 */

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                /**
                                 try {
                                    val = dealerCards[0].getString("value"); // fix this
                                    suit = dealerCards[0].getString("suit");
                                 } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                 }
                                 dealNewDealerCard(val, suit);
                                 updateScores()
                                 */

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                /**
                                 try {
                                    val = tableCards[1].getString("value"); // fix this
                                    suit = tableCards[1].getString("suit");
                                 } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                 }
                                 dealNewPlayerCard(val, suit);
                                 updateScores()
                                 */

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                /**
                                 try {
                                    val = dealerCards[0].getString("value"); // fix this
                                    suit = dealerCards[0].getString("suit");
                                 } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                 }
                                 dealNewDealerCard(val, suit);
                                 updateScores()
                                 */

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                //Deal remaining player cards
                                /**
                                 for (int i = playerCardIdx; i < 21; i++) {
                                    try {
                                        val = tableCards[i].getString("value"); // fix this
                                        suit = tableCards[i].getString("suit");
                                    } catch (JSONException e) {
                                        break;
                                    }
                                    dealNewPlayerCard(val, suit);
                                    updateScores()
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                 }

                                 //Deal remaining dealer cards

                                 for (int i = dealerCardIdx; i < 21; i++) {
                                    try {
                                        val = dealerCards[i].getString("value"); // fix this
                                        suit = dealerCards[i].getString("suit");
                                    } catch (JSONException e) {
                                        break;
                                    }
                                    dealNewDealerCard(val, suit);
                                    updateScores()
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                 }
                                 */

                                //Some sleep time to ensure the popup window doesn't come too soon.
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                currentlyAnimating = false;
                                runNextFunction();
                            }).start();
                        }
                    });
                    if (currentlyAnimating != true) {
                        runNextFunction();
                    }
                }
                else {
                    Log.e(TAG, "No data sent in gameResults signal.");
                }
            }
        });


        //TODO MAKE SURE THIS ALIGNS WITH GUAN'S BACKEND
        mSocket.on("gameEnded", new Emitter.Listener() {
            @Override
            //ChatGPT usage: Partial - for things related to to the popup window and queueing requests
            public void call(Object... args) {
                if (args[0] != null) {
                    JSONObject results = (JSONObject) args[0];
                    Log.d(TAG, "Game Results: " + results.toString());
                    requestQueue.offer(new Runnable() {
                        @Override
                        //ChatGPT usage: Partial - The call to showWinningsPopup
                        public void run() {
                            //TODO: MAKE SURE THIS WORKS WITH GUAN'S BACKEND - WILL NEED CHANGES
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
            public void onClick(View view) {
                //TODO: ChatGPT usage: ASK DAVIDZ AND DINGWEN
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
    private void runNextFunction() {
        if (!requestQueue.isEmpty()) {
            Runnable nextFunction = requestQueue.poll();
            nextFunction.run();
        }
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

    //ChatGPT usage: No
    private void dealNewDealerCard(String value, String suit) {
        if (dealerCardIdx <= 5) {
            dealerCardItems[dealerCardIdx].setText(value+ "\n" + suit);
        } else {
            Toast.makeText(getApplicationContext(), "Too many cards to display", Toast.LENGTH_SHORT).show();
        }
        dealerCardVals[dealerCardIdx] = cardValues.get(value);
        dealerCardIdx++;
    }

    //ChatGPT usage: No
    private void dealNewPlayerCard(String value, String suit) {
        if (playerCardIdx <= 5) {
            playerCardItems[playerCardIdx].setText(value+ "\n" + suit);
        } else {
            Toast.makeText(getApplicationContext(), "Too many cards to display", Toast.LENGTH_SHORT).show();
        }
        playerCardVals[playerCardIdx] = cardValues.get(value);
        playerCardIdx++;
    }

    //ChatGPT usage: No
    private void updateScores() {
        int playerScore = 0;
        int dealerScore = 0;

        for (int i = 0; i < playerCardIdx; i++) {
            playerScore += playerCardVals[i];
        }

        for (int j = 0; j < dealerCardIdx; j++) {
            dealerScore += dealerCardVals[j];
        }

        playerScore = playerScore % 10;
        dealerScore = dealerScore % 10;

        dealerScoreLabel.setText(String.valueOf(dealerScore));
        playerScoreLabel.setText(String.valueOf(playerScore));
    }
}