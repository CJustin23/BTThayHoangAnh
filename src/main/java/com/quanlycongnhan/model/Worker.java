package com.quanlycongnhan.model;

import java.io.Serializable;

/**
 * Đối tượng chính: Công nhân (Họ tên, Năm sinh, Quê quán, Mức lương)
 */
public class Worker implements Serializable {
    private int id;
    private String hoTen;
    private int namSinh;
    private int queQuanId;
    private String tenQueQuan;
    private String moTaQueQuan;
    private double mucLuong;

    public Worker() {
    }

    public Worker(int id, String hoTen, int namSinh, int queQuanId, String tenQueQuan, String moTaQueQuan, double mucLuong) {
        this.id = id;
        this.hoTen = hoTen;
        this.namSinh = namSinh;
        this.queQuanId = queQuanId;
        this.tenQueQuan = tenQueQuan;
        this.moTaQueQuan = moTaQueQuan;
        this.mucLuong = mucLuong;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public int getNamSinh() {
        return namSinh;
    }

    public void setNamSinh(int namSinh) {
        this.namSinh = namSinh;
    }

    public int getQueQuanId() {
        return queQuanId;
    }

    public void setQueQuanId(int queQuanId) {
        this.queQuanId = queQuanId;
    }

    public String getTenQueQuan() {
        return tenQueQuan;
    }

    public void setTenQueQuan(String tenQueQuan) {
        this.tenQueQuan = tenQueQuan;
    }

    public String getMoTaQueQuan() {
        return moTaQueQuan;
    }

    public void setMoTaQueQuan(String moTaQueQuan) {
        this.moTaQueQuan = moTaQueQuan;
    }

    public double getMucLuong() {
        return mucLuong;
    }

    public void setMucLuong(double mucLuong) {
        this.mucLuong = mucLuong;
    }
}
