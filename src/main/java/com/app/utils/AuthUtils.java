package com.app.utils;

import java.sql.*;

public class AuthUtils {
    // Cấu hình kết nối cơ sở dữ liệu
    private static final String DB_URL = "jdbc:mysql://localhost:3307/db_http_simple_server"; // Thay đổi theo cơ sở dữ liệu của bạn
    private static final String DB_USERNAME = "root"; // Thay đổi theo cơ sở dữ liệu của bạn
    private static final String DB_PASSWORD = "12345678"; // Thay đổi theo cơ sở dữ liệu của bạn

    public static boolean authenticate(String username, String password) {
        // Câu truy vấn sử dụng PreparedStatement để ngăn chặn SQL Injection
        String query = "SELECT COUNT(*) FROM admin WHERE username = ? AND password = ?";

        // Sử dụng try-with-resources để tự động đóng các tài nguyên
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD); PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Đặt các tham số cho PreparedStatement
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            // Thực thi câu truy vấn
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0; // Trả về true nếu tìm thấy ít nhất một bản ghi
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Bạn có thể xử lý ngoại lệ theo cách phù hợp với ứng dụng của bạn
        }

        return false; // Trả về false nếu có lỗi xảy ra hoặc không tìm thấy bản ghi nào
    }
}
