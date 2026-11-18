package com.quanlycongnhan.model;

import java.io.Serializable;

/**
 * Bản tin Request truyền từ Client tới Server qua giao thức TCP/IP Socket
 */
public class TcpRequest implements Serializable {
    private String action;
    private Province province;
    private Worker worker;
    private Integer workerId;
    private String keyword;

    // Tiêu chí tìm kiếm: theo quê quán / khoảng năm sinh / khoảng lương
    private Integer queQuanId;
    private Integer minNamSinh;
    private Integer maxNamSinh;
    private Double minMucLuong;
    private Double maxMucLuong;

    public TcpRequest() {
    }

    public TcpRequest(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public Integer getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Integer workerId) {
        this.workerId = workerId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getQueQuanId() {
        return queQuanId;
    }

    public void setQueQuanId(Integer queQuanId) {
        this.queQuanId = queQuanId;
    }

    public Integer getMinNamSinh() {
        return minNamSinh;
    }

    public void setMinNamSinh(Integer minNamSinh) {
        this.minNamSinh = minNamSinh;
    }

    public Integer getMaxNamSinh() {
        return maxNamSinh;
    }

    public void setMaxNamSinh(Integer maxNamSinh) {
        this.maxNamSinh = maxNamSinh;
    }

    public Double getMinMucLuong() {
        return minMucLuong;
    }

    public void setMinMucLuong(Double minMucLuong) {
        this.minMucLuong = minMucLuong;
    }

    public Double getMaxMucLuong() {
        return maxMucLuong;
    }

    public void setMaxMucLuong(Double maxMucLuong) {
        this.maxMucLuong = maxMucLuong;
    }
}
