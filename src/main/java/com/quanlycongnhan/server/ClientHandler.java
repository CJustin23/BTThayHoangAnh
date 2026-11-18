package com.quanlycongnhan.server;

import com.google.gson.Gson;
import com.quanlycongnhan.dao.ProvinceDAO;
import com.quanlycongnhan.dao.WorkerDAO;
import com.quanlycongnhan.model.Province;
import com.quanlycongnhan.model.TcpRequest;
import com.quanlycongnhan.model.TcpResponse;
import com.quanlycongnhan.model.Worker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Xử lý từng kết nối TCP/IP Socket từ Client gửi lên Server
 */
public class ClientHandler implements Runnable {
    private static final Gson gson = new Gson();
    private final Socket clientSocket;
    private final ProvinceDAO provinceDAO = new ProvinceDAO();
    private final WorkerDAO workerDAO = new WorkerDAO();

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                TcpResponse response = processRequest(line);
                writer.println(gson.toJson(response));
            }
        } catch (Exception e) {
            System.err.println("[TCP-SERVER] Lỗi giao tiếp với Client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (Exception ignored) {
            }
        }
    }

    private TcpResponse processRequest(String jsonPayload) {
        TcpResponse res = new TcpResponse();

        try {
            TcpRequest req = gson.fromJson(jsonPayload, TcpRequest.class);
            if (req == null || req.getAction() == null) {
                res.setSuccess(false);
                res.setAction("UNKNOWN");
                res.setMessage("Bản tin TCP không hợp lệ!");
                return res;
            }

            String action = req.getAction().trim().toUpperCase();
            res.setAction(action);

            switch (action) {
                case "GET_ALL_DATA", "GET_WORKERS" -> {
                    res.setProvinces(provinceDAO.getAllProvinces());
                    res.setWorkers(workerDAO.getAllWorkers());
                    res.setSuccess(true);
                    res.setMessage("Trả về danh sách công nhân thành công.");
                }

                case "GET_PROVINCES" -> {
                    res.setProvinces(provinceDAO.getAllProvinces());
                    res.setSuccess(true);
                    res.setMessage("Trả về danh sách Tỉnh/TP thành công.");
                }

                case "ADD_PROVINCE" -> {
                    Province p = req.getProvince();
                    if (p == null || p.getTenTinh() == null || p.getTenTinh().trim().isEmpty()) {
                        res.setSuccess(false);
                        res.setMessage("Tên Tỉnh/TP không được để trống!");
                        return res;
                    }
                    if (provinceDAO.existsByName(p.getTenTinh())) {
                        res.setSuccess(false);
                        res.setMessage("Tỉnh/TP '" + p.getTenTinh().trim() + "' đã tồn tại!");
                        return res;
                    }
                    Province created = provinceDAO.addProvince(p);
                    res.setAddedProvince(created);
                    res.setProvinces(provinceDAO.getAllProvinces());
                    res.setWorkers(workerDAO.getAllWorkers());
                    res.setSuccess(true);
                    res.setMessage("Thêm Tỉnh/TP '" + created.getTenTinh() + "' thành công.");
                }

                case "ADD_WORKER" -> {
                    Worker w = req.getWorker();
                    String validationError = validateWorker(w);
                    if (validationError != null) {
                        res.setSuccess(false);
                        res.setMessage(validationError);
                        return res;
                    }
                    Worker created = workerDAO.addWorker(w);
                    res.setAddedWorker(created);
                    res.setWorkers(workerDAO.getAllWorkers());
                    res.setProvinces(provinceDAO.getAllProvinces());
                    res.setSuccess(true);
                    res.setMessage("Thêm Công nhân '" + created.getHoTen() + "' thành công.");
                }

                case "UPDATE_WORKER" -> {
                    // Kiểm tra dữ liệu rồi chuyển yêu cầu cập nhật xuống WorkerDAO.
                    Worker w = req.getWorker();
                    if (w == null || w.getId() <= 0) {
                        res.setSuccess(false);
                        res.setMessage("Thiếu mã công nhân cần cập nhật!");
                        return res;
                    }
                    String validationError = validateWorker(w);
                    if (validationError != null) {
                        res.setSuccess(false);
                        res.setMessage(validationError);
                        return res;
                    }
                    Worker updated = workerDAO.updateWorker(w);
                    if (updated == null) {
                        res.setSuccess(false);
                        res.setMessage("Không tìm thấy công nhân để cập nhật!");
                        return res;
                    }
                    res.setAddedWorker(updated);
                    res.setWorkers(workerDAO.getAllWorkers());
                    res.setProvinces(provinceDAO.getAllProvinces());
                    res.setSuccess(true);
                    res.setMessage("Cập nhật công nhân '" + updated.getHoTen() + "' thành công.");
                }

                case "DELETE_WORKER_BY_ID" -> {
                    // Xóa một công nhân theo ID được gửi từ giao diện.
                    if (req.getWorkerId() == null || req.getWorkerId() <= 0) {
                        res.setSuccess(false);
                        res.setMessage("Mã công nhân cần xóa không hợp lệ!");
                        return res;
                    }
                    boolean deleted = workerDAO.deleteWorkerById(req.getWorkerId());
                    res.setWorkers(workerDAO.getAllWorkers());
                    res.setProvinces(provinceDAO.getAllProvinces());
                    res.setSuccess(deleted);
                    res.setMessage(deleted ? "Đã xóa công nhân thành công." : "Không tìm thấy công nhân cần xóa!");
                }

                case "DELETE_WORKERS_BY_KEYWORD" -> {
                    // Xóa nhiều công nhân theo tên hoặc tên tỉnh/thành.
                    String kw = req.getKeyword();
                    if (kw == null || kw.trim().isEmpty()) {
                        res.setSuccess(false);
                        res.setMessage("Vui lòng nhập từ khóa để xóa!");
                        return res;
                    }
                    int deletedCount = workerDAO.deleteWorkersByKeyword(kw);
                    res.setWorkers(workerDAO.getAllWorkers());
                    res.setProvinces(provinceDAO.getAllProvinces());
                    res.setSuccess(true);
                    res.setMessage(deletedCount > 0
                            ? "Đã xóa " + deletedCount + " công nhân theo từ khóa '" + kw.trim() + "'."
                            : "Không có công nhân nào khớp từ khóa '" + kw.trim() + "'.");
                }

                case "SEARCH_WORKERS" -> {
                    List<Worker> found = workerDAO.searchWorkers(
                            req.getQueQuanId(),
                            req.getMinNamSinh(),
                            req.getMaxNamSinh(),
                            req.getMinMucLuong(),
                            req.getMaxMucLuong()
                    );
                    res.setWorkers(found);
                    res.setProvinces(provinceDAO.getAllProvinces());
                    res.setSuccess(true);
                    res.setMessage("Tìm thấy " + found.size() + " công nhân.");
                }

                default -> {
                    res.setSuccess(false);
                    res.setMessage("Lệnh không hợp lệ: " + action);
                }
            }
        } catch (Exception e) {
            res.setSuccess(false);
            res.setMessage("Lỗi tại TCP Server: " + e.getMessage());
        }
        return res;
    }

    private String validateWorker(Worker w) {
        if (w == null) {
            return "Dữ liệu công nhân không hợp lệ!";
        }
        if (w.getHoTen() == null || w.getHoTen().trim().isEmpty()) {
            return "Họ tên không được để trống!";
        }
        if (w.getNamSinh() < 1940 || w.getNamSinh() > 2015) {
            return "Năm sinh không hợp lệ!";
        }
        if (w.getQueQuanId() <= 0) {
            return "Vui lòng chọn Quê quán từ danh sách!";
        }
        if (w.getMucLuong() <= 0) {
            return "Mức lương phải lớn hơn 0!";
        }
        return null;
    }
}
