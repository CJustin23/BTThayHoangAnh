# TỔNG HỢP CÁC SƠ ĐỒ THIẾT KẾ HỆ THỐNG QUẢN LÝ CÔNG NHÂN (TCP/IP)

Tài liệu này tổng hợp đầy đủ 8 sơ đồ trong Báo cáo BTL cá nhân môn Lập trình mạng (bao gồm cả hình ảnh `.png` trong thư mục `doc/so_do/` và mã nguồn sơ đồ Mermaid).

---

## 1. Hình 1.1: Mô hình giao tiếp Socket TCP/IP giữa Client và Server trong Java
- **File ảnh PNG:** [so_do/Hinh_1_1_Mo_Hinh_Giao_Tiep_TCP_IP.png](so_do/Hinh_1_1_Mo_Hinh_Giao_Tiep_TCP_IP.png)

![Hình 1.1](so_do/Hinh_1_1_Mo_Hinh_Giao_Tiep_TCP_IP.png)

```mermaid
flowchart LR
    subgraph ClientSide["PHÍA CLIENT"]
        UI["Giao diện người dùng<br>(HTML / CSS / JS)"]
        TC["TCP Client<br>(java.net.Socket)"]
        UI <-->|"HTTP / JSON"| TC
    end
    subgraph ServerSide["PHÍA SERVER (Cổng 9090)"]
        SS["ServerSocket(9090)<br>TcpServer"]
        CH["Luồng xử lý ClientHandler<br>(BufferedReader / PrintWriter)"]
        DAO["Tầng truy xuất CSDL<br>(ProvinceDAO / WorkerDAO)"]
        DB[("CSDL Quan hệ<br>MySQL / H2")]
        SS -->|"accept()"| CH
        CH <--> DAO
        DAO <-->|"JDBC"| DB
    end
    TC <===>|"Giao thức TCP/IP Socket<br>(Gói tin JSON 1 dòng)"| SS
```

---

## 2. Hình 2.1: Biểu đồ Use Case tổng quát của hệ thống
- **File ảnh PNG:** [so_do/Hinh_2_1_UseCase_Tong_Quat.png](so_do/Hinh_2_1_UseCase_Tong_Quat.png)

![Hình 2.1](so_do/Hinh_2_1_UseCase_Tong_Quat.png)

```mermaid
flowchart LR
    User(["👤 Người dùng<br>(Quản lý)"])

    subgraph System["HỆ THỐNG QUẢN LÝ CÔNG NHÂN (TCP/IP)"]
        UC1(["UC01: Thêm Tỉnh/TP"])
        UC2(["UC02: Thêm Công nhân<br>(Chọn quê quán từ danh sách)"])
        UC3(["UC03: Hiển thị kết quả Thêm"])
        UC4(["UC04: Xem danh sách<br>Công nhân & Tỉnh/TP"])
        UC5(["UC05: Tìm kiếm Công nhân<br>(Quê quán / Năm sinh / Lương)"])
        UC6(["UC06: Cập nhật thông tin<br>Công nhân"])
        UC7(["UC07: Xóa một Công nhân"])
        UC8(["UC08: Xóa Công nhân<br>theo từ khóa"])
    end

    User --> UC1
    User --> UC2
    User --> UC4
    User --> UC5
    User --> UC6
    User --> UC7
    User --> UC8

    UC1 -.->|"&lt;&lt;include&gt;&gt;"| UC3
    UC2 -.->|"&lt;&lt;include&gt;&gt;"| UC3
```

---

## 3. Hình 2.2: Biểu đồ Use Case chi tiết phần cá nhân thực hiện
- **File ảnh PNG:** [so_do/Hinh_2_2_UseCase_Chi_Tiet_Ca_Nhan.png](so_do/Hinh_2_2_UseCase_Chi_Tiet_Ca_Nhan.png)

![Hình 2.2](so_do/Hinh_2_2_UseCase_Chi_Tiet_Ca_Nhan.png)

```mermaid
flowchart LR
    Actor(["👤 Người dùng"])

    subgraph PersonalScope["PHẠM VI BTL CÁ NHÂN THỰC HIỆN"]
        UC_AddProv(["UC01: Thêm Tỉnh/TP"])
        UC_AddWorker(["UC02: Thêm Công nhân"])
        UC_SelectProv(["Chọn Quê quán từ<br>danh sách Tỉnh/TP"])
        UC_Validate(["Kiểm tra hợp lệ &<br>Lưu CSDL qua TCP Server"])
        UC_ShowResult(["UC03: Hiển thị kết quả Thêm<br>(Thành công / Thất bại)"])
    end

    Actor --> UC_AddProv
    Actor --> UC_AddWorker

    UC_AddWorker -.->|"&lt;&lt;include&gt;&gt;"| UC_SelectProv
    UC_AddProv -.->|"&lt;&lt;include&gt;&gt;"| UC_Validate
    UC_AddWorker -.->|"&lt;&lt;include&gt;&gt;"| UC_Validate
    UC_AddProv -.->|"&lt;&lt;include&gt;&gt;"| UC_ShowResult
    UC_AddWorker -.->|"&lt;&lt;include&gt;&gt;"| UC_ShowResult
```

