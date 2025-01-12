package com.app.controller;

import com.app.config.ServerConfig;
import com.app.handlers.AdminHandler;
import com.app.handlers.AuthHandler;
import com.app.handlers.HomeHandler;
import com.app.models.ClientInfo;
import com.app.utils.DatabaseUtils;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerController {
    private HttpServer server;
    private ServerConfig config;
    private List<ClientInfo> connectedClients = new CopyOnWriteArrayList<>();

    private boolean isRunning = false;

    public boolean isRunning() {
        return isRunning;
    }

    public ServerController(ServerConfig config) {
        this.config = config;
    }

    // Chạy server
    public void startServer() throws IOException {
        if (isRunning) {
            // Nếu server đang chạy, hiển thị thông báo
            System.out.println("Server is already running on http://localhost:" + config.getPort());
            return;
        }

        // Tạo server nếu chưa chạy
        server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);

        // Thêm các context với handler tương ứng
        server.createContext("/login", new AuthHandler());
        server.createContext("/admin", new AdminHandler(this));
        server.createContext("/", new HomeHandler(this));

        // Cấu hình executor
        server.setExecutor(null);
        server.start();
        isRunning = true;
        System.out.println("Server is running on http://localhost:" + config.getPort());
    }

    // Dừng server
    public void stopServer() {
        if (isRunning) {
            server.stop(0);
            isRunning = false;
            System.out.println("Server stopped.");
            // Giải phóng cổng
            try {
                ServerSocket socket = new ServerSocket(config.getPort());
                socket.close();
            } catch (BindException e) {
                System.out.println("Port is still in use. Waiting to be released...");
                while (true) {
                    try {
                        ServerSocket socket = new ServerSocket(config.getPort());
                        socket.close();
                        break;
                    } catch (BindException ex) {
                        System.out.println("Port is still in use. Waiting to be released...");
                        try {
                            Thread.sleep(1000); // Chờ 1 giây
                        } catch (InterruptedException ex1) {
                            Thread.currentThread().interrupt();
                        }
                    } catch (IOException ex) {
                        System.out.println("Error releasing port.");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error releasing port.");
            }
        } else {
            System.out.println("Server is not running.");
        }
    }

    // set cổng
    public void setPort(int port) {
        config.setPort(port);
    }

    // set đường dẫn
    public void setFilePath(String filePath) {
        config.setFilePath(filePath);
    }

    public List<ClientInfo> getConnectedClients() {
        return connectedClients;
    }

    public void addNewClient(ClientInfo client) {
        for (ClientInfo existingClient : connectedClients) {
            if (existingClient.getIpAddress().equals(client.getIpAddress())) {
                // Client đã tồn tại, không thêm lại
                return;
            }
        }
        // Nếu không tìm thấy, thêm mới client
        connectedClients.add(client);
    }
}
