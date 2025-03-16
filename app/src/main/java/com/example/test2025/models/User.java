package com.example.test2025.models;

public class User {
    private String userName, email, cin;


    public User(String userName, String email, String cin , Number phone) {
        this.userName = userName;
        this.email = email;
        this.cin = cin;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }


}
