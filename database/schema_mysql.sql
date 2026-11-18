-- =========================================================
-- CƠ SỞ DỮ LIỆU QUẢN LÝ CÔNG NHÂN (MySQL)
-- Chủ đề: Quản lý Công nhân - Giao thức: TCP/IP
-- Đối tượng chính: Công nhân + Tỉnh/TP
-- =========================================================

CREATE DATABASE IF NOT EXISTS quan_ly_cong_nhan
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE quan_ly_cong_nhan;

-- 1. Bảng Tỉnh / Thành phố (tên, mô tả)
CREATE TABLE IF NOT EXISTS tinh_tp (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ten_tinh VARCHAR(120) NOT NULL UNIQUE,
    mo_ta VARCHAR(500) DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng Công nhân (Họ tên, Năm sinh, Quê quán, Mức lương)
CREATE TABLE IF NOT EXISTS cong_nhan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ho_ten VARCHAR(150) NOT NULL,
    nam_sinh INT NOT NULL,
    que_quan_id INT NOT NULL,
    muc_luong DOUBLE NOT NULL,
    CONSTRAINT fk_cong_nhan_tinh FOREIGN KEY (que_quan_id)
        REFERENCES tinh_tp(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dữ liệu mẫu Tỉnh/TP
INSERT IGNORE INTO tinh_tp (id, ten_tinh, mo_ta) VALUES
(1, 'Hà Nội', 'Thủ đô, trung tâm kinh tế - hành chính phía Bắc'),
(2, 'Hải Phòng', 'Thành phố cảng công nghiệp trọng điểm Bắc Bộ'),
(3, 'Bắc Ninh', 'Trung tâm khu công nghiệp điện tử và sản xuất'),
(4, 'Thanh Hóa', 'Tỉnh Bắc Trung Bộ có khu kinh tế Nghi Sơn'),
(5, 'Đà Nẵng', 'Thành phố trung tâm kinh tế miền Trung'),
(6, 'TP. Hồ Chí Minh', 'Trung tâm công nghiệp - dịch vụ lớn nhất phía Nam');

-- Dữ liệu mẫu Công nhân
INSERT IGNORE INTO cong_nhan (id, ho_ten, nam_sinh, que_quan_id, muc_luong) VALUES
(1, 'Nguyễn Văn An', 1995, 1, 12500000),
(2, 'Trần Thị Bình', 1998, 3, 11000000),
(3, 'Lê Hoàng Cường', 1992, 4, 14500000),
(4, 'Phạm Minh Đức', 2000, 2, 9800000),
(5, 'Hoàng Thị Hoa', 1996, 5, 13200000);
