package com.example.chattapp.Models;

public class User {
    private   String username;
    private String imageURL;
    private  String id;
    private String status;
    private String  searchName;

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public User() {
    }

    public User(String username, String imageURL, String id, String status, String searchName) {
        this.username = username;
        this.imageURL = imageURL;
        this.id = id;
        this.status = status;
        this.searchName = searchName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
