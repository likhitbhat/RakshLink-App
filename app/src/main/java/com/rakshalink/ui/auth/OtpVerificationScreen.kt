package com.rakshalink.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakshalink.domain.model.UserRole
import com.rakshalink.ui.components.GlassCard
import com.rakshalink.ui.components.RakshaTopBar
import com.rakshalink.ui.theme.BackgroundDark
import com.rakshalink.ui.theme.CyanAccent
import com.rakshalink.ui.theme.GlassBorder
import com.rakshalink.ui.theme.PrimaryRed
import com.rakshalink.ui.theme.TextPrimary
import com.rakshalink.ui.theme.TextSecondary

@Composable
fun OtpVerificationScreen(
    viewModel: TwilioAuthViewModel,
    userRole: UserRole,
    onVerificationSuccess: (UserRole) -> Unit,
    onBackClick: () -> Unit
) {
    val phone by viewModel.phone.collectAsState()
    val fullPhone = viewModel.fullPhoneNumber()
    val otpDigits by viewModel.otpDigits.collectAsState()
    val cooldownSeconds by viewModel.cooldownSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val focusRequesters = remember { List(4) { FocusRequester() } }

    LaunchedEffect(uiState) {
        when (uiState) {
            is TwilioAuthUiState.Verified -> {
                val role = (uiState as TwilioAuthUiState.Verified).role
                Toast.makeText(context, "Phone number verified successfully!", Toast.LENGTH_LONG).show()
                onVerificationSuccess(role)
            }
            is TwilioAuthUiState.Error -> {
                val msg = (uiState as TwilioAuthUiState.Error).message
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    val isVerifying = uiState is TwilioAuthUiState.Verifying
    val isComplete = otpDigits.all { it.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        RakshaTopBar(
            title = "Verify Code",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Verify OTP",
                    color = TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter the 4-digit code sent to $fullPhone",
                    color = TextSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ENTER 4-DIGIT VERIFICATION CODE",
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 4-Digit OTP Box Grid
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0..3) {
                                val charValue = otpDigits.getOrElse(i) { "" }

                                OutlinedTextField(
                                    value = charValue,
                                    onValueChange = { input ->
                                        if (input.length > 1) {
                                            viewModel.pasteOtp(input)
                                            focusManager.clearFocus()
                                        } else {
                                            viewModel.updateOtpDigit(i, input)
                                            if (input.isNotBlank() && i < 3) {
                                                focusRequesters[i + 1].requestFocus()
                                            } else if (input.isBlank() && i > 0) {
                                                focusRequesters[i - 1].requestFocus()
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .size(56.dp)
                                        .focusRequester(focusRequesters[i]),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = CyanAccent,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = if (i == 3) ImeAction.Done else ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            if (isComplete) {
                                                viewModel.verifyOtp(userRole)
                                            }
                                        }
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanAccent,
                                        unfocusedBorderColor = GlassBorder,
                                        focusedContainerColor = Color(0xFF0B132B),
                                        unfocusedContainerColor = Color(0xFF0B132B)
                                    )
                                )
                            }
                        }

                        if (uiState is TwilioAuthUiState.Error) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = (uiState as TwilioAuthUiState.Error).message,
                                color = PrimaryRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Resend Timer & Button Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LockClock,
                                    contentDescription = "Timer",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isTimerRunning) "Resend OTP in ${cooldownSeconds}s" else "Didn't receive the code?",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Text(
                                text = "Resend OTP",
                                color = if (!isTimerRunning) CyanAccent else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable(enabled = !isTimerRunning) {
                                    viewModel.resendOtp()
                                }
                            )
                        }
                    }
                }
            }

            // Verify Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.verifyOtp(userRole)
                },
                enabled = !isVerifying && isComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isVerifying) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp).width(20.dp),
                            color = BackgroundDark,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Verifying...", color = BackgroundDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Verify", tint = BackgroundDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verify & Proceed ✓", color = BackgroundDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
