package com.app;

import com.app.config.ServerConfig;
import com.app.controller.ServerController;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    private static ServerController controller;
    private static ServerConfig config;

    public static void main(String[] args) throws IOException {
        // Đọc cấu hình từ file
        Properties properties = new Properties();
        properties.load(Main.class.getClassLoader().getResourceAsStream("application.properties"));
        int port = Integer.parseInt(properties.getProperty("server.port"));
        String filePath = properties.getProperty("server.filePath");

        // Kiểm tra xem cổng đã được sử dụng chưa
        while (true) {
            if (!isValidPort(port)) {
                System.out.println("Invalid port number. Port must be between 1024-49151 or 49152-65535.");
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter a new port: ");
                port = scanner.nextInt();
            } else {
                try {
                    ServerSocket socket = new ServerSocket(port);
                    socket.close();
                    break; // Cổng có thể sử dụng
                } catch (BindException e) {
                    System.out.println("Port " + port + " is already in use");
                    Scanner scanner = new Scanner(System.in);
                    System.out.print("Enter a new port: ");
                    port = scanner.nextInt();
                }
            }
        }

        // Tạo server
        ServerConfig config = new ServerConfig(port, filePath);
        ServerController controller = new ServerController(config);

        // Bắt đầu server
        controller.startServer();

        // Công cụ cấu hình từ dòng lệnh
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nChoose a command below: ");
            System.out.println("1.Start    2.Stop   3.Config    4.Exit");
            System.out.print("\nType your command: ");

            String command = scanner.nextLine().toLowerCase();

            switch (command) {
                case "start":
                    controller.startServer();
                    break;

                case "stop":
                    controller.stopServer();
                    break;

                case "config":
                    // Config cổng
                    System.out.println("Current port: " + config.getPort());
                    System.out.print("Enter port: ");
                    String portInput = scanner.nextLine();
                    int newPort = config.getPort(); // Giữ nguyên cổng

                    // Kiểm tra cổng mới nhập có hợp lệ không
                    if (!portInput.isEmpty()) {
                        try {
                            newPort = Integer.parseInt(portInput);
                            if (!isValidPort(newPort)) {
                                System.out.println("Invalid port number. Port must be between 1024-49151 or 49152-65535.");
                                break;
                            }
                            if (!isPortAvailable(newPort)) {
                                System.out.println("Port " + newPort + " is already in use. Please enter a different port.");
                                break;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid port. Please enter a valid port.");
                            break;
                        }
                    }

                    System.out.println("Current file path: " + config.getFilePath());
                    System.out.print("Enter file path: ");
                    String filePathInput = scanner.nextLine();
                    String newFilePath = config.getFilePath(); // Giữ nguyên đường dẫn file
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

    // Kiểm tra cổng có hợp lệ không
    private static boolean isValidPort(int port) {
        return (port >= 1024 && port <= 49151) || (port >= 49152 && port <= 65535);
    }

    // Kiểm tra cổng có thể sử dụng không
    private static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
