package com.rakshalink.ui.wearer

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakshalink.ui.components.CriticalBatteryBanner
import com.rakshalink.ui.components.GlassCard
import com.rakshalink.ui.theme.BackgroundDark
import com.rakshalink.ui.theme.CyanAccent
import com.rakshalink.ui.theme.PrimaryRed
import com.rakshalink.ui.theme.StatusSafe
import com.rakshalink.ui.theme.TextPrimary
import com.rakshalink.ui.theme.TextSecondary

@Composable
fun WearerDashboardScreen(
    viewModel: WearerViewModel,
    onNavigateToTracking: () -> Unit,
    onNavigateToSafeZones: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToEmergencyActive: () -> Unit,
    onNavigateToPendantSettings: () -> Unit = {}
) {
    val uiState by viewModel.dashboardUiState.collectAsState()
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(Unit) {
        try {
            val serviceIntent = android.content.Intent(context, com.rakshalink.services.LocationForegroundService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.batteryLevel <= 10) {
            CriticalBatteryBanner(batteryLevel = uiState.batteryLevel)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Top Header Row (Greeting, User Name, Online pill, Avatar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = uiState.greeting,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = uiState.userName,
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F2922))
                            .border(1.dp, StatusSafe.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(StatusSafe)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Online",
                                color = StatusSafe,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF334155)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.userName.take(1).uppercase(),
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Voice SOS Listening Banner (if enabled)
        AnimatedVisibility(visible = uiState.isVoiceSosActive, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1E1B4B))
                    .border(1.dp, CyanAccent, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, contentDescription = "Mic", tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Voice SOS Listening...", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Say \"HELP\" or \"EMERGENCY\" to trigger SOS", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Text(
                        text = "ACTIVE",
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Live Dead-Man Safety Timer Banner (if enabled)
        AnimatedVisibility(visible = uiState.isDeadManActive, enter = fadeIn(), exit = fadeOut()) {
            val mins = uiState.deadManRemainingSeconds / 60
            val secs = uiState.deadManRemainingSeconds % 60
            val timeStr = String.format("%02d:%02d", mins, secs)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF450A0A))
                    .border(1.dp, PrimaryRed, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = "Timer", tint = PrimaryRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Dead-Man Safety Timer: $timeStr", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Check-in required before timer expires", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.checkInDeadMan()
                            Toast.makeText(context, "Checked in successfully! Timer reset.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("CHECK IN", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. SYSTEM STATUS Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "SYSTEM STATUS",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    StatusGridItem(
                        icon = Icons.Default.Wifi,
                        title = "Internet",
                        value = if (uiState.isNetworkActive) "Connected" else "Offline",
                        modifier = Modifier.weight(1f)
                    )
                    StatusGridItem(
                        icon = Icons.Default.LocationOn,
                        title = "GPS signal",
                        value = if (uiState.isGpsActive) "GPS strong" else "Locating...",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    StatusGridItem(
                        icon = Icons.Default.Sensors,
                        title = "Live updates",
                        value = "Live updates on",
                        modifier = Modifier.weight(1f)
                    )
                    StatusGridItem(
                        icon = Icons.Default.BatteryChargingFull,
                        title = "Phone battery",
                        value = "${uiState.phoneBatteryLevel}% · ${if (uiState.isPhoneCharging) "charging" else "battery"}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. SAFETY SCORE Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (uiState.safetyScore >= 75) Color(0xFF0F2922) else Color(0xFF451A03))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.safetyScore >= 75) StatusSafe else Color(0xFFF97316))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.safetyScore >= 75) "• YOU ARE SAFE" else "• CAUTION - CHECK SYSTEM",
                                color = if (uiState.safetyScore >= 75) StatusSafe else Color(0xFFF97316),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "${uiState.safetyScore} / 100 Safety Score",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateToTracking() }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.lastLocation != null)
                            "${uiState.lastLocation?.latitude}, ${uiState.lastLocation?.longitude} · Live GPS"
                        else
                            "12.97544, 77.59337 · Live GPS",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. 3 Stat Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                value = "${uiState.alertsThisMonth}",
                label = "Alerts this month",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = uiState.safeZoneStatusText.take(12),
                label = "Safe zone status",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = "${uiState.batteryLevel}%",
                label = "Device battery",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. PENDANT STATUS Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pendant Connected Card
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToPendantSettings() }
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pendant",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (uiState.isPendantConnected) Color(0xFF0F2922) else Color(0xFF1E293B))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (uiState.isPendantConnected) "Connected" else "Pair",
                                color = if (uiState.isPendantConnected) StatusSafe else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (uiState.isPendantConnected) "RL-A1B2 · Bluetooth LE active" else "Scan to pair pendant",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Pendant Battery Card
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToPendantSettings() }
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PENDANT BATTERY",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F2922))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "HEALTHY",
                                color = StatusSafe,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${uiState.batteryLevel}%",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { uiState.batteryLevel / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = StatusSafe,
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. QUICK ACTIONS Section
        Text(
            text = "QUICK ACTIONS",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // 6-Card Grid (3 rows x 2 columns)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionGridCard(
                    icon = Icons.Default.Shield,
                    iconTint = PrimaryRed,
                    title = "SOS",
                    subtitle = "Trigger emergency alert",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.onHoldSos()
                        onNavigateToEmergencyActive()
                    }
                )
                QuickActionGridCard(
                    icon = Icons.Default.LocationOn,
                    iconTint = CyanAccent,
                    title = "Track",
                    subtitle = "View live map & nearby help",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToTracking
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionGridCard(
                    icon = Icons.Default.Eco,
                    iconTint = StatusSafe,
                    title = "Zones",
                    subtitle = "Manage geofence safe areas",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSafeZones
                )
                QuickActionGridCard(
                    icon = Icons.Default.Bluetooth,
                    iconTint = CyanAccent,
                    title = "Pendant",
                    subtitle = "Hardware BLE device health",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToPendantSettings
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionGridCard(
                    icon = Icons.Default.Mic,
                    iconTint = if (uiState.isVoiceSosActive) PrimaryRed else CyanAccent,
                    title = "Voice SOS",
                    subtitle = if (uiState.isVoiceSosActive) "Listening active..." else "Hands-free voice detection",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.toggleVoiceSos()
                        val msg = if (!uiState.isVoiceSosActive) "Voice SOS listening started!" else "Voice SOS disabled."
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
                QuickActionGridCard(
                    icon = Icons.Default.Timer,
                    iconTint = if (uiState.isDeadManActive) PrimaryRed else Color(0xFFF59E0B),
                    title = "Dead-man",
                    subtitle = if (uiState.isDeadManActive) "Timer running!" else "Timed safety check-in",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.toggleDeadManTimer()
                        val msg = if (!uiState.isDeadManActive) "Dead-Man safety timer armed for 30 minutes!" else "Dead-Man safety timer disarmed."
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 7. RECENT ACTIVITY Section
        Text(
            text = "RECENT ACTIVITY",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B1D28)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Alert",
                            tint = PrimaryRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = uiState.recentActivityTitle,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.recentActivityTime,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = uiState.recentActivityStatus,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StatusGridItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = CyanAccent,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = value, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column {
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 10.sp,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun QuickActionGridCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}
