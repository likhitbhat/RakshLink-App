#!/usr/bin/env bash

# Set your target backend URL (Default: http://localhost:3000 or your Render/Railway HTTPS URL)
BACKEND_URL="${1:-http://localhost:3000}"
TEST_PHONE="${2:-+919876543210}"

echo "===================================================="
echo "🧪 Testing RakshaLink Twilio Verify Backend Endpoint"
echo "Backend Base URL: $BACKEND_URL"
echo "Target Phone: $TEST_PHONE"
echo "===================================================="
echo ""

# 1. Health Check
echo "1️⃣ Testing GET /health..."
curl -s -X GET "$BACKEND_URL/health" | grep -o '{.*}'
echo -e "\n"

# 2. Send OTP
echo "2️⃣ Testing POST /api/auth/send-otp..."
curl -s -X POST "$BACKEND_URL/api/auth/send-otp" \
  -H "Content-Type: application/json" \
  -d "{\"phone\": \"$TEST_PHONE\"}"
echo -e "\n"

# 3. Resend OTP
echo "3️⃣ Testing POST /api/auth/resend-otp..."
curl -s -X POST "$BACKEND_URL/api/auth/resend-otp" \
  -H "Content-Type: application/json" \
  -d "{\"phone\": \"$TEST_PHONE\"}"
echo -e "\n"

# 4. Verify OTP (Test incorrect code first)
echo "4️⃣ Testing POST /api/auth/verify-otp (Incorrect Code: 0000)..."
curl -s -X POST "$BACKEND_URL/api/auth/verify-otp" \
  -H "Content-Type: application/json" \
  -d "{\"phone\": \"$TEST_PHONE\", \"otp\": \"0000\"}"
echo -e "\n"

echo "===================================================="
echo "✅ Test script completed!"
echo "To test with your actual 4-digit SMS OTP, run:"
echo "curl -X POST $BACKEND_URL/api/auth/verify-otp -H \"Content-Type: application/json\" -d '{\"phone\": \"$TEST_PHONE\", \"otp\": \"YOUR_REAL_4DIGIT_OTP\"}'"
echo "===================================================="
