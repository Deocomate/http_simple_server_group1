package com.app.config;

public class ServerConfig {
    private int port;
    private String filePath;

    public ServerConfig(int port, String filePath) {
        this.port = port;
        this.filePath = filePath;
    }

    // Getters and Setters
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
