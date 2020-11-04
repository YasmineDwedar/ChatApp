package com.example.chattapp.Models;

public class Chat {
    private String sender;
    private String reciever;
    private String messege;
    private  Boolean seen ;

    public Chat(String sender, String reciever, String messege, Boolean seen) {
        this.sender = sender;
        this.reciever = reciever;
        this.messege = messege;
        this.seen = seen;
    }

    public Chat(String sender, String reciever, String messege) {
        this.sender = sender;
        this.reciever = reciever;
        this.messege = messege;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public Chat(){

    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReciever() {
        return reciever;
    }

    public void setReciever(String reciever) {
        this.reciever = reciever;
    }

    public String getMessege() {
        return messege;
    }

    public void setMessege(String messege) {
        this.messege = messege;
    }
}
