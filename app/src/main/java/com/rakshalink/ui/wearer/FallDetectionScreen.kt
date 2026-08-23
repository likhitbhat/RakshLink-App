package com.rakshalink.ui.wearer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakshalink.domain.model.FallState
import com.rakshalink.ui.components.GlassButton
import com.rakshalink.ui.components.GlassCard
import com.rakshalink.ui.components.GlassOutlinedButton
import com.rakshalink.ui.components.GlassBottomSheet
import com.rakshalink.ui.components.RakshaTopBar
import com.rakshalink.ui.theme.BackgroundDark
import com.rakshalink.ui.theme.CyanAccent
import com.rakshalink.ui.theme.PrimaryRed
import com.rakshalink.ui.theme.StatusSafe
import com.rakshalink.ui.theme.TextPrimary
import com.rakshalink.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FallDetectionScreen(
    viewModel: WearerViewModel,
    onBackClick: () -> Unit
) {
    val fallState by viewModel.fallState.collectAsState()
    var isMonitoringEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        RakshaTopBar(title = "Fall Detection", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Sensors, contentDescription = "Sensors", tint = CyanAccent)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Automatic Fall Sensor", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Monitors accelerometer & gyro for heavy impact + stillness", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = isMonitoringEnabled,
                        onCheckedChange = { isMonitoringEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = StatusSafe)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("SENSOR DIAGNOSTICS", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Impact Sensitivity: High (G-Force > 2.5g)", color = TextPrimary, fontSize = 14.sp)
                    Text("Stillness Threshold: 10 seconds immobile", color = TextPrimary, fontSize = 14.sp)
                    Text("Countdown Duration: 15 seconds before auto SOS", color = TextPrimary, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Disclaimer: Fall detection is a secondary safety heuristic and not a medical-grade diagnostic tool.",
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            GlassOutlinedButton(
                onClick = { viewModel.simulateFallImpact() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simulate Fall Event Test")
            }
        }

        if (fallState is FallState.Countdown || fallState is FallState.PossibleFallDetected) {
            val seconds = if (fallState is FallState.Countdown) (fallState as FallState.Countdown).secondsRemaining else 15
            GlassBottomSheet(onDismissRequest = { viewModel.cancelFall() }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Possible Fall", tint = PrimaryRed)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Possible Fall Detected", color = PrimaryRed, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Triggering SOS alert in $seconds seconds...", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        GlassButton(
                            onClick = { viewModel.cancelFall() },
                            modifier = Modifier.weight(1f),
                            containerColor = StatusSafe
                        ) {
                            Text("I'm Okay")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        GlassButton(
                            onClick = { viewModel.triggerActiveSos() },
                            modifier = Modifier.weight(1f),
                            containerColor = PrimaryRed
                        ) {
                            Text("Send SOS Now")
                        }
                    }
                }
            }
        }
    }
}
