package com.example.test2025.models;


public class Door {
    private String location;
    private String doorCode;
    private String doorStatus;

    public Door(String location, String doorCode, String doorStatus) {
        this.location = location;
        this.doorCode = doorCode;
        this.doorStatus = doorStatus;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDoorCode() {
        return doorCode;
    }

    public void setDoorCode(String doorCode) {
        this.doorCode = doorCode;
    }

    public String getDoorStatus() {
        return doorStatus;
    }

    public void setDoorStatus(String doorStatus) {
        this.doorStatus = doorStatus;
    }
}
