package com.quanlycongnhan.dao;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Quản lý kết nối Cơ sở dữ liệu qua JDBC (MySQL / SQL Server / H2 Fallback)
 */
public class DatabaseConnection {
    private static final Properties props = new Properties();
    private static String activeDbType = "mysql";
    private static String activeUrl = "";
    private static String activeUser = "";
    private static String activePassword = "";
    private static String activeDbDisplayName = "MySQL (JDBC)";
    private static boolean initialized = false;

    static {
        loadProperties();
        initDatabase();
    }

    private static void loadProperties() {
        try {
            try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    props.load(in);
                }
            }
            Path externalConfig = Path.of("src", "main", "resources", "db.properties");
            if (Files.exists(externalConfig)) {
                try (InputStream extIn = new FileInputStream(externalConfig.toFile())) {
                    props.load(extIn);
                }
            }
        } catch (Exception e) {
            System.err.println("[DB] Không đọc được db.properties, dùng cấu hình mặc định: " + e.getMessage());
        }
    }

    public static synchronized void initDatabase() {
        // Khởi tạo kết nối CSDL ưu tiên theo cấu hình trong db.properties.
        if (initialized) {
            return;
        }
        DriverManager.setLoginTimeout(2);
        String preferredType = props.getProperty("db.type", "mysql").trim().toLowerCase();
        boolean autoFallback = Boolean.parseBoolean(props.getProperty("db.autoFallback", "true"));

        if ("sqlserver".equals(preferredType)) {
            if (tryConnectSqlServer()) {
                initialized = true;
                createTablesAndSeedData();
                return;
            }
        } else if ("mysql".equals(preferredType)) {
            if (tryConnectMySql()) {
                initialized = true;
                createTablesAndSeedData();
                return;
            }
        }

        if (autoFallback || "h2".equals(preferredType)) {
            setupH2Fallback();
            initialized = true;
            createTablesAndSeedData();
        }
    }

    private static boolean tryConnectMySql() {
        // Kết nối tới MySQL Server và lưu thông tin kết nối đang hoạt động.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = props.getProperty("mysql.url",
                    "jdbc:mysql://localhost:3306/quan_ly_cong_nhan?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&useSSL=false");
            String user = props.getProperty("mysql.user", "root");
            String password = props.getProperty("mysql.password", "");

            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                activeDbType = "mysql";
                activeUrl = url;
                activeUser = user;
                activePassword = password;
                activeDbDisplayName = "MySQL 8.x (JDBC - localhost:3306/quan_ly_cong_nhan)";
                System.out.println("[DB] Kết nối thành công tới MySQL qua JDBC!");
                return true;
            }
        } catch (Exception e) {
            System.out.println("[DB] MySQL tại localhost:3306 chưa bật hoặc cần cấu hình mật khẩu (" + e.getMessage() + ").");
            return false;
        }
    }

    private static boolean tryConnectSqlServer() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = props.getProperty("sqlserver.url",
                    "jdbc:sqlserver://localhost:1433;databaseName=quan_ly_cong_nhan;encrypt=true;trustServerCertificate=true");
            String user = props.getProperty("sqlserver.user", "sa");
            String password = props.getProperty("sqlserver.password", "123456");

            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                activeDbType = "sqlserver";
                activeUrl = url;
                activeUser = user;
                activePassword = password;
                activeDbDisplayName = "SQL Server (JDBC - localhost:1433/quan_ly_cong_nhan)";
                System.out.println("[DB] Kết nối thành công tới SQL Server qua JDBC!");
                return true;
            }
        } catch (Exception e) {
            System.out.println("[DB] SQL Server tại localhost:1433 chưa bật hoặc cần cấu hình mật khẩu (" + e.getMessage() + ").");
            return false;
        }
    }

    private static void setupH2Fallback() {
        try {
            Class.forName("org.h2.Driver");
            activeDbType = "h2";
            activeUrl = "jdbc:h2:./data/quan_ly_cong_nhan_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE";
            activeUser = "sa";
            activePassword = "";
            activeDbDisplayName = "JDBC Embedded SQL (MySQL Mode - ./data/quan_ly_cong_nhan_db)";
            System.out.println("[DB] Đang sử dụng CSDL JDBC tích hợp (MySQL Mode) tại ./data/quan_ly_cong_nhan_db.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Không tìm thấy JDBC Driver!", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        // Cấp một kết nối JDBC mới cho các DAO thực hiện truy vấn.
        if (!initialized) {
            initDatabase();
        }
        return DriverManager.getConnection(activeUrl, activeUser, activePassword);
    }

    public static String getActiveDbDisplayName() {
        return activeDbDisplayName;
    }

    public static int getTcpPort() {
        return Integer.parseInt(props.getProperty("tcp.port", "9090").trim());
    }

    public static String getTcpHost() {
        return props.getProperty("tcp.host", "127.0.0.1").trim();
    }

    public static int getWebPort() {
        return Integer.parseInt(props.getProperty("web.port", "8080").trim());
    }

    private static void createTablesAndSeedData() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            if ("sqlserver".equals(activeDbType)) {
                stmt.execute("""
                    IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[tinh_tp]') AND type in (N'U'))
                    CREATE TABLE tinh_tp (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        ten_tinh NVARCHAR(120) NOT NULL UNIQUE,
                        mo_ta NVARCHAR(500) DEFAULT N''
                    );
                """);
                stmt.execute("""
                    IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[cong_nhan]') AND type in (N'U'))
                    CREATE TABLE cong_nhan (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        ho_ten NVARCHAR(150) NOT NULL,
                        nam_sinh INT NOT NULL,
                        que_quan_id INT NOT NULL,
                        muc_luong FLOAT NOT NULL,
                        CONSTRAINT fk_cong_nhan_tinh FOREIGN KEY (que_quan_id) REFERENCES tinh_tp(id)
                    );
                """);
            } else {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS tinh_tp (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        ten_tinh VARCHAR(120) NOT NULL UNIQUE,
                        mo_ta VARCHAR(500) DEFAULT ''
                    )
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS cong_nhan (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        ho_ten VARCHAR(150) NOT NULL,
                        nam_sinh INT NOT NULL,
                        que_quan_id INT NOT NULL,
                        muc_luong DOUBLE NOT NULL,
                        CONSTRAINT fk_cong_nhan_tinh FOREIGN KEY (que_quan_id) REFERENCES tinh_tp(id)
                    )
                """);
            }

            // Kiểm tra và thêm dữ liệu mẫu Tỉnh/TP nếu trống
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM tinh_tp")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate("INSERT INTO tinh_tp (ten_tinh, mo_ta) VALUES ('Hà Nội', 'Thủ đô, trung tâm kinh tế - hành chính phía Bắc')");
                    stmt.executeUpdate("INSERT INTO tinh_tp (ten_tinh, mo_ta) VALUES ('Hải Phòng', 'Thành phố cảng công nghiệp trọng điểm Bắc Bộ')");
                    stmt.executeUpdate("INSERT INTO tinh_tp (ten_tinh, mo_ta) VALUES ('Bắc Ninh', 'Trung tâm khu công nghiệp điện tử và sản xuất')");
                    stmt.executeUpdate("INSERT INTO tinh_tp (ten_tinh, mo_ta) VALUES ('Thanh Hóa', 'Tỉnh Bắc Trung Bộ có khu kinh tế Nghi Sơn')");
                    stmt.executeUpdate("INSERT INTO tinh_tp (ten_tinh, mo_ta) VALUES ('Đà Nẵng', 'Thành phố trung tâm kinh tế miền Trung')");
                    stmt.executeUpdate("INSERT INTO tinh_tp (ten_tinh, mo_ta) VALUES ('TP. Hồ Chí Minh', 'Trung tâm công nghiệp - dịch vụ lớn nhất phía Nam')");
                }
            }

            // Kiểm tra và thêm dữ liệu mẫu Công nhân nếu trống
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM cong_nhan")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate("INSERT INTO cong_nhan (ho_ten, nam_sinh, que_quan_id, muc_luong) VALUES ('Nguyễn Văn An', 1995, 1, 12500000)");
                    stmt.executeUpdate("INSERT INTO cong_nhan (ho_ten, nam_sinh, que_quan_id, muc_luong) VALUES ('Trần Thị Bình', 1998, 3, 11000000)");
                    stmt.executeUpdate("INSERT INTO cong_nhan (ho_ten, nam_sinh, que_quan_id, muc_luong) VALUES ('Lê Hoàng Cường', 1992, 4, 14500000)");
                    stmt.executeUpdate("INSERT INTO cong_nhan (ho_ten, nam_sinh, que_quan_id, muc_luong) VALUES ('Phạm Minh Đức', 2000, 2, 9800000)");
                    stmt.executeUpdate("INSERT INTO cong_nhan (ho_ten, nam_sinh, que_quan_id, muc_luong) VALUES ('Hoàng Thị Hoa', 1996, 5, 13200000)");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Lỗi khởi tạo bảng dữ liệu: " + e.getMessage());
        }
    }
}
