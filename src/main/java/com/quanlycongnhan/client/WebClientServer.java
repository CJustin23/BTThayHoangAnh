package com.quanlycongnhan.client;

import com.google.gson.Gson;
import com.quanlycongnhan.dao.DatabaseConnection;
import com.quanlycongnhan.model.TcpRequest;
import com.quanlycongnhan.model.TcpResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

/**
 * Web Server phía Client phục vụ giao diện Frontend HTML/CSS/JS
 * và đóng vai trò TCP/IP Client gửi toàn bộ thao tác qua Socket tới TcpServer.
 */
public class WebClientServer {
    private static final Gson gson = new Gson();
    private final int webPort;
    private final TcpClient tcpClient;
    private HttpServer httpServer;

    public WebClientServer(int webPort, String tcpHost, int tcpPort) {
        this.webPort = webPort;
        this.tcpClient = new TcpClient(tcpHost, tcpPort);
    }

    public void start() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(webPort), 0);
        httpServer.setExecutor(Executors.newCachedThreadPool());

        // API Bridge chuyển tiếp từ giao diện HTML sang TCP/IP Socket
        httpServer.createContext("/api/tcp", this::handleTcpApi);

        // Phục vụ file tĩnh HTML, CSS, JS
        httpServer.createContext("/", this::handleStaticFiles);

        httpServer.start();
        System.out.println("==================================================================");
        System.out.println("  FRONTEND HTML & TCP/IP CLIENT ĐÃ KHỞI ĐỘNG");
        System.out.println("  Địa chỉ truy cập giao diện Web : http://localhost:" + webPort);
        System.out.println("  Kết nối TCP/IP tới Server      : "
                + DatabaseConnection.getTcpHost() + ":" + DatabaseConnection.getTcpPort());
        System.out.println("==================================================================");
    }

    private void handleTcpApi(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            TcpRequest tcpRequest;
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                tcpRequest = new TcpRequest("GET_ALL_DATA");
            } else {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                tcpRequest = gson.fromJson(body, TcpRequest.class);
            }

            // Gửi yêu cầu thực sự qua giao thức TCP/IP Socket tới TcpServer
            TcpResponse tcpResponse = tcpClient.sendRequest(tcpRequest);
            byte[] jsonBytes = gson.toJson(tcpResponse).getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, jsonBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonBytes);
            }
        } catch (Exception e) {
            TcpResponse err = new TcpResponse(false, "ERROR", "Lỗi tại Client Bridge: " + e.getMessage());
            byte[] jsonBytes = gson.toJson(err).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(500, jsonBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonBytes);
            }
        }
    }

    private void handleStaticFiles(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path == null || path.equals("/") || path.isBlank()) {
            path = "/index.html";
        }

        String resourcePath = "static" + path;
        byte[] content = loadResource(resourcePath);
        if (content == null) {
            byte[] notFound = "404 Not Found".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(404, notFound.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(notFound);
            }
            return;
        }

        String contentType = "text/plain; charset=UTF-8";
        if (path.endsWith(".html")) {
            contentType = "text/html; charset=UTF-8";
        } else if (path.endsWith(".css")) {
            contentType = "text/css; charset=UTF-8";
        } else if (path.endsWith(".js")) {
            contentType = "application/javascript; charset=UTF-8";
        }

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, content.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(content);
        }
    }

    private byte[] loadResource(String resourcePath) throws IOException {
        String fileName = resourcePath.startsWith("static/") ? resourcePath.substring("static/".length()) : resourcePath;
        Path rootFrontendFile = Path.of("frontend").resolve(fileName.replace("/", java.io.File.separator));
        if (Files.exists(rootFrontendFile) && Files.isRegularFile(rootFrontendFile)) {
            return Files.readAllBytes(rootFrontendFile);
        }
        Path localFile = Path.of("src", "main", "resources").resolve(resourcePath.replace("/", java.io.File.separator));
        if (Files.exists(localFile) && Files.isRegularFile(localFile)) {
            return Files.readAllBytes(localFile);
        }
        try (InputStream is = WebClientServer.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is != null) {
                return is.readAllBytes();
            }
        }
        return null;
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    public static void main(String[] args) {
        int webPort = DatabaseConnection.getWebPort();
        String tcpHost = DatabaseConnection.getTcpHost();
        int tcpPort = DatabaseConnection.getTcpPort();
        WebClientServer clientServer = new WebClientServer(webPort, tcpHost, tcpPort);
        try {
            clientServer.start();
        } catch (IOException e) {
            System.err.println("Không thể khởi chạy WebClientServer: " + e.getMessage());
        }
    }
}