---

## 4. Hình 2.3: Biểu đồ lớp (Class Diagram) của toàn bộ hệ thống
- **File ảnh PNG:** [so_do/Hinh_2_3_Bieu_Do_Lop_Class_Diagram.png](so_do/Hinh_2_3_Bieu_Do_Lop_Class_Diagram.png)

![Hình 2.3](so_do/Hinh_2_3_Bieu_Do_Lop_Class_Diagram.png)

```mermaid
classDiagram
    class Province {
        -int id
        -String tenTinh
        -String moTa
        +getId() int
        +setId(int) void
        +getTenTinh() String
        +setTenTinh(String) void
        +getMoTa() String
        +setMoTa(String) void
    }

    class Worker {
        -int id
        -String hoTen
        -int namSinh
        -int queQuanId
        -String tenQueQuan
        -String moTaQueQuan
        -double mucLuong
        +getId() int
        +getHoTen() String
        +getNamSinh() int
        +getQueQuanId() int
        +getTenQueQuan() String
        +getMucLuong() double
    }

    class TcpRequest {
        -String action
        -Province province
        -Worker worker
        -Integer workerId
        -String keyword
        -Integer queQuanId
        -Integer minNamSinh
        -Integer maxNamSinh
        -Double minMucLuong
        -Double maxMucLuong
    }

    class TcpResponse {
        -boolean success
        -String action
        -String message
        -Province addedProvince
        -Worker addedWorker
        -List~Province~ provinces
        -List~Worker~ workers
    }

    class DatabaseConnection {
        -Properties props
        -String activeUrl
        +initDatabase()$ void
        +getConnection()$ Connection
        +getTcpHost()$ String
        +getTcpPort()$ int
        +getWebPort()$ int
    }

    class ProvinceDAO {
        +getAllProvinces() List~Province~
        +getProvinceById(int id) Province
        +existsByName(String tenTinh) boolean
        +addProvince(Province p) Province
    }

    class WorkerDAO {
        +getAllWorkers() List~Worker~
        +getWorkerById(int id) Worker
        +addWorker(Worker w) Worker
        +updateWorker(Worker w) Worker
        +deleteWorkerById(int id) boolean
        +deleteWorkersByKeyword(String kw) int
        +searchWorkers(...) List~Worker~
    }

    class TcpServer {
        -int port
        -ServerSocket serverSocket
        -ExecutorService threadPool
        +start() void
        +startAsync() void
        +stop() void
    }

    class ClientHandler {
        -Socket clientSocket
        -ProvinceDAO provinceDAO
        -WorkerDAO workerDAO
        +run() void
        -processRequest(String json) TcpResponse
        -validateWorker(Worker w) String
    }

    class TcpClient {
        -String serverHost
        -int serverPort
        +sendRequest(TcpRequest req) TcpResponse
    }

    class WebClientServer {
        -int webPort
        -TcpClient tcpClient
        -HttpServer httpServer
        +start() void
        -handleTcpApi(HttpExchange) void
        -handleStaticFiles(HttpExchange) void
    }

    TcpRequest *-- Province
    TcpRequest *-- Worker
    TcpResponse *-- Province
    TcpResponse *-- Worker
    ProvinceDAO ..> DatabaseConnection : uses
    ProvinceDAO ..> Province : manages
    WorkerDAO ..> DatabaseConnection : uses
    WorkerDAO ..> Worker : manages
    TcpServer *-- ClientHandler : spawns
    ClientHandler --> ProvinceDAO : calls
    ClientHandler --> WorkerDAO : calls
    ClientHandler ..> TcpRequest : parses
    ClientHandler ..> TcpResponse : builds
    WebClientServer *-- TcpClient : uses
    TcpClient ..> TcpRequest : sends
    TcpClient ..> TcpResponse : receives
```

---

## 5. Hình 2.4: Biểu đồ tuần tự chức năng Thêm Tỉnh/TP và Hiển thị kết quả Thêm
- **File ảnh PNG:** [so_do/Hinh_2_4_Sequence_Them_Tinh_TP.png](so_do/Hinh_2_4_Sequence_Them_Tinh_TP.png)

