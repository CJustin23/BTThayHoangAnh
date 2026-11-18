-- =========================================================
-- CƠ SỞ DỮ LIỆU QUẢN LÝ CÔNG NHÂN (SQL Server)
-- Chủ đề: Quản lý Công nhân - Giao thức: TCP/IP
-- Đối tượng chính: Công nhân + Tỉnh/TP
-- =========================================================

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'quan_ly_cong_nhan')
BEGIN
    CREATE DATABASE quan_ly_cong_nhan;
END
GO

USE quan_ly_cong_nhan;
GO

-- 1. Bảng Tỉnh / Thành phố (tên, mô tả)
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[tinh_tp]') AND type in (N'U'))
BEGIN
    CREATE TABLE tinh_tp (
        id INT IDENTITY(1,1) PRIMARY KEY,
        ten_tinh NVARCHAR(120) NOT NULL UNIQUE,
        mo_ta NVARCHAR(500) DEFAULT N''
    );
END
GO

-- 2. Bảng Công nhân (Họ tên, Năm sinh, Quê quán, Mức lương)
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[cong_nhan]') AND type in (N'U'))
BEGIN
    CREATE TABLE cong_nhan (
        id INT IDENTITY(1,1) PRIMARY KEY,
        ho_ten NVARCHAR(150) NOT NULL,
        nam_sinh INT NOT NULL,
        que_quan_id INT NOT NULL,
        muc_luong FLOAT NOT NULL,
        CONSTRAINT fk_cong_nhan_tinh FOREIGN KEY (que_quan_id)
            REFERENCES tinh_tp(id)
    );
END
GO

-- Dữ liệu mẫu Tỉnh/TP
IF NOT EXISTS (SELECT 1 FROM tinh_tp)
BEGIN
    INSERT INTO tinh_tp (ten_tinh, mo_ta) VALUES
    (N'Hà Nội', N'Thủ đô, trung tâm kinh tế - hành chính phía Bắc'),
    (N'Hải Phòng', N'Thành phố cảng công nghiệp trọng điểm Bắc Bộ'),
    (N'Bắc Ninh', N'Trung tâm khu công nghiệp điện tử và sản xuất'),
    (N'Thanh Hóa', N'Tỉnh Bắc Trung Bộ có khu kinh tế Nghi Sơn'),
    (N'Đà Nẵng', N'Thành phố trung tâm kinh tế miền Trung'),
    (N'TP. Hồ Chí Minh', N'Trung tâm công nghiệp - dịch vụ lớn nhất phía Nam');
END
GO

-- Dữ liệu mẫu Công nhân
IF NOT EXISTS (SELECT 1 FROM cong_nhan)
BEGIN
    INSERT INTO cong_nhan (ho_ten, nam_sinh, que_quan_id, muc_luong) VALUES
    (N'Nguyễn Văn An', 1995, 1, 12500000),
    (N'Trần Thị Bình', 1998, 3, 11000000),
    (N'Lê Hoàng Cường', 1992, 4, 14500000),
    (N'Phạm Minh Đức', 2000, 2, 9800000),
    (N'Hoàng Thị Hoa', 1996, 5, 13200000);
END
GO
