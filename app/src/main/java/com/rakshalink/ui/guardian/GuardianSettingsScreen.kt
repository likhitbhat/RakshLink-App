package com.rakshalink.ui.guardian

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakshalink.ui.components.GlassCard
import com.rakshalink.ui.theme.BackgroundDark
import com.rakshalink.ui.theme.CyanAccent
import com.rakshalink.ui.theme.PrimaryRed
import com.rakshalink.ui.theme.StatusSafe
import com.rakshalink.ui.theme.TextPrimary
import com.rakshalink.ui.theme.TextSecondary

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun GuardianSettingsScreen(
    viewModel: GuardianViewModel = hiltViewModel(),
    onNavigateToWearerDetail: (String) -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onSignOutClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val guardianInfo by viewModel.guardianInfo.collectAsState()
    val linkedWearers by viewModel.linkedWearersState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var pushEnabled by remember { mutableStateOf(true) }
    var quietHoursEnabled by remember { mutableStateOf(false) }
    var alertSoundsEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var volumeLevel by remember { mutableStateOf(0.8f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Guardian Profile Header Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(guardianInfo.first.take(1).uppercase(), color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(guardianInfo.first, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(guardianInfo.second, color = TextSecondary, fontSize = 12.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusSafe.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Active", color = StatusSafe, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // LINKED WEARERS Section
        Text("LINKED WEARERS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        Spacer(modifier = Modifier.height(8.dp))

        if (linkedWearers.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No wearers linked yet. Add a wearer using their pairing code.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            linkedWearers.forEach { wearer ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { onNavigateToWearerDetail(wearer.id) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(wearer.name.take(1).uppercase(), color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(wearer.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Battery: ${wearer.batteryLevel}% · ${wearer.statusText}", color = TextSecondary, fontSize = 11.sp)
                        }
                        IconButton(
                            onClick = {
                                viewModel.removeWearer(wearer.id) { success, msg ->
                                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Unlink Wearer",
                                tint = PrimaryRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // MONITORING Section
        Text("MONITORING", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        Spacer(modifier = Modifier.height(8.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                SettingsNavigationRow(
                    icon = Icons.Default.People,
                    title = "Watched wearers",
                    onClick = {
                        val activeId = linkedWearers.firstOrNull()?.id ?: ""
                        if (activeId.isNotEmpty()) {
                            onNavigateToWearerDetail(activeId)
                        } else {
                            onBackClick()
                        }
                    }
                )
                HorizontalDivider(color = Color(0xFF1E293B))
                SettingsNavigationRow(icon = Icons.Default.LocationOn, title = "Live map", onClick = onNavigateToMap)
                HorizontalDivider(color = Color(0xFF1E293B))
                SettingsNavigationRow(icon = Icons.Default.Notifications, title = "Alert history", onClick = onNavigateToAlerts)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // PREFERENCES Section
        Text("PREFERENCES", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        Spacer(modifier = Modifier.height(8.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                // Push Notifications item
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = "Push", tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Push notifications", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (pushEnabled) StatusSafe.copy(alpha = 0.2f) else Color(0xFF332005))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (pushEnabled) "ENABLED" else "MUTED",
                                        color = if (pushEnabled) StatusSafe else Color(0xFFFFA726),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text("Alerts for SOS, zone events & low battery", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = pushEnabled,
                            onCheckedChange = { pushEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = StatusSafe)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F1E2E))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (pushEnabled) "Real-time push notifications are active for all critical emergency pings." else "Notifications are muted. You will not receive pop-up alerts.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { pushEnabled = !pushEnabled },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(if (pushEnabled) "Disable" else "Enable", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF1E293B))

                // Quiet hours item
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.NightlightRound, contentDescription = "Quiet", tint = CyanAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Quiet hours & Low Energy mode", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Silence non-critical pings & save battery 10pm–7am", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = quietHoursEnabled,
                        onCheckedChange = { quietHoursEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = StatusSafe)
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293B))

                // Language item
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Language, contentDescription = "Language", tint = CyanAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Language", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("App display language", color = TextSecondary, fontSize = 12.sp)
                    }
                    Text("English ⌄", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ALERTS & FEEDBACK Section
        Text("ALERTS & FEEDBACK", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        Spacer(modifier = Modifier.height(8.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Sounds", tint = CyanAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Alert sounds", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Play tones for SOS, zone & alerts", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = alertSoundsEnabled,
                        onCheckedChange = { alertSoundsEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = StatusSafe)
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293B))

                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Vibration, contentDescription = "Vibration", tint = CyanAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vibration", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Haptic feedback for taps & alerts", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = StatusSafe)
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293B))

                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Volume", tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Volume", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Loudness of alert sounds", color = TextSecondary, fontSize = 12.sp)
                        }
                        Text("${(volumeLevel * 100).toInt()}%", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = volumeLevel,
                        onValueChange = { volumeLevel = it },
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293B))

                // Preview sound & vibration button
                OutlinedButton(
                    onClick = {
                        try {
                            if (alertSoundsEnabled) {
                                val notificationUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                                val ringtone = android.media.RingtoneManager.getRingtone(context, notificationUri)
                                ringtone?.play()
                            }
                            if (vibrationEnabled) {
                                val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                    val vm = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                                    vm?.defaultVibrator
                                } else {
                                    @Suppress("DEPRECATION")
                                    context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                                }
                                vibrator?.vibrate(android.os.VibrationEffect.createOneShot(300, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                            }
                            android.widget.Toast.makeText(context, "Testing alert sound & vibration feedback!", android.widget.Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) { e.printStackTrace() }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Preview", tint = TextPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Preview sound & vibration", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Out Button
        OutlinedButton(
            onClick = onSignOutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryRed.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = PrimaryRed.copy(alpha = 0.1f), contentColor = PrimaryRed)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Logout, contentDescription = "Sign out", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign out", color = PrimaryRed, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Footer version
        Text(
            text = "RakshaLink v1.0 · Protected Session",
            color = TextSecondary.copy(alpha = 0.6f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = CyanAccent, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = "Navigate", tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}
