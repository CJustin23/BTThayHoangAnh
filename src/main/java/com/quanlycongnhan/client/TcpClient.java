package com.quanlycongnhan.client;

import com.google.gson.Gson;
import com.quanlycongnhan.model.TcpRequest;
import com.quanlycongnhan.model.TcpResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * TCP/IP Client sử dụng java.net.Socket để gửi yêu cầu và nhận phản hồi từ TcpServer
 */
public class TcpClient {
    private static final Gson gson = new Gson();
    private final String serverHost;
    private final int serverPort;

    public TcpClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public TcpResponse sendRequest(TcpRequest request) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(serverHost, serverPort), 3000);
            socket.setSoTimeout(5000);

            try (
                PrintWriter writer = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
            ) {
                String jsonRequest = gson.toJson(request);
                writer.println(jsonRequest);

                String jsonResponse = reader.readLine();
                if (jsonResponse == null) {
                    return new TcpResponse(false, request.getAction(), "TCP Server không trả dữ liệu!");
                }
                return gson.fromJson(jsonResponse, TcpResponse.class);
            }
        } catch (Exception e) {
            return new TcpResponse(false,
                    request != null ? request.getAction() : "UNKNOWN",
                    "Không kết nối được tới TCP/IP Server (" + e.getMessage() + ")");
        }
    }
}
