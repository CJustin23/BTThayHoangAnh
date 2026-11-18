package com.quanlycongnhan.model;

import java.io.Serializable;
import java.util.List;

/**
 * Bản tin Response trả về từ TCP/IP Server về Client
 */
public class TcpResponse implements Serializable {
    private boolean success;
    private String action;
    private String message;

    // Kết quả vừa thêm (phục vụ "Hiển thị kết quả Thêm")
    private Province addedProvince;
    private Worker addedWorker;

    // Danh sách dữ liệu trả về
    private List<Province> provinces;
    private List<Worker> workers;

    public TcpResponse() {
    }

    public TcpResponse(boolean success, String action, String message) {
        this.success = success;
        this.action = action;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Province getAddedProvince() {
        return addedProvince;
    }

    public void setAddedProvince(Province addedProvince) {
        this.addedProvince = addedProvince;
    }

    public Worker getAddedWorker() {
        return addedWorker;
    }

    public void setAddedWorker(Worker addedWorker) {
        this.addedWorker = addedWorker;
    }

    public List<Province> getProvinces() {
        return provinces;
    }

    public void setProvinces(List<Province> provinces) {
        this.provinces = provinces;
    }

    public List<Worker> getWorkers() {
        return workers;
    }

    public void setWorkers(List<Worker> workers) {
        this.workers = workers;
    }
}
