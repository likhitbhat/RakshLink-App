require('dotenv').config();
const express = require('express');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
const twilio = require('twilio');
const { parsePhoneNumberFromString } = require('libphonenumber-js');

const app = express();
const PORT = process.env.PORT || 3000;

// Enable CORS & JSON Body Parsing
app.use(cors());
app.use(express.json());

// Initialize Twilio Client
const accountSid = process.env.TWILIO_ACCOUNT_SID;
const authToken = process.env.TWILIO_AUTH_TOKEN;
const verifyServiceSid = process.env.TWILIO_VERIFY_SERVICE_SID;

let twilioClient = null;
if (accountSid && authToken && accountSid.startsWith('AC')) {
  try {
    twilioClient = twilio(accountSid, authToken);
    console.log('[Twilio Backend] Twilio client initialized successfully.');
  } catch (err) {
    console.warn('[Twilio Backend] Warning: Failed to initialize Twilio client:', err.message);
  }
} else {
  console.warn('[Twilio Backend] Notice: TWILIO_ACCOUNT_SID or TWILIO_AUTH_TOKEN not set or using placeholder.');
}

/**
 * Phone Number Validator & E.164 Converter using libphonenumber-js
 */
function validateAndFormatPhone(rawPhone) {
  if (!rawPhone || typeof rawPhone !== 'string') {
    return { valid: false, error: 'Phone number is required.' };
  }

  let cleaned = rawPhone.trim();
  if (!cleaned.startsWith('+')) {
    cleaned = '+' + cleaned;
  }

  const phoneNumber = parsePhoneNumberFromString(cleaned);
  if (phoneNumber && phoneNumber.isValid()) {
    return { valid: true, e164: phoneNumber.number };
  }

  // Secondary fallback check for India / international format
  const fallbackCheck = parsePhoneNumberFromString(rawPhone.trim(), 'IN');
  if (fallbackCheck && fallbackCheck.isValid()) {
    return { valid: true, e164: fallbackCheck.number };
  }

  return {
    valid: false,
    error: 'Invalid phone number format. Please provide a valid mobile number with country code (e.g. +91 9876543210).'
  };
}

/**
 * Express Rate Limiter: Max 5 requests per phone number per hour
 */
const phoneRateLimiter = rateLimit({
  windowMs: 60 * 60 * 1000, // 1 hour window
  max: 5, // max 5 requests per hour
  keyGenerator: (req) => {
    if (req.body && req.body.phone) {
      const parsed = parsePhoneNumberFromString(String(req.body.phone).trim());
      if (parsed && parsed.number) return parsed.number;
      return String(req.body.phone).trim();
    }
    return req.ip;
  },
  handler: (req, res) => {
    return res.status(429).json({
      success: false,
      message: 'Too many OTP requests for this phone number. Maximum 5 requests per hour allowed.'
    });
  },
  standardHeaders: true,
  legacyHeaders: false
});

// Health check endpoint for Render/Railway
app.get('/', (req, res) => res.json({ status: 'ok', service: 'RakshaLink Twilio Verify Backend' }));
app.get('/health', (req, res) => res.json({ status: 'ok', service: 'RakshaLink Twilio Verify Backend', twilioConfigured: Boolean(twilioClient && verifyServiceSid && !verifyServiceSid.includes('xxxx')) }));

/**
 * Controller: Send OTP
 */
async function handleSendOtp(req, res, next) {
  try {
    const { phone } = req.body;
    const phoneCheck = validateAndFormatPhone(phone);

    if (!phoneCheck.valid) {
      return res.status(400).json({
        success: false,
        message: phoneCheck.error
      });
    }

    const formattedPhone = phoneCheck.e164;
    console.log(`[Twilio Backend] Sending OTP to ${formattedPhone}...`);

    if (!twilioClient || !verifyServiceSid || verifyServiceSid.includes('xxxx')) {
      console.log(`[Twilio Backend (DEV DEMO MODE)] Credentials pending. Simulated OTP sent to ${formattedPhone}.`);
      return res.json({
        success: true,
        message: `OTP sent successfully to ${formattedPhone} (Demo Mode: Credentials pending in backend/.env)`
      });
    }

    const verification = await twilioClient.verify.v2
      .services(verifyServiceSid)
      .verifications.create({
        to: formattedPhone,
        channel: 'sms'
      });

    console.log(`[Twilio Backend] Verification status: ${verification.status} for ${formattedPhone}`);

    return res.json({
      success: true,
      message: `OTP sent successfully to ${formattedPhone}`
    });
  } catch (error) {
    next(error);
  }
}

