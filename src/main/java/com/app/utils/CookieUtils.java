package com.app.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class CookieUtils {
    private static final Logger logger = LoggerFactory.getLogger(CookieUtils.class);
    private static final int SESSION_DURATION_HOURS = 24;

    /**
     * Tạo một session cookie và lưu vào cơ sở dữ liệu.
     *
     * @param username Tên người dùng
     * @return Chuỗi Set-Cookie
     */
    public static String createCookie(String username) {
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime creationTime = LocalDateTime.now();
        LocalDateTime expiryTime = creationTime.plusHours(SESSION_DURATION_HOURS);

        String insertSessionSQL = "INSERT INTO sessions (session_id, username, creation_time, expiry_time) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseUtils.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(insertSessionSQL)) {

            preparedStatement.setString(1, sessionId);
            preparedStatement.setString(2, username);
            preparedStatement.setTimestamp(3, Timestamp.valueOf(creationTime));
            preparedStatement.setTimestamp(4, Timestamp.valueOf(expiryTime));

            preparedStatement.executeUpdate();
            logger.info("Session created for user '{}', session ID: {}", username, sessionId);

        } catch (SQLException e) {
            logger.error("Error creating session for user '{}': {}", username, e.getMessage());
            // Có thể ném ngoại lệ hoặc xử lý theo cách khác tùy yêu cầu
        }

        // Tạo header Set-Cookie
        // Ví dụ: Set-Cookie: SESSIONID=<sessionId>; HttpOnly; Path=/; Max-Age=86400
        String cookie = String.format("SESSIONID=%s; HttpOnly; Path=/; Max-Age=%d", sessionId, SESSION_DURATION_HOURS * 3600);
        return cookie;
    }

    /**
     * Xác thực một session cookie.
     *
     * @param sessionId ID của session
     * @return Tên người dùng nếu session hợp lệ, ngược lại null
     */
    public static String validateSession(String sessionId) {
        String query = "SELECT username, expiry_time FROM sessions WHERE session_id = ?";
        try (Connection connection = DatabaseUtils.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, sessionId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String username = resultSet.getString("username");
                    Timestamp expiryTimestamp = resultSet.getTimestamp("expiry_time");
                    LocalDateTime expiryTime = expiryTimestamp.toLocalDateTime();

                    if (LocalDateTime.now().isBefore(expiryTime)) {
                        logger.info("Session '{}' is valid for user '{}'.", sessionId, username);
                        return username;
                    } else {
                        logger.warn("Session '{}' for user '{}' has expired.", sessionId, username);
                        deleteSession(sessionId);
                    }
                } else {
                    logger.warn("Session '{}' not found.", sessionId);
                }
            }

        } catch (SQLException e) {
            logger.error("Error validating session '{}': {}", sessionId, e.getMessage());
        }

        return null;
    }

    /**
     * Xóa một session khỏi cơ sở dữ liệu.
     *
     * @param sessionId ID của session
     */
    public static void deleteSession(String sessionId) {
        String deleteSQL = "DELETE FROM sessions WHERE session_id = ?";

        try (Connection connection = DatabaseUtils.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL)) {

            preparedStatement.setString(1, sessionId);
            preparedStatement.executeUpdate();
            logger.info("Session '{}' has been deleted.", sessionId);

        } catch (SQLException e) {
            logger.error("Error deleting session '{}': {}", sessionId, e.getMessage());
        }
    }

    /**
     * Phân tích chuỗi Cookie thành một Map.
     *
     * @param cookieHeader Chuỗi Cookie từ header
     * @return Map chứa các cặp key-value của cookie
     */
    public static java.util.Map<String, String> parseCookies(String cookieHeader) {
        java.util.Map<String, String> cookieMap = new java.util.HashMap<>();
        String[] cookies = cookieHeader.split(";");

        for (String cookie : cookies) {
            String[] parts = cookie.trim().split("=", 2);
            if (parts.length == 2) {
                String name = parts[0].trim();
                String value = parts[1].trim();
                cookieMap.put(name, value);
            }
        }

        return cookieMap;
    }
}
