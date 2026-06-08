@echo off
echo ============================================================
echo   DvBakes SaaS Platform - Spring Boot Backend Launcher
echo ============================================================
echo.

REM Check if Maven is installed
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
  echo [ERROR] Maven (mvn) is not installed or not in PATH.
  echo Download from: https://maven.apache.org/download.cgi
  echo Or install via: winget install Apache.Maven
  pause
  exit /b 1
)

REM Check if Java 21+ is installed
java -version 2>&1 | findstr "version" | findstr /R "2[1-9]\|[3-9][0-9]" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
  echo [WARNING] Java 21+ is recommended. Checking current version...
  java -version
  echo.
)

echo [INFO] Creating data directory...
if not exist "data" mkdir data

echo [INFO] Building Spring Boot application...
call mvn clean package -DskipTests -q

if %ERRORLEVEL% NEQ 0 (
  echo [ERROR] Build failed. Please check the error messages above.
  pause
  exit /b 1
)

echo.
echo [SUCCESS] Build complete!
echo [INFO] Starting Spring Boot server on http://localhost:8080
echo.
echo Available Endpoints:
echo   GET  http://localhost:8080/api/products
echo   GET  http://localhost:8080/api/orders
echo   POST http://localhost:8080/api/auth/validate-pin  (PIN: 1234)
echo   GET  http://localhost:8080/api/admin/dashboard    (JWT Required)
echo   GET  http://localhost:8080/api/admin/db-metrics   (JWT Required)
echo   WS   http://localhost:8080/ws  (Live DB Monitor)
echo   GET  http://localhost:8080/actuator/health
echo.
echo Press Ctrl+C to stop the server.
echo ============================================================

call mvn spring-boot:run

pause
