@echo off
echo 🚀 Starting Smart Quiz App...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java not found! Please install Java JDK 17 first.
    echo 📥 Download from: https://adoptium.net/temurin/releases/?version=17
    pause
    exit /b 1
)

echo ✅ Java found!
echo.

REM Start Python server in background
echo 🐍 Starting Python backend server...
start /B cmd /c "cd server && python demo_app.py"

REM Wait for server to start
echo ⏳ Waiting for server to start...
timeout /t 5 /nobreak >nul

REM Build Android app
echo 🤖 Building Android app...
gradlew.bat assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo 🎉 Build successful!
    echo 📱 APK location: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo 🌐 Backend server running at: http://localhost:5000
    echo 📊 API endpoints available:
    echo    - GET  /api/v1/health
    echo    - GET  /api/v1/subjects  
    echo    - POST /api/v1/questions/generate
    echo    - POST /api/v1/feedback/generate
    echo.
    echo 🚀 Ready to install APK on Android device!
) else (
    echo ❌ Build failed! Check error messages above.
)

pause