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
import org.json.JSONException;
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
    private Map<String, Integer> cardValues = new HashMap<String, Integer>() {{
        put("Ace", 1); //or 11
        put("2", 2);
        put("3", 3);
        put("4", 4);
        put("5", 5);
        put("6", 6);
        put("7", 7);
        put("8", 8);
        put("9", 9);
        put("10", 0);
        put("Jack", 0);
        put("Queen", 0);
        put("King", 0);
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

        mSocket.on("gameEnded", new Emitter.Listener() {
            @Override
            //ChatGPT usage: Partial - for things related to to the popup window and queueing requests
            public void call(Object... args) {
                if (args[0] != null) {
                    JSONObject gameResults = (JSONObject) args[0];
                    Log.d(TAG, "Game Results: " + gameResults.toString());

                    requestQueue.offer(new Runnable() {
                        @Override
                        //ChatGPT usage: No
                        public void run() {
                            new Thread(() -> {
                                currentlyAnimating = true;


                                JSONObject gameData = null;
                                JSONObject gameItems = null;
                                JSONObject globalItems = null;

                                JSONArray playerHandJsonArray = null;
                                JSONArray dealerHandJsonArray = null;
                                try {
                                    gameData = gameResults.getJSONObject("gameData");
                                    gameItems = gameData.getJSONObject("gameItems");
                                    globalItems = gameItems.getJSONObject("globalItems");

                                    playerHandJsonArray = globalItems.getJSONArray("playerHand");
                                    dealerHandJsonArray = globalItems.getJSONArray("bankerHand");
                                }
                                catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }


                                String card;
                                try {
                                    card = playerHandJsonArray.getString(0);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                dealNewPlayerCard(card);
                                updateScores();

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }


                                try {
                                    card = dealerHandJsonArray.getString(0);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                dealNewDealerCard(card);
                                updateScores();


                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    card = playerHandJsonArray.getString(1);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                dealNewPlayerCard(card);
                                updateScores();

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    card = dealerHandJsonArray.getString(0);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                dealNewDealerCard(card);
                                updateScores();

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                //Deal remaining player cards
                                for (int i = playerCardIdx; i < 21; i++) {
                                    try {
                                        card = playerHandJsonArray.getString(i);
                                    } catch (JSONException e) {
                                        break;
                                    }
                                    dealNewPlayerCard(card);
                                    updateScores();

                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                                //Deal remaining dealer cards

                                for (int i = dealerCardIdx; i < 21; i++) {
                                    try {
                                        card = dealerHandJsonArray.getString(1);
                                    } catch (JSONException e) {
                                        break;
                                    }
                                    dealNewDealerCard(card);
                                    updateScores();

                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

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
    private void dealNewDealerCard(String card) {
        String[] cardDetails = getCardDetails(card);

        String value = cardDetails[0];
        String suit = cardDetails[1];

        if (dealerCardIdx <= 5) {
            dealerCardItems[dealerCardIdx].setText(value+ "\n" + suit);
        } else {
            Toast.makeText(getApplicationContext(), "Too many cards to display", Toast.LENGTH_SHORT).show();
        }
        dealerCardVals[dealerCardIdx] = cardValues.get(value);
        dealerCardIdx++;
    }

    //ChatGPT usage: No
    private void dealNewPlayerCard(String card) {
        String[] cardDetails = getCardDetails(card);

        String value = cardDetails[0];
        String suit = cardDetails[1];

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

    //ChatGPT usage: No
    private String[] getCardDetails(String card) {
        String val = "";
        String valAbbrv = "";
        String suitAbbrv = "";
        String suit = "";

        if (card.startsWith("10")) {
            valAbbrv = "10";
            suitAbbrv = String.valueOf(card.charAt(2));
        } else {
            valAbbrv = String.valueOf(card.charAt(0));
            suitAbbrv = String.valueOf(card.charAt(1));
        }

        if (suitAbbrv == "H") {
            suit = "Heart";
        } else if (suitAbbrv == "D") {
            suit = "Diamond";
        } else if (suitAbbrv == "C") {
            suit = "Club";
        } else if (suitAbbrv == "S") {
            suit = "Spade";
        }

        if (valAbbrv == "A") {
            val = "Ace";
        } else if (valAbbrv == "J") {
            val = "Jack";
        } else if (valAbbrv == "Q") {
            val = "Queen";
        } else if (valAbbrv == "K") {
            val = "King";
        } else {
            val = valAbbrv;
        }

        String[] returnObj = {val, suit};
        return returnObj;
    }
}