![Hình 2.4](so_do/Hinh_2_4_Sequence_Them_Tinh_TP.png)

```mermaid
sequenceDiagram
    autonumber
    actor User as Người dùng
    participant UI as Giao diện HTML/JS<br>(app.js)
    participant TC as TCP Client<br>(TcpClient.java)
    participant TS as TCP Server (9090)<br>(ClientHandler.java)
    participant DAO as ProvinceDAO
    participant DB as CSDL (MySQL/H2)

    User->>UI: Nhập Tên Tỉnh/TP, Mô tả & Bấm "Thêm Tỉnh/TP"
    UI->>TC: Gửi yêu cầu {action: "ADD_PROVINCE", province}
    TC->>TS: Mở kết nối Socket(127.0.0.1, 9090)<br>writer.println(jsonRequest)
    TS->>DAO: existsByName(tenTinh)
    DAO->>DB: SELECT COUNT(*) FROM tinh_tp WHERE LOWER(ten_tinh) = LOWER(?)
    DB-->>DAO: Kết quả kiểm tra trùng
    alt Tên Tỉnh/TP chưa tồn tại
        TS->>DAO: addProvince(province)
        DAO->>DB: INSERT INTO tinh_tp (ten_tinh, mo_ta) VALUES (?, ?)
        DB-->>DAO: Trả về Generated Key (newId)
        DAO->>DB: SELECT id, ten_tinh, mo_ta FROM tinh_tp WHERE id = newId
        DB-->>DAO: Bản ghi Province vừa tạo
        DAO-->>TS: Đối tượng Province (created)
        TS->>DAO: getAllProvinces()
        DAO-->>TS: Danh sách Tỉnh/TP mới nhất
        TS-->>TC: writer.println(jsonResponse)<br>{success: true, addedProvince: created, provinces}
        TC-->>UI: Trả về đối tượng TcpResponse
        UI->>UI: Cập nhật bảng Tỉnh/TP & Dropdown Quê quán
        UI->>UI: renderAddResult('PROVINCE', true, msg, addedProvince)
        UI-->>User: Hiển thị kết quả Thêm Tỉnh/TP thành công
    else Tên Tỉnh/TP đã tồn tại
        TS-->>TC: writer.println(jsonResponse)<br>{success: false, message: "Tỉnh/TP đã tồn tại!"}
        TC-->>UI: Trả về TcpResponse lỗi
        UI->>UI: renderAddResult('PROVINCE', false, msg, province)
        UI-->>User: Hiển thị thông báo lỗi tại khung Kết quả Thêm
    end
```

---

## 6. Hình 2.5: Biểu đồ tuần tự chức năng Thêm Công nhân và Hiển thị kết quả Thêm
- **File ảnh PNG:** [so_do/Hinh_2_5_Sequence_Them_Cong_Nhan.png](so_do/Hinh_2_5_Sequence_Them_Cong_Nhan.png)

![Hình 2.5](so_do/Hinh_2_5_Sequence_Them_Cong_Nhan.png)

```mermaid
sequenceDiagram
    autonumber
    actor User as Người dùng
    participant UI as Giao diện HTML/JS<br>(app.js)
    participant TC as TCP Client<br>(TcpClient.java)
    participant TS as TCP Server (9090)<br>(ClientHandler.java)
    participant WDAO as WorkerDAO
    participant DB as CSDL (MySQL/H2)

    User->>UI: Nhập Họ tên, Năm sinh, Mức lương,<br>Chọn Quê quán (queQuanId) & Bấm "Thêm Công nhân"
    UI->>TC: Gửi yêu cầu {action: "ADD_WORKER", worker}
    TC->>TS: Kết nối Socket(127.0.0.1, 9090)<br>writer.println(jsonRequest)
    TS->>TS: validateWorker(worker)<br>(Kiểm tra Họ tên, Năm sinh, Quê quán, Lương)
    alt Dữ liệu hợp lệ
        TS->>WDAO: addWorker(worker)
        WDAO->>DB: INSERT INTO cong_nhan (ho_ten, nam_sinh, que_quan_id, muc_luong)<br>VALUES (?, ?, ?, ?)
        DB-->>WDAO: Trả về Generated Key (newId)
        WDAO->>DB: SELECT c.*, t.ten_tinh FROM cong_nhan c<br>INNER JOIN tinh_tp t ON c.que_quan_id = t.id WHERE c.id = newId
        DB-->>WDAO: Bản ghi Worker kèm tên Quê quán
        WDAO-->>TS: Đối tượng Worker (created)
        TS->>WDAO: getAllWorkers()
        WDAO-->>TS: Danh sách Công nhân mới nhất
        TS-->>TC: writer.println(jsonResponse)<br>{success: true, addedWorker: created, workers}
        TC-->>UI: Trả về đối tượng TcpResponse
        UI->>UI: Cập nhật bảng Danh sách Công nhân (tô sáng dòng mới)
        UI->>UI: renderAddResult('WORKER', true, msg, addedWorker)
        UI-->>User: Hiển thị chi tiết Công nhân vừa thêm tại khung Kết quả Thêm
    else Dữ liệu không hợp lệ
        TS-->>TC: writer.println(jsonResponse)<br>{success: false, message: validationError}
        TC-->>UI: Trả về TcpResponse lỗi
        UI->>UI: renderAddResult('WORKER', false, msg, worker)
        UI-->>User: Hiển thị lỗi tại khung Kết quả Thêm
    end
```

