package com.quanlycongnhan.server;

import com.quanlycongnhan.dao.DatabaseConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Backend TCP/IP Server sử dụng java.net.ServerSocket
 * Lắng nghe các kết nối từ TCP Client để thực hiện nghiệp vụ Quản lý Công nhân & Tỉnh/TP
 */
public class TcpServer {
    private final int port;
    private volatile boolean running = false;
    private ServerSocket serverSocket;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public TcpServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        DatabaseConnection.initDatabase();
        serverSocket = new ServerSocket(port);
        running = true;

        System.out.println("==================================================================");
        System.out.println("  BACKEND TCP/IP SERVER - QUẢN LÝ CÔNG NHÂN & TỈNH/TP");
        System.out.println("  Giao thức : TCP/IP Socket (java.net.ServerSocket)");
        System.out.println("  Cổng lắng nghe : " + port);
        System.out.println("  Cơ sở dữ liệu  : " + DatabaseConnection.getActiveDbDisplayName());
        System.out.println("==================================================================");

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new ClientHandler(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("[TCP-SERVER] Lỗi khi chấp nhận kết nối: " + e.getMessage());
                }
            }
        }
    }

    public void startAsync() {
        Thread serverThread = new Thread(() -> {
            try {
                start();
            } catch (IOException e) {
                System.err.println("[TCP-SERVER] Không thể khởi động cổng TCP " + port + ": " + e.getMessage());
            }
        }, "tcp-server-thread");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }
        threadPool.shutdownNow();
    }

    public static void main(String[] args) {
        int port = DatabaseConnection.getTcpPort();
        TcpServer server = new TcpServer(port);
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Không thể khởi chạy TCP Server: " + e.getMessage());
        }
    }
}
