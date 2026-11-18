package com.quanlycongnhan.dao;

import com.quanlycongnhan.model.Worker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO xử lý CSDL cho đối tượng Công nhân (Họ tên, Năm sinh, Quê quán, Mức lương)
 */
public class WorkerDAO {

    private static final String BASE_SELECT = """
        SELECT c.id, c.ho_ten, c.nam_sinh, c.que_quan_id,
               t.ten_tinh AS ten_que_quan, t.mo_ta AS mo_ta_que_quan,
               c.muc_luong
        FROM cong_nhan c
        INNER JOIN tinh_tp t ON c.que_quan_id = t.id
    """;

    private Worker mapRow(ResultSet rs) throws SQLException {
        return new Worker(
                rs.getInt("id"),
                rs.getString("ho_ten"),
                rs.getInt("nam_sinh"),
                rs.getInt("que_quan_id"),
                rs.getString("ten_que_quan"),
                rs.getString("mo_ta_que_quan"),
                rs.getDouble("muc_luong")
        );
    }

    public List<Worker> getAllWorkers() throws SQLException {
        // Lấy toàn bộ công nhân, kèm tên tỉnh/thành từ bảng tinh_tp.
        List<Worker> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY c.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Worker getWorkerById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE c.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Worker addWorker(Worker worker) throws SQLException {
        // Thêm công nhân mới vào MySQL và lấy lại bản ghi vừa tạo.
        String sql = "INSERT INTO cong_nhan (ho_ten, nam_sinh, que_quan_id, muc_luong) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, worker.getHoTen().trim());
            ps.setInt(2, worker.getNamSinh());
            ps.setInt(3, worker.getQueQuanId());
            ps.setDouble(4, worker.getMucLuong());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newId = keys.getInt(1);
                        return getWorkerById(newId);
                    }
                }
            }
        }
        return null;
    }

    public Worker updateWorker(Worker worker) throws SQLException {
        // Cập nhật thông tin công nhân theo mã ID.
        String sql = """
            UPDATE cong_nhan
            SET ho_ten = ?, nam_sinh = ?, que_quan_id = ?, muc_luong = ?
            WHERE id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, worker.getHoTen().trim());
            ps.setInt(2, worker.getNamSinh());
            ps.setInt(3, worker.getQueQuanId());
            ps.setDouble(4, worker.getMucLuong());
            ps.setInt(5, worker.getId());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                return getWorkerById(worker.getId());
            }
        }
        return null;
    }

    public boolean deleteWorkerById(int id) throws SQLException {
        // Xóa một công nhân theo mã ID.
        String sql = "DELETE FROM cong_nhan WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int deleteWorkersByKeyword(String keyword) throws SQLException {
        // Xóa các công nhân có tên hoặc quê quán khớp từ khóa.
        String kw = "%" + keyword.trim().toLowerCase() + "%";
        String sql = """
            DELETE FROM cong_nhan
            WHERE LOWER(ho_ten) LIKE ?
               OR que_quan_id IN (SELECT id FROM tinh_tp WHERE LOWER(ten_tinh) LIKE ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kw);
            ps.setString(2, kw);
            return ps.executeUpdate();
        }
    }

    public List<Worker> searchWorkers(
            Integer queQuanId,
            Integer minNamSinh,
            Integer maxNamSinh,
            Double minMucLuong,
            Double maxMucLuong
    ) throws SQLException {
        // Tìm kiếm công nhân theo quê quán, năm sinh và mức lương.
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (queQuanId != null && queQuanId > 0) {
            sql.append(" AND c.que_quan_id = ? ");
            params.add(queQuanId);
        }
        if (minNamSinh != null && minNamSinh > 0) {
            sql.append(" AND c.nam_sinh >= ? ");
            params.add(minNamSinh);
        }
        if (maxNamSinh != null && maxNamSinh > 0) {
            sql.append(" AND c.nam_sinh <= ? ");
            params.add(maxNamSinh);
        }
        if (minMucLuong != null && minMucLuong >= 0) {
            sql.append(" AND c.muc_luong >= ? ");
            params.add(minMucLuong);
        }
        if (maxMucLuong != null && maxMucLuong >= 0) {
            sql.append(" AND c.muc_luong <= ? ");
            params.add(maxMucLuong);
        }

        sql.append(" ORDER BY c.id DESC");

        List<Worker> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }
}
