package com.rakshalink.ui.wearer

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakshalink.domain.model.SosState
import com.rakshalink.ui.components.GlassCard
import com.rakshalink.ui.theme.BackgroundDark
import com.rakshalink.ui.theme.CyanAccent
import com.rakshalink.ui.theme.PrimaryRed
import com.rakshalink.ui.theme.StatusSafe
import com.rakshalink.ui.theme.TextPrimary
import com.rakshalink.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EmergencyActiveScreen(
    viewModel: WearerViewModel,
    onNavigateBack: () -> Unit
) {
    val sosState by viewModel.sosState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showFalseAlarmDialog by remember { mutableStateOf(false) }
    var activeTimerSeconds by remember { mutableStateOf(0) }
    var smsSentCount by remember { mutableStateOf(0) }

    val pulseScale = remember { Animatable(1.0f) }
    val pulseAlpha = remember { Animatable(0.3f) }

    val isActive = sosState is SosState.Active || sosState is SosState.Armed || sosState is SosState.Confirmation

    // Pulsing Glow Animations
    LaunchedEffect(Unit) {
        pulseScale.animateTo(
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(Unit) {
        pulseAlpha.animateTo(
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    // Active Timer & Automatic Real SMS Dispatch
    LaunchedEffect(isActive) {
        if (isActive) {
            // Trigger physical alarm sound & vibration
            viewModel.playTestFeedbackSoundAndVibration()

            // Real SMS Manager Dispatch
            viewModel.dispatchEmergencySms(context) { count, message ->
                smsSentCount = count
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }

            activeTimerSeconds = 0
            while (true) {
                delay(1000L)
                activeTimerSeconds += 1
            }
        }
    }

    val activeDurationFormatted = remember(activeTimerSeconds) {
        val mins = activeTimerSeconds / 60
        val secs = activeTimerSeconds % 60
        String.format("%02d:%02d", mins, secs)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title Header & Active Status
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isActive) "EMERGENCY SOS ACTIVE" else "SOS",
                color = if (isActive) PrimaryRed else TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isActive)
                    "Active Duration: $activeDurationFormatted · Real SMS & Live GPS Broadcasting"
                else
                    "Press & hold for 1.5s to dispatch Emergency SMS to contacts",
                color = if (isActive) PrimaryRed.copy(alpha = 0.9f) else TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large Central Pulsing HOLD / ACTIVE SOS Button
        Box(contentAlignment = Alignment.Center) {
            // Outer Pulsing Glow Circle
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .graphicsLayer {
                        scaleX = pulseScale.value
                        scaleY = pulseScale.value
                        alpha = pulseAlpha.value
                    }
                    .clip(CircleShape)
                    .background(PrimaryRed.copy(alpha = 0.35f))
            )

            // Inner Dark Circle Ring
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1B0B1E)),
                contentAlignment = Alignment.Center
            ) {
                // Red Solid HOLD / ACTIVE Button
                Box(
                    modifier = Modifier
                        .size(165.dp)
                        .clip(CircleShape)
                        .background(PrimaryRed)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    if (!isActive) {
                                        val job = scope.launch {
                                            delay(1200L)
                                            viewModel.triggerActiveSos()
                                        }
                                        tryAwaitRelease()
                                        job.cancel()
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isActive) Icons.Default.Warning else Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isActive) "ACTIVE" else "HOLD",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Status Card: Dispatched Actions & Emergency Readiness
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = if (isActive) "EMERGENCY BROADCAST STATUS" else "WHEN TRIGGERED",
                    color = if (isActive) CyanAccent else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (isActive) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusRow(
                            icon = Icons.Default.Phone,
                            label = "SMS Dispatched to $smsSentCount Emergency Contacts",
                            isSuccess = smsSentCount > 0
                        )
                        StatusRow(
                            icon = Icons.Default.LocationOn,
                            label = "Live GPS Link Shared (Google Maps)",
                            isSuccess = true
                        )
                        StatusRow(
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            label = "Emergency Siren & Vibration Active",
                            isSuccess = true
                        )
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TriggeredGridItem(icon = Icons.Default.Phone, label = "SMS to contacts", modifier = Modifier.weight(1f))
                        TriggeredGridItem(icon = Icons.Default.LocationOn, label = "Live GPS share", modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TriggeredGridItem(icon = Icons.Default.Mic, label = "Audio recording", modifier = Modifier.weight(1f))
                        TriggeredGridItem(icon = Icons.AutoMirrored.Filled.VolumeUp, label = "Loud siren", modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Cancel SOS / False Alarm Action Button
        if (isActive) {
            Button(
                onClick = { showFalseAlarmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancel SOS (False Alarm)", color = PrimaryRed, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Text("Back to Safety Dashboard", color = TextSecondary, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // False Alarm Confirmation Modal
        if (showFalseAlarmDialog) {
            AlertDialog(
                onDismissRequest = { showFalseAlarmDialog = false },
                title = { Text("Cancel Emergency SOS?") },
                text = { Text("This will stop the siren alarm and notify your emergency contacts that the alert was resolved.") },
                confirmButton = {
                    TextButton(onClick = {
                        val alertId = (sosState as? SosState.Active)?.alertId ?: ""
                        viewModel.cancelActiveSos(alertId, wasFalseAlarm = true)
                        showFalseAlarmDialog = false
                        Toast.makeText(context, "SOS Alert Cancelled. Contacts notified.", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Confirm Cancel (False Alarm)", color = PrimaryRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFalseAlarmDialog = false }) {
                        Text("Keep SOS Active", color = CyanAccent)
                    }
                }
            )
        }
    }
}

@Composable
private fun StatusRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSuccess: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isSuccess) Icons.Default.CheckCircle else icon,
            contentDescription = label,
            tint = if (isSuccess) StatusSafe else PrimaryRed,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TriggeredGridItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = CyanAccent,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
