package com.app.handlers;

import com.app.utils.AuthUtils;
import com.app.utils.CookieUtils;
import com.app.utils.QueryUtils;
import com.app.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AuthHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            // Lấy Data từ body
            String postQuery = QueryUtils.convertPostQuery(exchange.getRequestBody());
            // Chuyển query về dạng map để lấy
            Map<String, String> params = Utils.parseQuery(postQuery);
            // Lấy Data, đăng nhập
            String username = params.get("username");
            String password = params.get("password");
            System.out.println("username: " + username + " password: " + password);

            if (AuthUtils.authenticate(username, password)) {
                String cookie = CookieUtils.createCookie(username);
                exchange.getResponseHeaders().add("Set-Cookie", cookie);
                String response = "Login successful!";
                exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            } else {
                String response = "Invalid credentials!";
                exchange.sendResponseHeaders(401, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        } else {
            // Đọc file index.html từ resources
            InputStream is = getClass().getClassLoader().getResourceAsStream("index.html");
            if (is == null) {
                String notFoundResponse = "404 (Not Found)\n";
                exchange.sendResponseHeaders(404, notFoundResponse.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFoundResponse.getBytes(StandardCharsets.UTF_8));
                }
                return;
            }

            // Chuyển đổi InputStream thành byte[]
            byte[] fileBytes = is.readAllBytes();
            is.close();

            // Thiết lập các header phản hồi
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, fileBytes.length);

            // Ghi dữ liệu vào phản hồi
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }
    }
}
