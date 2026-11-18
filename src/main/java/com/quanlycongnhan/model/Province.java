package com.quanlycongnhan.model;

import java.io.Serializable;

/**
 * Đối tượng chính: Tỉnh/TP (tên, mô tả)
 */
public class Province implements Serializable {
    private int id;
    private String tenTinh;
    private String moTa;

    public Province() {
    }

    public Province(int id, String tenTinh, String moTa) {
        this.id = id;
        this.tenTinh = tenTinh;
        this.moTa = moTa;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTenTinh() {
        return tenTinh;
    }

    public void setTenTinh(String tenTinh) {
        this.tenTinh = tenTinh;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}
