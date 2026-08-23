@echo off
set BACKEND_URL=%1
if "%BACKEND_URL%"=="" set BACKEND_URL=http://localhost:3000
set TEST_PHONE=%2
if "%TEST_PHONE%"=="" set TEST_PHONE=+919876543210

echo ====================================================
echo Testing RakshaLink Twilio Verify Backend
echo URL: %BACKEND_URL%
echo Phone: %TEST_PHONE%
echo ====================================================
echo.

echo 1. Testing GET /health...
curl -s -X GET "%BACKEND_URL%/health"
echo.
echo.

echo 2. Testing POST /api/auth/send-otp...
curl -s -X POST "%BACKEND_URL%/api/auth/send-otp" -H "Content-Type: application/json" -d "{\"phone\":\"%TEST_PHONE%\"}"
echo.
echo.

echo 3. Testing POST /api/auth/resend-otp...
curl -s -X POST "%BACKEND_URL%/api/auth/resend-otp" -H "Content-Type: application/json" -d "{\"phone\":\"%TEST_PHONE%\"}"
echo.
echo.

echo 4. Testing POST /api/auth/verify-otp (Test Code: 0000)...
curl -s -X POST "%BACKEND_URL%/api/auth/verify-otp" -H "Content-Type: application/json" -d "{\"phone\":\"%TEST_PHONE%\",\"otp\":\"0000\"}"
echo.
echo.

echo ====================================================
echo Test Script Completed!
echo ====================================================
pause
