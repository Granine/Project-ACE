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
public class BlackJackActivity extends AppCompatActivity {

    private Socket mSocket;
    private String TAG = "BlackJackEvent";
    private boolean currentlyAnimating = false;

    private final Queue<Runnable> requestQueue = new LinkedList<>();

    private Button hitButton, standButton;
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
        put("ten", 10);
        put("jack", 10);
        put("queen", 10);
        put("king", 10);
    }};
    private TextView[] playerCardItems = new TextView[6];
    private TextView[] dealerCardItems = new TextView[6];

    private int dealerCardIdx = 0;
    private int playerCardIdx = 0;

    @Override
    //ChatGPT usage: Partial - things related to queueing requests and popup windows, and socket handling
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_jack);

        mSocket = SocketHandler.getSocket();

        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        String roomName = intent.getStringExtra("roomName");

        TextView tvLobbyName = findViewById(R.id.lobbyNameLabel);
        tvLobbyName.setText("Lobby Name: " + roomName);

        // Initialize UI components
        hitButton = findViewById(R.id.hitButton);
        standButton = findViewById(R.id.standButton);

        turnIndicator = findViewById(R.id.turnLabel);

        playerScoreLabel = findViewById(R.id.playerScoreLabel);
        dealerScoreLabel = findViewById(R.id.dealerScoreLabel);

        lobbyName = findViewById(R.id.lobbyNameLabel);
        lobbyName.setText("Lobby Name: " + roomName);

        playerCardItems[0] = findViewById(R.id.player_card_1);
        playerCardItems[1] = findViewById(R.id.player_card_2);
        playerCardItems[2] = findViewById(R.id.player_card_3);
        playerCardItems[3] = findViewById(R.id.player_card_4);
        playerCardItems[4] = findViewById(R.id.player_card_5);
        playerCardItems[5] = findViewById(R.id.player_card_6);

        dealerCardItems[0] = findViewById(R.id.dealer_card_1);
        dealerCardItems[1] = findViewById(R.id.dealer_card_2);
        dealerCardItems[2] = findViewById(R.id.dealer_card_3);
        dealerCardItems[3] = findViewById(R.id.dealer_card_4);
        dealerCardItems[4] = findViewById(R.id.dealer_card_5);
        dealerCardItems[5] = findViewById(R.id.dealer_card_6);

        // Example: Hide buttons when it's not the player's turn
        hitButton.setVisibility(View.GONE);
        standButton.setVisibility(View.GONE);

        hitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            //ChatGPT usage: No
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "HIT", Toast.LENGTH_SHORT).show();

                mSocket.emit("playTurn", lobbyName, userName, "hit");
            }
        });

        standButton.setOnClickListener(new View.OnClickListener() {
            @Override
            //ChatGPT usage: No
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "STAND", Toast.LENGTH_SHORT).show();

                mSocket.emit("playTurn", lobbyName, userName, "stand");
            }
        });

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


        mSocket.on("playerTurn", new Emitter.Listener() {
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

                                JSONObject gameState = (JSONObject) args[0];
                                Log.d(TAG, "BlackJack Init: " + gameState.toString());

                                //Get Dealer and Player Cards, and Current Player
                                JSONObject gameData = null;
                                JSONObject gameItems = null;
                                JSONObject globalItems = null;
                                JSONObject playerItems = null;
                                JSONObject currentUserData = null;
                                JSONObject turnPlayerData = null;

                                JSONArray playerHandJsonArray = null;
                                JSONArray dealerHandJsonArray = null;
                                JSONArray turnPlayerHandJsonArray = null; //This one is for future updates to the UI

                                JSONArray playerList = null;
                                int currentPlayerIndex = -1;
                                String turnPlayer = "";

                                String card;

                                try {
                                    gameData = gameState.getJSONObject("gameData");
                                    gameItems = gameData.getJSONObject("gameItems");
                                    globalItems = gameItems.getJSONObject("globalItems");
                                    playerItems = gameItems.getJSONObject("playerItems");

                                    playerList = gameState.getJSONArray("playerList");
                                    currentPlayerIndex = gameState.getInt("currentPlayerIndex");
                                    turnPlayer = playerList.getString(currentPlayerIndex);

                                    currentUserData = playerItems.getJSONObject(userName);
                                    turnPlayerData = playerItems.getJSONObject(turnPlayer);

                                    playerHandJsonArray = currentUserData.getJSONArray("playerHand");
                                    turnPlayerHandJsonArray = turnPlayerData.getJSONArray("playerHand");
                                    dealerHandJsonArray = globalItems.getJSONArray("dealerHand");

                                }
                                catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }

                                //See who's turn it is and change UI accordingly
                                if (userName != turnPlayer) {
                                    //in the future, we will replace the user's cards with the cards of whoever is currently going so they can watch.
                                    hideButtonsForOtherTurn(turnPlayer);
                                }
                                else {
                                    showButtonsForPlayerTurn();
                                }

                                //Set all user's cards in the game state that haven't been set yet
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


                                //Set all dealer cards in the game state that haven't been set yet
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

                                //Short sleep before letting next request start
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
                    Log.e(TAG, "No data sent in play turn signal.");
                }
            }
        });

        mSocket.on("gameOver", new Emitter.Listener() {
            @Override
            //ChatGPT usage: Partial - for things related to to the popup window and queueing requests
            public void call(Object... args) {
                if (args[0] != null) {
                    JSONObject gameState = (JSONObject) args[0];
                    Log.d(TAG, "Game Results: " + gameState.toString());

                    requestQueue.offer(new Runnable() {
                        @Override
                        public void run() {

                            //Get Dealer and Player Cards, and Current Player
                            JSONObject gameData = null;
                            JSONObject gameItems = null;
                            JSONObject globalItems = null;
                            JSONObject playerItems = null;
                            JSONObject currentUserData = null;
                            JSONObject turnPlayerData = null;

                            JSONArray playerHandJsonArray = null;
                            JSONArray dealerHandJsonArray = null;
                            JSONArray turnPlayerHandJsonArray = null; //This one is for future updates to the UI

                            JSONArray playerList = null;
                            int currentPlayerIndex = -1;
                            String turnPlayer = "";

                            String card;

                            try {
                                gameData = gameState.getJSONObject("gameData");
                                gameItems = gameData.getJSONObject("gameItems");
                                globalItems = gameItems.getJSONObject("globalItems");
                                playerItems = gameItems.getJSONObject("playerItems");
                                currentUserData = playerItems.getJSONObject(userName);
                                playerHandJsonArray = currentUserData.getJSONArray("playerHand");
                                dealerHandJsonArray = globalItems.getJSONArray("dealerHand");

                            }
                            catch (JSONException e) {
                                throw new RuntimeException(e);
                            }


                            //Set all user's cards in the game state that haven't been set yet
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

                            //Set all dealer cards in the game state that haven't been set yet
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

                            //Give some time to see results before bet winnings pops up
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            runNextFunction();
                        }
                    });
                    runNextFunction();

                    requestQueue.offer(new Runnable() {
                        @Override
                        //ChatGPT usage: Partial - The call to showWinningsPopup
                        public void run() {
                            double earnings = 0;
                            try {
                                JSONObject gameResult = gameState.getJSONObject("gameResult");
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
                    Log.e(TAG, "No data sent in gameOver signal.");
                }
            }
        });


        // Button: Send for Chat
        Button btnSend = findViewById(R.id.btnSend);
        final EditText etEnterMessage = findViewById(R.id.etEnterMessage);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            //TODO: ChatGPT usage: ASK DAVIDZ AND DINGWEN
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

    // Call this method to show buttons when it's the player's turn
    //ChatGPT usage: partial - how to do View.VISIBLE
    private void showButtonsForPlayerTurn() {
        hitButton.setVisibility(View.VISIBLE);
        standButton.setVisibility(View.VISIBLE);
        turnIndicator.setText("Your Turn");
    }

    // Call this method to hide buttons when it's not the player's turn
    //ChatGPT usage: Partial - how to do View.GONE
    private void hideButtonsForOtherTurn(String userName) {
        hitButton.setVisibility(View.GONE);
        standButton.setVisibility(View.GONE);
        turnIndicator.setText(userName + "'s Turn");
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

        //Check if we can use aces as 11 points
        for (int i = 0; i < playerCardIdx; i++) {
            int value = playerCardVals[i];
            if (value == 1) {
                if (playerScore + 10 <= 21) {
                    playerScore += 10;
                }
            }
        }

        for (int i = 0; i < dealerCardIdx; i++) {
            int value = dealerCardVals[i];
            if (value == 1) {
                if (dealerScore + 10 <= 21) {
                    dealerScore += 10;
                }
            }
        }

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