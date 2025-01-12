package com.app;

import com.app.config.ServerConfig;
import com.app.controller.ServerController;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    private static ServerController controller;
    private static ServerConfig config;

    public static void main(String[] args) throws IOException {
        // Khởi tạo cấu hình mặc định
        config = new ServerConfig(8000, "C:\\ServerG1\\files");
        controller = new ServerController(config);

        // Bắt đầu server
        controller.startServer();

//         Công cụ cấu hình đơn giản từ dòng lệnh
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Choose a command below: ");
            System.out.println("1. Start");
            System.out.println("2. Stop");
            System.out.println("3. Config");
            System.out.println("4. exit");
            System.out.print("\nType your command: ");

            String command = scanner.nextLine().toLowerCase(); // Chuyển thành chữ thường để tránh lỗi nhập liệu

            switch (command) {
                case "start":
                    controller.startServer();
                    break;

                case "stop":
                    controller.stopServer();
                    break;

                case "config":
                    // Hiển thị giá trị hiện tại và cho phép cập nhật
                    System.out.println("Current port: " + config.getPort());
                    System.out.print("Enter port (press Enter to keep current): ");
                    String portInput = scanner.nextLine();
                    int newPort = config.getPort(); // Giữ nguyên giá trị mặc định
                    if (!portInput.isEmpty()) {
                        try {
                            newPort = Integer.parseInt(portInput);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid port. Please enter a valid number.");
                            break;
                        }
                    }

                    System.out.println("Current file path: " + config.getFilePath());
                    System.out.print("Enter file path (press Enter to keep current): ");
                    String filePathInput = scanner.nextLine();
                    String newFilePath = config.getFilePath(); // Giữ nguyên giá trị mặc định
                    if (!filePathInput.isEmpty()) {
                        newFilePath = filePathInput;
                    }

                    // Kiểm tra nếu cấu hình thay đổi
                    boolean isPortChanged = newPort != config.getPort();
                    boolean isFilePathChanged = !newFilePath.equals(config.getFilePath());

                    if (isPortChanged || isFilePathChanged) {
                        if (controller.isRunning()) {
                            System.out.println("Configuration changed. Restarting server...");
                            controller.stopServer();
                            config.setPort(newPort);
                            config.setFilePath(newFilePath);
                            controller.startServer();
                        } else {
                            config.setPort(newPort);
                            config.setFilePath(newFilePath);
                            System.out.println("Configuration updated.");
                        }
                    } else {
                        System.out.println("No changes detected. Server remains running.");
                    }
                    break;

                case "exit":
                    controller.stopServer();
                    System.out.println("Exiting program...");
                    System.exit(0);

                default:
                    System.out.println("Unknown command. Please try again.");
            }
        }
    }
}