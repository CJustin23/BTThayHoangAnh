@echo off
chcp 65001 >nul
title Trình Quản Lý CSDL - H2 Web Console (Cổng 8082)
echo ===========================================================================
echo   HƯỚNG DẪN ĐĂNG NHẬP KHI TRÌNH DUYỆT MỞ RA (http://localhost:8082):
echo   1. Driver Class : org.h2.Driver
echo   2. JDBC URL     : jdbc:h2:./data/quan_ly_cong_nhan_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE
echo   3. User Name    : sa
echo   4. Password     : (Để trống)
echo   Sau đó bấm nút [Connect] để xem bảng tinh_tp và cong_nhan!
echo ===========================================================================
gradle runDbConsole
pause
