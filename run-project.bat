@echo off
cd /d "%~dp0"

where mvn >nul 2>nul
if errorlevel 1 (
    echo Maven is required but was not found in PATH.
    echo Install Maven or run the backend from an IDE.
    pause
    exit /b 1
)

where node >nul 2>nul
if errorlevel 1 (
    echo Node.js is required but was not found in PATH.
    echo Install Node.js 20+ and try again.
    pause
    exit /b 1
)

where npm >nul 2>nul
if errorlevel 1 (
    echo npm is required but was not found in PATH.
    echo Install Node.js 20+ and try again.
    pause
    exit /b 1
)

if not exist "%~dp0frontend\node_modules" (
    echo Installing frontend dependencies...
    cd /d "%~dp0frontend"
    call npm install
)

start "Urinalysis Backend" cmd /k "cd /d ""%~dp0backend"" && mvn spring-boot:run"
start "Urinalysis Frontend" cmd /k "cd /d ""%~dp0frontend"" && npm run dev -- --host 0.0.0.0"

echo.
echo Project started.
echo Backend: http://localhost:8080
echo Frontend: http://localhost:5173
echo Make sure MongoDB is running locally on mongodb://localhost:27017
pause
