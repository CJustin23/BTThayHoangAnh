package com.quanlycongnhan;

import com.quanlycongnhan.client.WebClientServer;
import com.quanlycongnhan.dao.DatabaseConnection;
import com.quanlycongnhan.server.TcpServer;

/**
 * Điểm khởi chạy tích hợp cả Backend (TCP/IP Server) và Frontend (HTML + TCP Client)
 */
public class MainApp {
    public static void main(String[] args) throws Exception {
        int tcpPort = DatabaseConnection.getTcpPort();
        String tcpHost = DatabaseConnection.getTcpHost();
        int webPort = DatabaseConnection.getWebPort();

        // 1. Khởi chạy Backend TCP/IP Server trên luồng riêng
        TcpServer tcpServer = new TcpServer(tcpPort);
        tcpServer.startAsync();

        // Chờ ngắn để TCP Server sẵn sàng lắng nghe
        Thread.sleep(400);

        // 2. Khởi chạy Web Client Server phục vụ giao diện HTML & kết nối TCP/IP
        WebClientServer webClientServer = new WebClientServer(webPort, tcpHost, tcpPort);
        webClientServer.start();

        // 3. Khởi chạy H2 Database Web Console (cổng 8082) để xem trực tiếp bảng CSDL trên trình duyệt
        try {
            org.h2.tools.Server h2WebConsole = org.h2.tools.Server.createWebServer("-webPort", "8082");
            h2WebConsole.start();
            System.out.println("  Trình quản lý CSDL (H2 Console): http://localhost:8082");
            System.out.println("  -> JDBC URL: jdbc:h2:./data/quan_ly_cong_nhan_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE");
            System.out.println("  -> User Name: sa   |   Password: (để trống)");
            System.out.println("==================================================================");
        } catch (Exception e) {
            System.out.println("[DB-Console] Cổng 8082 đang được sử dụng: " + e.getMessage());
        }
    }
}
