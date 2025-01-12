package com.app.handlers;

import com.app.controller.ServerController;
import com.app.utils.CookieUtils;
import com.app.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ConfigHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(ConfigHandler.class);
    private final ServerController controller;

    public ConfigHandler(ServerController controller) {
        this.controller = controller;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Chỉ xử lý phương thức POST để thay đổi cấu hình
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }

        // Xác thực session từ cookie
        String sessionId = getSessionIdFromCookies(exchange);
        if (sessionId == null || !isAuthenticated(sessionId)) {
            sendUnauthorized(exchange);
            return;
        }

        // Xử lý dữ liệu cấu hình từ body của request
        Map<String, String> params = Utils.parsePostQuery(exchange.getRequestBody());

        String response;
        int statusCode;

        if (params.containsKey("port")) {
            try {
                int port = Integer.parseInt(params.get("port"));
                controller.setPort(port);
                response = "Port updated to " + port;
                statusCode = 200;
                logger.info("Port updated to {}", port);
            } catch (NumberFormatException e) {
                response = "Invalid port number";
                statusCode = 400;
                logger.warn("Invalid port number received: {}", params.get("port"));
            }
        } else if (params.containsKey("filePath")) {
            String filePath = params.get("filePath");
            controller.setFilePath(filePath);
            response = "File path updated to " + filePath;
            statusCode = 200;
            logger.info("File path updated to {}", filePath);
        } else {
            response = "Invalid parameters";
            statusCode = 400;
            logger.warn("Invalid parameters received: {}", params.keySet());
        }

        // Gửi phản hồi về client
        sendResponse(exchange, response, statusCode);
    }

    /**
     * Lấy SESSIONID từ các cookie trong header của request.
     *
     * @param exchange HttpExchange object
     * @return SESSIONID nếu tồn tại và hợp lệ, ngược lại null
     */
    private String getSessionIdFromCookies(HttpExchange exchange) {
        List<String> cookieHeaders = exchange.getRequestHeaders().get("Cookie");
        if (cookieHeaders == null || cookieHeaders.isEmpty()) {
            logger.warn("No Cookie header found in the request.");
            return null;
        }

        // Giả sử chỉ có một header "Cookie"
        String cookieHeader = cookieHeaders.get(0);
        Map<String, String> cookieMap = CookieUtils.parseCookies(cookieHeader);
        String sessionId = cookieMap.get("SESSIONID");

        if (sessionId == null) {
            logger.warn("SESSIONID cookie not found.");
            return null;
        }

        return sessionId;
    }

    /**
     * Kiểm tra xem sessionId có hợp lệ không.
     *
     * @param sessionId ID của session
     * @return true nếu session hợp lệ, ngược lại false
     */
    private boolean isAuthenticated(String sessionId) {
        String username = CookieUtils.validateSession(sessionId);
        if (username != null) {
            logger.info("User '{}' authenticated with session '{}'.", username, sessionId);
            return true;
        } else {
            logger.warn("Authentication failed for session '{}'.", sessionId);
            return false;
        }
    }

    /**
     * Gửi phản hồi HTTP với nội dung và mã trạng thái cụ thể.
     *
     * @param exchange   HttpExchange object
     * @param response   Nội dung phản hồi
     * @param statusCode Mã trạng thái HTTP
     * @throws IOException nếu có lỗi khi gửi phản hồi
     */
    private void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
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
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(401, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Gửi phản hồi 405 Method Not Allowed
     *
     * @param exchange HttpExchange object
     * @throws IOException nếu có lỗi khi gửi phản hồi
     */
    private void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        String response = "Method Not Allowed";
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(405, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}