/**
 * Controller: Verify OTP
 */
async function handleVerifyOtp(req, res, next) {
  try {
    const { phone, otp } = req.body;
    const phoneCheck = validateAndFormatPhone(phone);

    if (!phoneCheck.valid) {
      return res.status(400).json({
        success: false,
        verified: false,
        message: phoneCheck.error
      });
    }

    if (!otp || typeof otp !== 'string' || !/^\d{4}$/.test(otp.trim())) {
      return res.status(400).json({
        success: false,
        verified: false,
        message: 'OTP must be exactly 4 numeric digits.'
      });
    }

    const formattedPhone = phoneCheck.e164;
    const cleanOtp = otp.trim();
    console.log(`[Twilio Backend] Verifying OTP for ${formattedPhone}...`);

    if (!twilioClient || !verifyServiceSid || verifyServiceSid.includes('xxxx')) {
      console.log(`[Twilio Backend (DEV DEMO MODE)] Verification for ${formattedPhone} with OTP: ${cleanOtp}`);
      return res.json({
        success: true,
        verified: true,
        message: 'Phone number verified successfully'
      });
    }

    const verificationCheck = await twilioClient.verify.v2
      .services(verifyServiceSid)
      .verificationChecks.create({
        to: formattedPhone,
        code: cleanOtp
      });

    console.log(`[Twilio Backend] Verification Check result: ${verificationCheck.status} for ${formattedPhone}`);

    if (verificationCheck.status === 'approved') {
      return res.json({
        success: true,
        verified: true,
        message: 'Phone number verified successfully'
      });
    } else {
      return res.status(400).json({
        success: false,
        verified: false,
        message: 'Incorrect OTP. Please try again.'
      });
    }
  } catch (error) {
    next(error);
  }
}

/**
 * Controller: Resend OTP
 */
async function handleResendOtp(req, res, next) {
  try {
    const { phone } = req.body;
    const phoneCheck = validateAndFormatPhone(phone);

    if (!phoneCheck.valid) {
      return res.status(400).json({
        success: false,
        message: phoneCheck.error
      });
    }

    const formattedPhone = phoneCheck.e164;
    console.log(`[Twilio Backend] Resending OTP to ${formattedPhone}...`);

    if (!twilioClient || !verifyServiceSid || verifyServiceSid.includes('xxxx')) {
      return res.json({
        success: true,
        message: 'New OTP resent successfully'
      });
    }

    await twilioClient.verify.v2
      .services(verifyServiceSid)
      .verifications.create({
        to: formattedPhone,
        channel: 'sms'
      });

    return res.json({
      success: true,
      message: 'New OTP resent successfully'
    });
  } catch (error) {
    next(error);
  }
}

// Routes with /api/auth prefix & short aliases
app.post('/api/auth/send-otp', phoneRateLimiter, handleSendOtp);
app.post('/send-otp', phoneRateLimiter, handleSendOtp);

app.post('/api/auth/verify-otp', handleVerifyOtp);
app.post('/verify-otp', handleVerifyOtp);

app.post('/api/auth/resend-otp', phoneRateLimiter, handleResendOtp);
app.post('/resend-otp', phoneRateLimiter, handleResendOtp);

// Centralized JSON Error Handler Middleware
app.use((err, req, res, next) => {
  console.error('[Twilio Backend Error Handler]:', err);
  const statusCode = err.status || err.statusCode || 500;
  return res.status(statusCode).json({
    success: false,
    message: err.message || 'An unexpected error occurred on the server.'
  });
});

// Start Express Server
app.listen(PORT, () => {
  console.log(`====================================================`);
  console.log(`🚀 RakshaLink Twilio Verify Backend running on port ${PORT}`);
  console.log(`Endpoints:`);
  console.log(`  POST /api/auth/send-otp`);
  console.log(`  POST /api/auth/verify-otp`);
  console.log(`  POST /api/auth/resend-otp`);
  console.log(`====================================================`);
});
