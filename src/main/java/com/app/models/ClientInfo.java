package com.app.models;

public class ClientInfo {
    private String ipAddress;
    private int port;

    public ClientInfo(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    // Getters
    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }
}
