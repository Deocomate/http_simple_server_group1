package com.app.controller;

import com.app.config.ServerConfig;
import com.app.handlers.AdminHandler;
import com.app.handlers.AuthHandler;
import com.app.handlers.HomeHandler;
import com.app.models.ClientInfo;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerController {
    private HttpServer server;
    private ServerConfig config;
    private List<ClientInfo> connectedClients = Collections.synchronizedList(new ArrayList<>());

    public ServerController(ServerConfig config) {
        this.config = config;
    }

    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);

        // Thêm các context với handler tương ứng
        server.createContext("/login", new AuthHandler());
        server.createContext("/checkAuthorized", new AdminHandler(this));
        server.createContext("/", new HomeHandler());

        // Thêm listener để theo dõi kết nối
        server.createContext("/", exchange -> {
            // Thêm thông tin client
            String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
            int clientPort = exchange.getRemoteAddress().getPort();
            System.out.println("IP: " + clientIP + ", Port: " + clientPort);

            connectedClients.add(new ClientInfo(clientIP, clientPort));

            System.out.println(clientIP);
            System.out.println(clientPort);

            connectedClients.removeIf(client -> client.getIpAddress().equals(clientIP) && client.getPort() == clientPort);
        });

        server.setExecutor(null); // Sử dụng executor mặc định
        server.start();
        System.out.println("Server is running on http://localhost:" + config.getPort());
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("Server stopped.");
        }
    }

    public List<ClientInfo> getConnectedClients() {
        return new ArrayList<>(connectedClients);
    }
}
