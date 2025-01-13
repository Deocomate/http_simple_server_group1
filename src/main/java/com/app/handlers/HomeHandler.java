package com.app.handlers;

import com.app.controller.ServerController;
import com.app.models.ClientInfo;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;

public class HomeHandler implements HttpHandler {

    private final ServerController serverController;
    private final byte[] fileBytes;
    private final String eTag;
    private final String lastModified;

    public HomeHandler(ServerController serverController) throws IOException {
        this.serverController = serverController;

        // Đọc tệp index.html từ thư mục resources
        InputStream is = getClass().getClassLoader().getResourceAsStream("index.html");
        if (is == null) {
            throw new IOException("index.html not found in resources.");
        }

        // Chuyển đổi InputStream thành byte[]
        this.fileBytes = is.readAllBytes();
        is.close();

        // Tạo ETag bằng cách hash nội dung tệp
        this.eTag = generateETag(fileBytes);

        // Thiết lập Last-Modified (ví dụ: sử dụng thời gian hiện tại)
        // Trong thực tế, bạn nên sử dụng thời gian chỉnh sửa thực của tệp nếu có thể
        ZonedDateTime now = ZonedDateTime.now();
        this.lastModified = DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.US).withZone(TimeZone.getTimeZone("GMT").toZoneId()).format(now);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Đảm bảo rằng chỉ xử lý phương thức GET
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            return;
        }

        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        int clientPort = exchange.getRemoteAddress().getPort();
        serverController.addNewClient(new ClientInfo(clientIP, clientPort));

        Headers requestHeaders = exchange.getRequestHeaders();
        Headers responseHeaders = exchange.getResponseHeaders();

        // Thêm ETag và Last-Modified vào phản hồi
        responseHeaders.set("ETag", eTag);
        responseHeaders.set("Last-Modified", lastModified);
        responseHeaders.set("Content-Type", "text/html; charset=UTF-8");

        // Kiểm tra các header điều kiện từ yêu cầu
        String ifNoneMatch = requestHeaders.getFirst("If-None-Match");
        String ifModifiedSince = requestHeaders.getFirst("If-Modified-Since");

        boolean notModified = false;

        // Kiểm tra If-None-Match với ETag
        if (ifNoneMatch != null && ifNoneMatch.equals(eTag)) {
            notModified = true;
        }

        // Nếu If-None-Match không tồn tại hoặc không trùng, kiểm tra If-Modified-Since
        if (!notModified && ifModifiedSince != null) {
            try {
                ZonedDateTime ifModifiedSinceDate = ZonedDateTime.parse(ifModifiedSince, DateTimeFormatter.RFC_1123_DATE_TIME);
                ZonedDateTime lastModifiedDate = ZonedDateTime.parse(lastModified, DateTimeFormatter.RFC_1123_DATE_TIME);

                if (!lastModifiedDate.isAfter(ifModifiedSinceDate)) {
                    notModified = true;
                }
            } catch (Exception e) {
                // Nếu không thể phân tích ngày, bỏ qua kiểm tra này
            }
        }

        if (notModified) {
            // Trả về 304 Not Modified
            exchange.sendResponseHeaders(304, -1);
            exchange.close();
            return;
        }

        // Nếu tài nguyên đã thay đổi hoặc không có header điều kiện, trả về tài nguyên
        exchange.sendResponseHeaders(200, fileBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(fileBytes);
        os.close();
    }

    /**
     * Tạo ETag từ nội dung tệp bằng cách sử dụng SHA-1 hash.
     *
     * @param content Nội dung của tệp dưới dạng byte[]
     * @return ETag dưới dạng chuỗi
     */
    private String generateETag(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(content);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return "\"" + sb.toString() + "\""; // ETag nên được bao quanh bởi dấu ngoặc kép
        } catch (NoSuchAlgorithmException e) {
            // Fallback nếu SHA-1 không được hỗ trợ
            return "\"" + content.hashCode() + "\"";
        }
    }
}
