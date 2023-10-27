package com.example.casino;

import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;
import android.util.Log;

public class SocketHandler {

    private static final String TAG = "SocketHandler";
    private static Socket mSocket;
    // Private constructor to prevent instantiation
    private SocketHandler() { }

    public synchronized static void setSocket() {
        //if (mSocket == null) {
        try {
            mSocket = IO.socket("http://10.0.2.2:3000");
        } catch (URISyntaxException e) {
            Log.e(TAG, "Error initializing socket", e);
        }
        //}
    }

    public synchronized static Socket getSocket() {
        mSocket.off();
        return mSocket;
    }

    public synchronized static void establishConnection() {
        //if (mSocket != null) {
        mSocket.connect();
        //}
    }

    public synchronized static void closeConnection() {
        //if (mSocket != null) {
        mSocket.disconnect();
        //}
    }
}