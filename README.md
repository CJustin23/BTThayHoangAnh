# BTL Quản Lý Công Nhân & Tỉnh/TP (Giao Thức TCP/IP - Java Gradle + HTML + JDBC)

## 1. Thông tin đề tài
- **Chủ đề quản lý**: Quản lý Công nhân
- **Giao thức**: TCP/IP (`java.net.ServerSocket` & `java.net.Socket`, cổng `9090`)
- **Đối tượng chính**:
  - **Công nhân**: Họ tên, Năm sinh, Quê quán (`que_quan_id` tham chiếu Tỉnh/TP), Mức lương
  - **Tỉnh/TP**: Tên Tỉnh/TP, Mô tả
- **Nghiệp vụ Server chính**:
  - Thêm công nhân (`ADD_WORKER`)
  - Trả về danh sách công nhân (`GET_WORKERS`, `GET_ALL_DATA`)
  - Cập nhật thông tin công nhân (`UPDATE_WORKER`)
  - Xóa công nhân theo từ khóa (`DELETE_WORKERS_BY_KEYWORD`)
- **Nghiệp vụ Client chính**:
  - Thêm công nhân (chọn quê quán từ danh sách Tỉnh/TP)
  - Tìm kiếm và hiển thị theo quê quán / khoảng năm sinh / khoảng lương (`SEARCH_WORKERS`)
  - Xóa một công nhân (`DELETE_WORKER_BY_ID`)
- **BTL Cá nhân thực hiện**:
  1. **Thêm Tỉnh/TP** (`ADD_PROVINCE`)
  2. **Thêm Công nhân** (`ADD_WORKER`)
  3. **Hiển thị kết quả Thêm** (Hiển thị trực tiếp kết quả phản hồi từ TCP Server và cập nhật bảng dữ liệu tức thì)

---

## 2. Cấu trúc thư mục
```text
├── build.gradle                         # Cấu hình Gradle (Java 21, MySQL, SQL Server, H2, Gson)
├── settings.gradle
├── database/
│   ├── schema_mysql.sql                 # Script tạo CSDL & bảng cho MySQL
│   └── schema_sqlserver.sql             # Script tạo CSDL & bảng cho SQL Server
└── src/main/
    ├── java/com/quanlycongnhan/
    │   ├── MainApp.java                 # Khởi chạy tích hợp cả TCP Server (9090) & Web Client (8080)
    │   ├── model/
    │   │   ├── Province.java            # Đối tượng Tỉnh/TP (id, tenTinh, moTa)
    │   │   ├── Worker.java              # Đối tượng Công nhân (id, hoTen, namSinh, queQuanId, mucLuong)
    │   │   ├── TcpRequest.java          # Gói tin Request qua TCP/IP
    │   │   └── TcpResponse.java         # Gói tin Response từ TCP/IP Server
    │   ├── dao/
    │   │   ├── DatabaseConnection.java  # Kết nối JDBC (MySQL / SQL Server + tự động tạo bảng)
    │   │   ├── ProvinceDAO.java         # Truy vấn & Thêm Tỉnh/TP
    │   │   └── WorkerDAO.java           # Thêm, Danh sách, Cập nhật, Tìm kiếm, Xóa Công nhân
    │   ├── server/
    │   │   ├── TcpServer.java           # Backend TCP/IP Server (ServerSocket cổng 9090)
    │   │   └── ClientHandler.java       # Luồng xử lý từng kết nối Client Socket
    │   └── client/
    │       ├── TcpClient.java           # TCP/IP Socket Client (java.net.Socket)
    │       └── WebClientServer.java     # Phục vụ giao diện HTML (cổng 8080) & cầu nối TCP Client
    └── resources/
        ├── db.properties                # Cấu hình MySQL / SQL Server & cổng TCP/Web
        └── static/
            ├── index.html               # Giao diện Frontend HTML
            ├── style.css                # Giao diện CSS
            └── app.js                   # Xử lý sự kiện Frontend & hiển thị kết quả TCP/IP
```

---

## 3. Cấu hình Cơ sở dữ liệu (MySQL / SQL Server)
Mở file `src/main/resources/db.properties`:
- Mặc định hệ thống ưu tiên kết nối **MySQL** tại `localhost:3306/quan_ly_cong_nhan` (tự động tạo database và bảng `tinh_tp`, `cong_nhan` nếu chưa có).
- Nếu muốn dùng **SQL Server**, đổi `db.type=sqlserver` và chỉnh `sqlserver.password`.
- Nếu máy chưa bật MySQL/SQL Server, chế độ `db.autoFallback=true` sẽ tự động dùng JDBC SQL nhúng (`./data/quan_ly_cong_nhan_db`) để chương trình vẫn chạy ngay lập tức mà không báo lỗi.

---

## 4. Hướng dẫn chạy chương trình bằng Gradle

### Cách 1: Chạy tích hợp cả TCP Server + Giao diện HTML (Khuyên dùng)
```powershell
.\gradlew.bat run
```
Sau khi chạy, mở trình duyệt tại: **http://localhost:8080**

### Cách 2: Chạy riêng 2 cửa sổ Terminal (Server riêng & Client riêng)
- **Terminal 1 (Backend TCP/IP Server - cổng 9090)**:
  ```powershell
  .\gradlew.bat runServer
  ```
- **Terminal 2 (Client TCP/IP + Giao diện HTML - cổng 8080)**:
  ```powershell
  .\gradlew.bat runClient
  ```
