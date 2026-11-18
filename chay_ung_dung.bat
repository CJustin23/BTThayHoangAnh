@echo off
chcp 65001 >nul
title Quản Lý Công Nhân & Tỉnh/TP - TCP/IP (Gradle + Java + HTML)
echo ==================================================================
echo   ĐANG KHỞI ĐỘNG HỆ THỐNG QUẢN LÝ CÔNG NHÂN (TCP/IP + HTML + JDBC)
echo   Giao diện Web: http://localhost:8080
echo   TCP/IP Server: 127.0.0.1:9090
echo ==================================================================
start "" http://localhost:8080
gradle run
pause
