package com.rakshalink.ui.guardian

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
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakshalink.ui.components.GlassButton
import com.rakshalink.ui.components.GlassCard
import com.rakshalink.ui.components.RakshaTopBar
import com.rakshalink.ui.theme.BackgroundDark
import com.rakshalink.ui.theme.CyanAccent
import com.rakshalink.ui.theme.StatusSafe
import com.rakshalink.ui.theme.TextPrimary
import com.rakshalink.ui.theme.TextSecondary

@Composable
fun WearerDetailScreen(
    wearerId: String,
    viewModel: GuardianViewModel,
    onBackClick: () -> Unit
) {
    val wearers by viewModel.linkedWearersState.collectAsState()
    val wearer = wearers.find { it.id == wearerId } ?: wearers.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        RakshaTopBar(title = wearer?.name ?: "Wearer Details", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = "Wearer", tint = CyanAccent)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(wearer?.name ?: "Priya Sharma", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(wearer?.email ?: "priya@example.com", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = "Status", tint = StatusSafe)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Current Protection Status", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(wearer?.statusText ?: "Protected", color = StatusSafe, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BatteryFull, contentDescription = "Battery", tint = CyanAccent)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Hardware Pendant Battery", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("${wearer?.batteryLevel ?: 88}% Charged", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            GlassButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Request Immediate Location Ping")
            }
        }
    }
}
