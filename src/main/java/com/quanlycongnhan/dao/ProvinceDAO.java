package com.quanlycongnhan.dao;

import com.quanlycongnhan.model.Province;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO xử lý CSDL cho đối tượng Tỉnh/TP (tên, mô tả)
 */
public class ProvinceDAO {

    public List<Province> getAllProvinces() throws SQLException {
        List<Province> list = new ArrayList<>();
        String sql = "SELECT id, ten_tinh, mo_ta FROM tinh_tp ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Province(
                        rs.getInt("id"),
                        rs.getString("ten_tinh"),
                        rs.getString("mo_ta")
                ));
            }
        }
        return list;
    }

    public Province getProvinceById(int id) throws SQLException {
        String sql = "SELECT id, ten_tinh, mo_ta FROM tinh_tp WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Province(
                            rs.getInt("id"),
                            rs.getString("ten_tinh"),
                            rs.getString("mo_ta")
                    );
                }
            }
        }
        return null;
    }

    public boolean existsByName(String tenTinh) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tinh_tp WHERE LOWER(ten_tinh) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenTinh.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public Province addProvince(Province province) throws SQLException {
        String sql = "INSERT INTO tinh_tp (ten_tinh, mo_ta) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, province.getTenTinh().trim());
            ps.setString(2, province.getMoTa() != null ? province.getMoTa().trim() : "");
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newId = keys.getInt(1);
                        return getProvinceById(newId);
                    }
                }
            }
        }
        return null;
    }
}
