package com.example.chattapp.Notification;

public class Sender {
    public Sender(Data data, String to) {
        this.data = data;
        this.to = to;
    }

    public Data data;

    public String to;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

}
