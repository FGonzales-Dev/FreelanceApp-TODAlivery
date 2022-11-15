package com.todaliveryph.todaliverymarketdeliveryapp.chats;

public class Chat {
    private String sender, receiver, message, time,date,type;

    public Chat(){

    }

    public Chat(String sender, String receiver, String message,String time,String date, String type) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.time = time;
        this.date = date;
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDate(String date) {
        this.date = date;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
