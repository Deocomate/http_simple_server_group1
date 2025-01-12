package com.app;

import com.app.config.ServerConfig;
import com.app.controller.ServerController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    private static ServerController controller;
    private static ServerConfig config;

    public static void main(String[] args) throws IOException {
        // Khởi tạo cấu hình mặc định
        config = new ServerConfig(8000);
        controller = new ServerController(config);

        // Bắt đầu server
        controller.startServer();

////         Công cụ cấu hình đơn giản từ dòng lệnh
//        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//        String command;
//        System.out.println("Enter command (start, stop, config, exit):");
//        while ((command = reader.readLine()) != null) {
//            switch (command.toLowerCase()) {
//                case "start":
//                    controller.startServer();
//                    break;
//                case "stop":
//                    controller.stopServer();
//                    break;
//                case "config":
//                    System.out.print("Enter port: ");
//                    int port = Integer.parseInt(reader.readLine());
//                    System.out.print("Enter file path: ");
//                    String path = reader.readLine();
//                    config.setPort(port);
//                    config.setFilePath(path);
//                    System.out.println("Configuration updated.");
//                    break;
//                case "exit":
//                    controller.stopServer();
//                    System.exit(0);
//                    break;
//                default:
//                    System.out.println("Unknown command.");
//            }
//            System.out.println("Enter command (start, stop, config, exit):");
//            return;
//        }
    }
}
