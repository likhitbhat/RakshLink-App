package com.rakshalink.ui.wearer

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakshalink.ui.components.GlassCard
import com.rakshalink.ui.theme.BackgroundDark
import com.rakshalink.ui.theme.CyanAccent
import com.rakshalink.ui.theme.PrimaryRed
import com.rakshalink.ui.theme.StatusSafe
import com.rakshalink.ui.theme.TextPrimary
import com.rakshalink.ui.theme.TextSecondary

@Composable
fun WearerSettingsScreen(
    viewModel: WearerViewModel,
    onNavigateToContacts: () -> Unit = {},
    onNavigateToSafeZones: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val dashboardState by viewModel.dashboardUiState.collectAsState()
    val guardiansList by viewModel.guardiansList.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var inviteInput by remember { mutableStateOf("") }
    var pushEnabled by remember { mutableStateOf(true) }
    var shareLocationEnabled by remember { mutableStateOf(true) }
    var darkThemeSelected by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("English") }

    var alertSoundsEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var volumeLevel by remember { mutableFloatStateOf(0.85f) }
    var fromTime by remember { mutableStateOf("22:00") }
    var toTime by remember { mutableStateOf("07:00") }

    val wearerId = dashboardState.wearerPairingCode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Profile Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.2f))
                            .border(1.5.dp, CyanAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dashboardState.userName.take(1).uppercase(),
                            color = CyanAccent,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = dashboardState.userName,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dashboardState.wearerEmail,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F2922))
                        .border(1.dp, StatusSafe.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("ACTIVE WEARER", color = StatusSafe, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // GUARDIAN PAIRING Section (Unique Wearer ID)
        Text(
            text = "GUARDIAN PAIRING CODE",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Your Unique Wearer ID", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Share this unique ID with your Guardian so they can pair and monitor your safety.", color = TextSecondary, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0B132B))
                        .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("WEARER PAIRING ID", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text(wearerId, color = CyanAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Copy Button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F293B))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(wearerId))
                                    Toast.makeText(context, "Wearer ID copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyanAccent, modifier = Modifier.size(16.dp))
                        }

                        // Share Button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.2f))
                                .clickable {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Hello! My RakshaLink Wearer Pairing ID is: $wearerId. Enter this in your Guardian app to link with me.")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Wearer ID"))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = CyanAccent, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // CONNECTED GUARDIANS Section
        Text(
            text = "CONNECTED GUARDIANS (${guardiansList.size})",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                if (guardiansList.isEmpty()) {
                    Text("No guardians connected yet. Invite a guardian below using their email or phone number.", color = TextSecondary, fontSize = 12.sp)
                } else {
                    guardiansList.forEachIndexed { index, guardian ->
                        GuardianRow(
                            guardian = guardian,
                            onRemove = {
                                viewModel.removeGuardian(guardian.id)
                                Toast.makeText(context, "Removed ${guardian.name} from guardians", Toast.LENGTH_SHORT).show()
                            }
                        )
                        if (index < guardiansList.size - 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Invite Form
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inviteInput,
                        onValueChange = { inviteInput = it },
                        placeholder = { Text("Guardian email or phone", color = TextSecondary, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (inviteInput.isNotBlank()) {
                                viewModel.inviteGuardian(inviteInput) { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    inviteInput = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Invite", tint = BackgroundDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", color = BackgroundDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SAFETY QUICK OPTIONS Section
        Text(
            text = "SAFETY QUICK OPTIONS",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                SafetyOptionRow(
                    icon = Icons.Default.Phone,
                    label = "Emergency contacts",
                    onClick = onNavigateToContacts
                )
                Spacer(modifier = Modifier.height(12.dp))
                SafetyOptionRow(
                    icon = Icons.Default.Eco,
                    label = "Safe zones & Geofences",
                    onClick = onNavigateToSafeZones
                )
                Spacer(modifier = Modifier.height(12.dp))
                SafetyOptionRow(
                    icon = Icons.Default.History,
                    label = "Emergency history & Logs",
                    onClick = onNavigateToHistory
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // PREFERENCES & APP SETTINGS Section
        Text(
            text = "PREFERENCES & SECURITY",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                // Push Notifications
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = "Push", tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Push notifications", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (pushEnabled) Color(0xFF0F2922) else Color(0xFF451A03))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        if (pushEnabled) "ENABLED" else "NOT ENABLED",
                                        color = if (pushEnabled) StatusSafe else Color(0xFFF97316),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text("SOS, safe-zone & emergency alerts", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Switch(
                        checked = pushEnabled,
                        onCheckedChange = {
                            pushEnabled = it
                            Toast.makeText(context, if (it) "Notifications enabled" else "Notifications disabled", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = StatusSafe)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Share live location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = "Share", tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Share live location", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Allow active guardians to track live GPS", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Switch(
                        checked = shareLocationEnabled,
                        onCheckedChange = {
                            shareLocationEnabled = it
                            Toast.makeText(context, if (it) "Live location sharing ON" else "Location sharing PAUSED", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = StatusSafe)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Theme Mode Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NightlightRound, contentDescription = "Theme", tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("App Theme", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(if (darkThemeSelected) "Dark Cyber Mode" else "Light Mode", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0F172A))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (darkThemeSelected) CyanAccent else Color.Transparent)
                                .clickable { darkThemeSelected = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Dark", color = if (darkThemeSelected) BackgroundDark else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (!darkThemeSelected) CyanAccent else Color.Transparent)
                                .clickable { darkThemeSelected = false }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Light", color = if (!darkThemeSelected) BackgroundDark else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Language Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = "Language", tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Language", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("System display language", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                            .clickable {
                                selectedLanguage = when (selectedLanguage) {
                                    "English" -> "Hindi"
                                    "Hindi" -> "Kannada"
                                    "Kannada" -> "Tamil"
                                    "Tamil" -> "Telugu"
                                    else -> "English"
                                }
                                Toast.makeText(context, "Language set to $selectedLanguage", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("$selectedLanguage ⌄", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ALERTS & SOUND FEEDBACK Section
        Text(
            text = "ALERTS & SOUND FEEDBACK",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Alert sounds", tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Alert sounds", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Play emergency siren for SOS & fall alerts", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = alertSoundsEnabled,
                        onCheckedChange = { alertSoundsEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = StatusSafe)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = "Vibration", tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Vibration feedback", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Haptic pulse feedback during alerts", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = StatusSafe)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Volume slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Volume", tint = CyanAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Alert volume", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Siren audio volume level", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                        Text("${(volumeLevel * 100).toInt()}%", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = volumeLevel,
                        onValueChange = { volumeLevel = it },
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real Test Sound & Vibration Button
                OutlinedButton(
                    onClick = {
                        viewModel.playTestFeedbackSoundAndVibration()
                        Toast.makeText(context, "Testing alarm sound & haptic vibration...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Preview", tint = CyanAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Alarm Sound & Haptic Vibration", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Red Outlined Sign out Button
        OutlinedButton(
            onClick = {
                onSignOutClick()
                Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, PrimaryRed.copy(alpha = 0.6f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryRed)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign out", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", color = PrimaryRed, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "RakshaLink v1.0.0 · Live Protection Active",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun GuardianRow(
    guardian: GuardianModel,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F293B)),
                contentAlignment = Alignment.Center
            ) {
                Text(guardian.name.take(1).uppercase(), color = CyanAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(guardian.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (guardian.isPrimary) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyanAccent.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("PRIMARY", color = CyanAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text("${guardian.email} · ${guardian.phone}", color = TextSecondary, fontSize = 11.sp)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F2922))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(guardian.status, color = StatusSafe, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = PrimaryRed.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onRemove() }
            )
        }
    }
}

@Composable
private fun SafetyOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = CyanAccent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}
