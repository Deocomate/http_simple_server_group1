package com.app.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HomeHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Đảm bảo rằng chỉ xử lý phương thức GET
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            return;
        }

        // Đọc tệp index.html từ thư mục resources
        InputStream is = getClass().getClassLoader().getResourceAsStream("index.html");
        if (is == null) {
            String notFoundResponse = "404 (Not Found)\n";
            exchange.sendResponseHeaders(404, notFoundResponse.length());
            OutputStream os = exchange.getResponseBody();
            os.write(notFoundResponse.getBytes(StandardCharsets.UTF_8));
            os.close();
            return;
        }

        // Chuyển đổi InputStream thành byte[]
        byte[] fileBytes = is.readAllBytes();
        is.close();

        // Thiết lập các header phản hồi
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, fileBytes.length);

        // Ghi dữ liệu vào phản hồi
        OutputStream os = exchange.getResponseBody();
        os.write(fileBytes);
        os.close();
    }
}
