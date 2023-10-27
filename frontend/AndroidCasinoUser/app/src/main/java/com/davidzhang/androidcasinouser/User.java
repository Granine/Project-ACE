package com.davidzhang.androidcasinouser;
import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String id;
    private String userId;
    private String username;
    private int balance;
    private boolean isAdmin;
    private boolean isChatBanned;
    private String lastRedemptionDate;

    // Constructor
    public User() {
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public int getBalance() {
        return balance;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean isChatBanned() {
        return isChatBanned;
    }

    public String getLastRedemptionDate() {
        return lastRedemptionDate;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public void setChatBanned(boolean chatBanned) {
        isChatBanned = chatBanned;
    }

    public void setLastRedemptionDate(String lastRedemptionDate) {
        this.lastRedemptionDate = lastRedemptionDate;
    }

    // Parcelable implementation
    protected User(Parcel in) {
        id = in.readString();
        userId = in.readString();
        username = in.readString();
        balance = in.readInt();
        isAdmin = in.readByte() != 0;
        isChatBanned = in.readByte() != 0;
        lastRedemptionDate = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userId);
        dest.writeString(username);
        dest.writeInt(balance);
        dest.writeByte((byte) (isAdmin ? 1 : 0));
        dest.writeByte((byte) (isChatBanned ? 1 : 0));
        dest.writeString(lastRedemptionDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
