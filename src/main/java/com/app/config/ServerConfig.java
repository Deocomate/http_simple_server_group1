package com.app.config;

public class ServerConfig {
    private int port;

    public ServerConfig(int port) {
        this.port = port;
    }

    // Getters and Setters
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
