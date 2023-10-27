package com.example.casino;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import io.socket.client.Socket;

public class LobbyAdapter extends RecyclerView.Adapter<LobbyAdapter.LobbyViewHolder> {

    private List<Lobby> lobbyList;
    private Context context;
    private Socket mSocket;

    public LobbyAdapter(Context context, List<Lobby> lobbyList) {
        this.context = context;
        this.lobbyList = lobbyList;
    }

    @NonNull
    @Override
    public LobbyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_lobby, parent, false);
        return new LobbyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LobbyViewHolder holder, int position) {
        Lobby lobby = lobbyList.get(position);
        holder.textViewLobbyName.setText("Lobby Name: " + lobby.getName());
        holder.textViewGameType.setText("Game Type: " + lobby.getGameType());
    }

    @Override
    public int getItemCount() {
        return lobbyList.size();
    }

    public class LobbyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLobbyName;
        TextView textViewGameType;
        // reference to the whole CardView
        View rootView;

        public LobbyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewLobbyName = itemView.findViewById(R.id.textViewLobbyName);
            textViewGameType = itemView.findViewById(R.id.textViewGameType);
            rootView = itemView;

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Lobby clickedLobby = lobbyList.get(position);

                        mSocket = SocketHandler.getSocket();
                        mSocket.emit("joinLobby", clickedLobby.getName(), clickedLobby.getCurrentUser());
                        Intent intent = new Intent(context, LobbyActivity.class);

                        intent.putExtra("roomName", clickedLobby.getName());
                        intent.putExtra("gameType", clickedLobby.getGameType());
                        intent.putExtra("gameStarted", clickedLobby.getGameStarted());
                        intent.putExtra("maxPlayer", clickedLobby.getMaxPlayer());
                        intent.putExtra("playNumber", clickedLobby.getPlayNumber() + 1);
                        intent.putExtra("currentUser", clickedLobby.getCurrentUser());

                        context.startActivity(intent);
                    }
                }
            });
        }
    }
}