---

## 7. Hình 2.6: Sơ đồ thực thể quan hệ (ERD) của Cơ sở dữ liệu
- **File ảnh PNG:** [so_do/Hinh_2_6_So_Do_ERD_CSDL.png](so_do/Hinh_2_6_So_Do_ERD_CSDL.png)

![Hình 2.6](so_do/Hinh_2_6_So_Do_ERD_CSDL.png)

```mermaid
erDiagram
    TINH_TP ||--o{ CONG_NHAN : "là quê quán của (que_quan_id)"
    TINH_TP {
        int id PK "Mã Tỉnh/TP (AUTO_INCREMENT)"
        varchar(120) ten_tinh UK "Tên Tỉnh/TP (NOT NULL, UNIQUE)"
        varchar(500) mo_ta "Mô tả Tỉnh/TP"
    }
    CONG_NHAN {
        int id PK "Mã Công nhân (AUTO_INCREMENT)"
        varchar(150) ho_ten "Họ và tên công nhân (NOT NULL)"
        int nam_sinh "Năm sinh (NOT NULL)"
        int que_quan_id FK "Mã Tỉnh/TP quê quán (NOT NULL)"
        double muc_luong "Mức lương VNĐ (NOT NULL)"
    }
```

---

## 8. Hình 3.1: Sơ đồ kiến trúc tổng thể các khối của ứng dụng
- **File ảnh PNG:** [so_do/Hinh_3_1_Kien_Truc_Ung_Dung.png](so_do/Hinh_3_1_Kien_Truc_Ung_Dung.png)

![Hình 3.1](so_do/Hinh_3_1_Kien_Truc_Ung_Dung.png)

```mermaid
flowchart TB
    subgraph UserBlock["KHỐI NGƯỜI DÙNG (USER)"]
        U1["👤 Người dùng 1<br>(Trình duyệt Web)"]
        U2["👤 Người dùng 2<br>(Trình duyệt Web)"]
    end

    subgraph ClientBlock["KHỐI CLIENT (Cổng 8080)"]
        HTML["Giao diện Frontend<br>(index.html, style.css, app.js)"]
        WCS["WebClientServer<br>(com.sun.net.httpserver)"]
        TCPC["TcpClient<br>(java.net.Socket)"]
        HTML <-->|"Fetch JSON (/api/tcp)"| WCS
        WCS --> TCPC
    end

    subgraph ServerBlock["KHỐI BACKEND TCP/IP SERVER (Cổng 9090)"]
        TCPS["TcpServer<br>(java.net.ServerSocket: 9090)"]
        TP["ExecutorService ThreadPool<br>(Xử lý đa luồng ClientHandler)"]
        PDAO["ProvinceDAO<br>(Nghiệp vụ Tỉnh/TP)"]
        WDAO["WorkerDAO<br>(Nghiệp vụ Công nhân)"]
        DBC["DatabaseConnection<br>(Quản lý kết nối JDBC)"]
        TCPS --> TP
        TP --> PDAO
        TP --> WDAO
        PDAO --> DBC
        WDAO --> DBC
    end

    subgraph DBBlock["KHỐI CƠ SỞ DỮ LIỆU"]
        MYSQL[("MySQL 8.x / H2 SQL<br>Bảng: tinh_tp, cong_nhan")]
    end

    U1 <--> HTML
    U2 <--> HTML
    TCPC <===>|"Giao thức TCP/IP Socket (127.0.0.1:9090)<br>Luồng BufferedReader / PrintWriter (UTF-8)"| TCPS
    DBC <===>|"JDBC PreparedStatement"| MYSQL
```
