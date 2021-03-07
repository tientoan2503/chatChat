package com.example.chatchat.model;

public class Chat {

    private String message;
    private String sender;
    private String receiver;
    private boolean seen;

    public Chat() {
    }

    public Chat(String chat, String sender, String receiver, boolean seen) {
        this.message = chat;
        this.sender = sender;
        this.receiver = receiver;
        this.seen = seen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
