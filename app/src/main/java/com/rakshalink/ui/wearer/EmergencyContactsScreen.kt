package com.rakshalink.ui.wearer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.rakshalink.domain.model.EmergencyContactModel
import com.rakshalink.ui.components.GlassBottomSheet
import com.rakshalink.ui.components.GlassCard
import com.rakshalink.ui.components.RakshaTopBar
import com.rakshalink.ui.theme.BackgroundDark
import com.rakshalink.ui.theme.CyanAccent
import com.rakshalink.ui.theme.GlassBorder
import com.rakshalink.ui.theme.PrimaryRed
import com.rakshalink.ui.theme.StatusSafe
import com.rakshalink.ui.theme.TextPrimary
import com.rakshalink.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.UUID

private fun sendRealVerificationSms(context: Context, rawPhoneNumber: String, otpCode: String): Pair<Boolean, String> {
    val cleanPhone = rawPhoneNumber.replace(Regex("[^0-9+]"), "")
    val message = "[RakshaLink] Emergency contact verification code: $otpCode. Valid for 5 minutes."

    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.SEND_SMS
    ) == PackageManager.PERMISSION_GRANTED

    android.util.Log.d("RakshaOTP", "[OTP Request Sent] Phone: '$cleanPhone', Code: '$otpCode', PermissionGranted: $hasPermission")

    if (!hasPermission) {
        android.util.Log.w("RakshaOTP", "[OTP Request Warning] SEND_SMS permission required.")
        return Pair(false, "PERMISSION_REQUIRED")
    }

    return try {
        val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
        smsManager.sendTextMessage(cleanPhone, null, message, null, null)
        android.util.Log.d("RakshaOTP", "[OTP Sent Success] SMS text message dispatched via SmsManager to $cleanPhone")
        Pair(true, "SMS OTP ($otpCode) automatically sent in background to $cleanPhone!")
    } catch (e: Exception) {
        android.util.Log.e("RakshaOTP", "[OTP Sent Exception] ${e.message}", e)
        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$cleanPhone")).apply {
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Pair(true, "Opened SMS app to send code $otpCode to $cleanPhone")
        } catch (ex: Exception) {
            Pair(false, "Failed to send SMS to $cleanPhone: ${e.localizedMessage}")
        }
    }
}

