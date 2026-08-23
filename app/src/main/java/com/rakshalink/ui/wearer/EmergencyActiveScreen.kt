package com.rakshalink.ui.wearer

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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakshalink.ui.components.GlassCard
import com.rakshalink.ui.theme.BackgroundDark
import com.rakshalink.ui.theme.CyanAccent
import com.rakshalink.ui.theme.PrimaryRed
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
    var showFalseAlarmDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val pulseScale = remember { Animatable(1.0f) }
    val pulseAlpha = remember { Animatable(0.3f) }

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

        // Title Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "SOS",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Press & hold to alert your guardians",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large Central Pulsing HOLD SOS Button
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
                // Red Solid HOLD Button
                Box(
                    modifier = Modifier
                        .size(165.dp)
                        .clip(CircleShape)
                        .background(PrimaryRed)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    val isPressed = true
                                    val job = scope.launch {
                                        delay(1500L)
                                        viewModel.triggerActiveSos()
                                        showFalseAlarmDialog = true
                                    }
                                    tryAwaitRelease()
                                    job.cancel()
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "HOLD",
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

        // WHEN TRIGGERED Info Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "WHEN TRIGGERED",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2x2 Grid Items
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

        Spacer(modifier = Modifier.height(24.dp))

        // False Alarm Feedback Dialog
        if (showFalseAlarmDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Emergency Alert Sent") },
                text = { Text("Your Guardians have been alerted with your live location. Was this a test or false alarm?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.cancelActiveSos("", wasFalseAlarm = true)
                        showFalseAlarmDialog = false
                    }) {
                        Text("Cancel SOS (False Alarm)")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showFalseAlarmDialog = false
                    }) {
                        Text("Keep SOS Active")
                    }
                }
            )
        }
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
