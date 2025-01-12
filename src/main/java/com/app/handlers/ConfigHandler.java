package com.app.handlers;

import com.app.controller.ServerController;
import com.app.utils.CookieUtils;
import com.app.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class ConfigHandler implements HttpHandler {
    private ServerController controller;

    public ConfigHandler(ServerController controller) {
        this.controller = controller;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Xác thực cookie
        List<String> cookieHeaders = exchange.getRequestHeaders().get("Cookie");

        if (cookieHeaders == null || cookieHeaders.isEmpty()) {
            String response = "Unauthorized";
            exchange.sendResponseHeaders(401, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            return;
        }

        // Giả sử chỉ có một header "Cookie", bạn có thể điều chỉnh nếu có nhiều hơn
        String cookieHeader = cookieHeaders.get(0);

        // Phân tách các cookie thành một Map
        Map<String, String> cookieMap = CookieUtils.parseCookies(cookieHeader);

        // Kiểm tra xem cookie "session" có tồn tại và hợp lệ không
        String sessionCookie = cookieMap.get("session");
        boolean authenticated = false;

        // Kiểm tra cookie để đăng nhập
        if (sessionCookie != null && CookieUtils.validateCookie("session=" + sessionCookie)) {
            authenticated = true;
        }

        if (!authenticated) {
            String response = "Unauthorized";
            exchange.sendResponseHeaders(401, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            // Xử lý yêu cầu cấu hình
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = Utils.parseQuery(query);

            if (params.containsKey("port")) {
                int port = Integer.parseInt(params.get("port"));
                controller.setPort(port);
                String response = "Port updated to " + port;
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else if (params.containsKey("filePath")) {
                String filePath = params.get("filePath");
                controller.setFilePath(filePath);
                String response = "File path updated to " + filePath;
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                String response = "Invalid parameters";
                exchange.sendResponseHeaders(400, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
}