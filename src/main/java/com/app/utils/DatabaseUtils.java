package com.app.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DatabaseUtils {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtils.class);
    private static HikariDataSource dataSource;

    static {
        Properties props = new Properties();
        try (InputStream input = DatabaseUtils.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.error("Unable to find application.properties");
                throw new RuntimeException("application.properties not found");
            }
            props.load(input);
        } catch (IOException ex) {
            logger.error("Error loading application.properties: {}", ex.getMessage());
            throw new RuntimeException("Failed to load application.properties", ex);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));

        // Các cấu hình bổ sung cho HikariCP
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {
            dataSource = new HikariDataSource(config);
            logger.info("Database connection pool initialized.");
        } catch (Exception e) {
            logger.error("Failed to initialize HikariCP: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize HikariCP", e);
        }
    }

    /**
     * Lấy kết nối từ pool.
     *
     * @return Connection object
     * @throws SQLException nếu không thể lấy kết nối
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Đóng connection pool khi ứng dụng dừng.
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool shut down.");
        }
    }
}
