package com.app.controller;

import com.app.config.ServerConfig;
import com.app.handlers.AdminHandler;
import com.app.handlers.AuthHandler;
import com.app.handlers.HomeHandler;
import com.app.models.ClientInfo;
import com.app.utils.DatabaseUtils;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
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
        server.setExecutor(null); // Sử dụng executor mặc định
        server.start();
        isRunning = true; // Cập nhật trạng thái server
        System.out.println("Server is running on http://localhost:" + config.getPort());
    }

    public void stopServer() {
        if (isRunning) {
            isRunning = false;
            server.stop(0); // Dừng server ngay lập tức
            System.out.println("Server stopped.");

            // Đóng connection pool
            DatabaseUtils.shutdown();
        } else {
            System.out.println("Server is not running.");
        }
    }

    // set cổng
    public void setPort(int port) {
        config.setPort(port);
    }

    // set đường dẫn đến file hệ thống của http server
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
