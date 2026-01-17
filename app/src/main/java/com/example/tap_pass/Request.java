package com.example.tap_pass;

// A simple data class to hold the information for each request.
public class Request {
    private String userName;
    private String status;

    public Request(String userName, String status) {
        this.userName = userName;
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public String getStatus() {
        return status;
    }
}