@Composable
fun EmergencyContactsScreen(
    viewModel: WearerViewModel,
    onBackClick: () -> Unit
) {
    val contacts by viewModel.contactsState.collectAsState()
    val context = LocalContext.current

    var showAddModal by remember { mutableStateOf(false) }
    var verifyingContactTarget by remember { mutableStateOf<EmergencyContactModel?>(null) }

    val verifiedCount = contacts.count { it.isVerified }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        RakshaTopBar(
            title = "Emergency Contacts",
            onBackClick = onBackClick,
            actions = {
                IconButton(onClick = { showAddModal = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Contact", tint = CyanAccent)
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Banner
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = "Safety", tint = StatusSafe, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AUTOMATIC SMS OTP ENGINE",
                                    color = StatusSafe,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$verifiedCount of ${contacts.size} contacts verified for instant background alert sync",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = { showAddModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = BackgroundDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add", color = BackgroundDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (contacts.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "No contacts",
                                tint = CyanAccent,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Emergency Contacts Added",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'Add' above to verify and save your real emergency contacts.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Contacts List
                items(contacts) { contact ->
                    ContactItemCard(
                        contact = contact,
                        onVerifyClick = {
                            verifyingContactTarget = contact
                        },
                        onCallClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phoneNumber}"))
                            context.startActivity(intent)
                        },
                        onSendTestSms = {
                            val sendIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("smsto:${contact.phoneNumber}")
                                putExtra("sms_body", "[RakshaLink Test Alert] Hi ${contact.name}, this is a test emergency contact message from RakshaLink protection service.")
                            }
                            try {
                                context.startActivity(sendIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Test SMS alert triggered for ${contact.name}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onSetPrimary = {
                            viewModel.setPrimaryContact(contact.id)
                            Toast.makeText(context, "${contact.name} set as primary contact", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteClick = {
                            viewModel.deleteContact(contact.id)
                            Toast.makeText(context, "Deleted ${contact.name}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Add Contact Modal (Step 1 -> Automatic SMS Step 2 OTP Verification)
        if (showAddModal) {
            AddContactWithVerificationModal(
                onSaveVerifiedContact = { newContact ->
                    viewModel.addContact(newContact)
                    showAddModal = false
                    Toast.makeText(context, "Emergency Contact ${newContact.name} verified & saved!", Toast.LENGTH_LONG).show()
                },
                onDismiss = { showAddModal = false }
            )
        }

        // Real Automatic OTP Verification Modal for Unverified Contacts
        verifyingContactTarget?.let { contactToVerify ->
            OtpVerificationModal(
                contactName = contactToVerify.name,
                phoneNumber = contactToVerify.phoneNumber,
                onVerificationSuccess = {
                    viewModel.verifyContact(contactToVerify.id)
                    verifyingContactTarget = null
                    Toast.makeText(context, "${contactToVerify.name} verified successfully!", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { verifyingContactTarget = null }
            )
        }
    }
}

@Composable
private fun ContactItemCard(
    contact: EmergencyContactModel,
    onVerifyClick: () -> Unit,
    onCallClick: () -> Unit,
    onSendTestSms: () -> Unit,
    onSetPrimary: () -> Unit,
    onDeleteClick: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (contact.isPrimary) CyanAccent.copy(alpha = 0.2f) else Color(0xFF0F293B))
                            .border(1.dp, if (contact.isPrimary) CyanAccent else Color(0xFF1E293B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.name.take(1).uppercase(),
                            color = if (contact.isPrimary) CyanAccent else TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(contact.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            if (contact.isPrimary) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyanAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("PRIMARY", color = CyanAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${contact.relationship} • ${contact.phoneNumber}", color = TextSecondary, fontSize = 12.sp)
                    }
                }

                // Primary Star Toggle
                IconButton(onClick = onSetPrimary) {
                    Icon(
                        imageVector = if (contact.isPrimary) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Set Primary",
                        tint = if (contact.isPrimary) CyanAccent else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Verification Pill & Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Pill
                if (contact.isVerified) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F2922))
                            .border(1.dp, StatusSafe.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = "Verified", tint = StatusSafe, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("VERIFIED ✓", color = StatusSafe, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF451A03))
                            .border(1.dp, Color(0xFFF97316).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { onVerifyClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Unverified", tint = Color(0xFFF97316), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("UNVERIFIED — TAP TO VERIFY", color = Color(0xFFF97316), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Call & Test Actions
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F2922))
                            .clickable { onCallClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = StatusSafe, modifier = Modifier.size(15.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F293B))
                            .clickable { onSendTestSms() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Message, contentDescription = "Test SMS", tint = CyanAccent, modifier = Modifier.size(15.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2D1214))
                            .clickable { onDeleteClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PrimaryRed, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddContactWithVerificationModal(
    onSaveVerifiedContact: (EmergencyContactModel) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var step by remember { mutableIntStateOf(1) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("Family") }
    var isPrimary by remember { mutableStateOf(false) }

    // Dynamic Real 4-Digit Generated OTP Code
    var realOtpCode by remember { mutableStateOf("") }

    // Runtime Permission Launcher for SEND_SMS
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && phone.isNotBlank() && realOtpCode.isNotBlank()) {
            val (_, msg) = sendRealVerificationSms(context, phone, realOtpCode)
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        } else {
            val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$cleanPhone")).apply {
                putExtra("sms_body", "[RakshaLink] Emergency contact verification code: $realOtpCode")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try { context.startActivity(intent) } catch (e: Exception) {}
        }
    }

    GlassBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            if (step == 1) {
                // STEP 1: Contact Form
                Text("Add Emergency Contact", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Enter contact details to receive instant SOS & Fall alerts.", color = TextSecondary, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Name (e.g. Ramesh Bhat)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = CyanAccent,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (e.g. +91 98450 12345)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = CyanAccent,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("Relationship (e.g. Father, Sister, Doctor)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = CyanAccent,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Set as Primary Contact", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Called first automatically during SOS", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = StatusSafe)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank()) {
                            val newOtp = (1000..9999).random().toString()
                            realOtpCode = newOtp
                            val (success, msg) = sendRealVerificationSms(context, phone, newOtp)
                            if (!success && msg == "PERMISSION_REQUIRED") {
                                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                            } else {
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                            step = 2
                        } else {
                            Toast.makeText(context, "Please enter both Contact Name and Phone Number", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Send SMS Verification Code ➔", color = BackgroundDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                // STEP 2: Real Automatic OTP Verification
                OtpVerificationContent(
                    contactName = name,
                    phoneNumber = phone,
                    correctOtp = realOtpCode,
                    onVerified = {
                        onSaveVerifiedContact(
                            EmergencyContactModel(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                phoneNumber = phone,
                                relationship = relationship,
                                isPrimary = isPrimary,
                                isVerified = true
                            )
                        )
                    },
                    onResendRequested = {
                        val newOtp = (1000..9999).random().toString()
                        realOtpCode = newOtp
                        val (success, msg) = sendRealVerificationSms(context, phone, newOtp)
                        if (!success && msg == "PERMISSION_REQUIRED") {
                            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                        } else {
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                        newOtp
                    },
                    onBackToStep1 = { step = 1 }
                )
            }
        }
    }
}

@Composable
private fun OtpVerificationModal(
    contactName: String,
    phoneNumber: String,
    onVerificationSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var realOtpCode by remember { mutableStateOf((1000..9999).random().toString()) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val (_, msg) = sendRealVerificationSms(context, phoneNumber, realOtpCode)
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        val (success, msg) = sendRealVerificationSms(context, phoneNumber, realOtpCode)
        if (!success && msg == "PERMISSION_REQUIRED") {
            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
        } else {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    GlassBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            OtpVerificationContent(
                contactName = contactName,
                phoneNumber = phoneNumber,
                correctOtp = realOtpCode,
                onVerified = onVerificationSuccess,
                onResendRequested = {
                    val newOtp = (1000..9999).random().toString()
                    realOtpCode = newOtp
                    val (success, msg) = sendRealVerificationSms(context, phoneNumber, newOtp)
                    if (!success && msg == "PERMISSION_REQUIRED") {
                        smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                    } else {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                    newOtp
                },
                onBackToStep1 = onDismiss
            )
        }
    }
}

@Composable
private fun OtpVerificationContent(
    contactName: String,
    phoneNumber: String,
    correctOtp: String,
    onVerified: () -> Unit,
    onResendRequested: () -> String,
    onBackToStep1: () -> Unit
) {
    var typedOtpCode by remember { mutableStateOf("") }
    var activeCorrectOtp by remember { mutableStateOf(correctOtp) }
    var errorMessage by remember { mutableStateOf("") }

    var timerSeconds by remember { mutableIntStateOf(30) }
    var isTimerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(correctOtp) {
        activeCorrectOtp = correctOtp
    }

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (timerSeconds > 0) {
                delay(1000L)
                timerSeconds -= 1
            }
            isTimerRunning = false
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("SMS Verification Code", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F2922))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("AUTOMATIC SMS SENT", color = StatusSafe, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "We automatically sent a 4-digit SMS verification code to $phoneNumber for $contactName. Enter the code below.",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 4-Box OTP Input Visual
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val digits = typedOtpCode.padEnd(4, ' ').take(4)
            digits.forEach { char ->
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0B132B))
                        .border(
                            1.5.dp,
                            if (errorMessage.isNotEmpty()) PrimaryRed else CyanAccent,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (char != ' ') char.toString() else "-",
                        color = if (errorMessage.isNotEmpty()) PrimaryRed else CyanAccent,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Real OTP Input Text Box
        OutlinedTextField(
            value = typedOtpCode,
            onValueChange = {
                if (it.length <= 4) {
                    typedOtpCode = it
                    errorMessage = ""
                }
            },
            label = { Text("Enter 4-digit SMS OTP", textAlign = TextAlign.Center) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (errorMessage.isNotEmpty()) PrimaryRed else CyanAccent,
                unfocusedBorderColor = if (errorMessage.isNotEmpty()) PrimaryRed else GlassBorder,
                focusedLabelColor = if (errorMessage.isNotEmpty()) PrimaryRed else CyanAccent,
                unfocusedLabelColor = TextSecondary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorMessage,
                color = PrimaryRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Resend Timer Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LockClock, contentDescription = "Timer", tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (timerSeconds > 0) "Resend code in ${timerSeconds}s" else "Code expired",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "Resend SMS",
                color = if (timerSeconds == 0) CyanAccent else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(enabled = timerSeconds == 0) {
                    val newCode = onResendRequested()
                    activeCorrectOtp = newCode
                    typedOtpCode = ""
                    errorMessage = ""
                    timerSeconds = 30
                    isTimerRunning = true
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Verify Button with Real Code Check
        Button(
            onClick = {
                val isMatch = (typedOtpCode == activeCorrectOtp)
                android.util.Log.d("RakshaOTP", "[OTP Comparison] Typed: '$typedOtpCode', Expected: '$activeCorrectOtp', ResultMatch: $isMatch")
                if (typedOtpCode.length < 4) {
                    errorMessage = "Please enter the complete 4-digit SMS OTP code."
                } else if (!isMatch) {
                    errorMessage = "Incorrect OTP code ($typedOtpCode)! Check your SMS and try again."
                } else {
                    errorMessage = ""
                    android.util.Log.d("RakshaOTP", "[OTP Verified Success] OTP match confirmed! Executing onVerified callback")
                    onVerified()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Verify", tint = BackgroundDark, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Verify & Save Contact ✓", color = BackgroundDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onBackToStep1,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
        ) {
            Text("Back / Cancel", fontSize = 13.sp)
        }
    }
}
