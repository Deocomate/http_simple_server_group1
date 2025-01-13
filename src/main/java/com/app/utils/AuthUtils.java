package com.app.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthUtils {
    private static final Logger logger = LoggerFactory.getLogger(AuthUtils.class);

    /**
     * Xác thực người dùng bằng cách kiểm tra thông tin đăng nhập trong cơ sở dữ liệu.
     *
     * @param username Tên người dùng
     * @param password Mật khẩu
     * @return true nếu thông tin đăng nhập hợp lệ, ngược lại false
     */
    public static boolean authenticate(String username, String password) {
        // Câu truy vấn sử dụng PreparedStatement để chặn SQL Injection
        String query = "SELECT COUNT(*) FROM admin WHERE username = ? AND password = ?";

        // Sử dụng try-with-resources để tự động đóng các tài nguyên
        try (Connection connection = DatabaseUtils.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    if (count > 0) {
                        return true; // Trả về true nếu tìm thấy ít nhất một bản ghi
                    } else {
                        return false;
                    }
                }
            }

        } catch (SQLException e) {
            logger.error("Database error during authentication for user '{}': {}", username, e.getMessage());
        }

        return false; // Trả về false nếu có lỗi xảy ra hoặc không tìm thấy bản ghi nào
    }
}
