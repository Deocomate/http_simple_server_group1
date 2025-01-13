package com.app.handlers;

import com.app.models.ClientInfo;
import com.app.controller.ServerController;
import com.app.utils.CookieUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class AdminHandler implements HttpHandler {
    private final ServerController controller;

    public AdminHandler(ServerController controller) {
        this.controller = controller;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Chỉ xử lý phương thức GET
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            return;
        }

        // Xác thực cookie
        List<String> cookieHeaders = exchange.getRequestHeaders().get("Cookie");

        if (cookieHeaders == null || cookieHeaders.isEmpty()) {
            sendUnauthorized(exchange);
            return;
        }

        String cookieHeader = cookieHeaders.get(0);

        // Phân tách các cookie thành một Map
        Map<String, String> cookieMap = CookieUtils.parseCookies(cookieHeader);

        // Lấy giá trị SESSIONID từ cookie
        String sessionId = cookieMap.get("SESSIONID");
        boolean authenticated = false;
        String username = null;

        if (sessionId != null) {
            username = CookieUtils.validateSession(sessionId);
            if (username != null) {
                authenticated = true;
            }
        }

        if (!authenticated) {
            sendUnauthorized(exchange);
            return;
        }

        // Nếu đã xác thực thì lấy danh sách các client
        List<ClientInfo> clients = controller.getConnectedClients();

        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>").append("<html>").append("<head>").append("<meta charset=\"UTF-8\">").append("<title>Connected Clients</title>").append("</head>").append("<body>").append("<h1>Connected Clients</h1>");

        if (clients.isEmpty()) {
            htmlBuilder.append("<p>No clients are currently connected.</p>");
        } else {
            htmlBuilder.append("<ul>");
            for (ClientInfo client : clients) {
                htmlBuilder.append("<li>").append("IP: ").append(client.getIpAddress()).append(", Port: ").append(client.getPort()).append("</li>");
            }
            htmlBuilder.append("</ul>");
        }

        htmlBuilder.append("</body>").append("</html>");

        String response = htmlBuilder.toString();

        // Thiết lập các header phản hồi
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);

        // Ghi dữ liệu vào phản hồi
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Gửi phản hồi 401 Unauthorized
     *
     * @param exchange HttpExchange object
     * @throws IOException nếu có lỗi khi gửi phản hồi
     */
    private void sendUnauthorized(HttpExchange exchange) throws IOException {
        String response = "Unauthorized";
        exchange.sendResponseHeaders(401, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}
