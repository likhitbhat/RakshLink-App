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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakshalink.ui.components.GlassCard
import com.rakshalink.ui.components.RakshaTopBar
import com.rakshalink.ui.theme.BackgroundDark
import com.rakshalink.ui.theme.CyanAccent
import com.rakshalink.ui.theme.PrimaryRed
import com.rakshalink.ui.theme.TextPrimary
import com.rakshalink.ui.theme.TextSecondary

data class HistoryEventItem(
    val id: String,
    val title: String,
    val description: String,
    val timeAgo: String,
    val isEmergency: Boolean
)

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit
) {
    val historyItems = listOf(
        HistoryEventItem("1", "EMERGENCY SOS ALERT", "SOS button triggered near MG Road", "2 hours ago", true),
        HistoryEventItem("2", "Safe Zone Exit", "Exited Home Safe Zone", "5 hours ago", false),
        HistoryEventItem("3", "Safe Zone Entry", "Entered College Safe Zone", "6 hours ago", false),
        HistoryEventItem("4", "BLE Pendant Connected", "Hardware pendant synced via Bluetooth LE", "1 day ago", false)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        RakshaTopBar(title = "Safety Event History", onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(historyItems) { item ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (item.isEmergency) Icons.Default.Warning else Icons.Default.History,
                            contentDescription = item.title,
                            tint = if (item.isEmergency) PrimaryRed else CyanAccent
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(item.description, color = TextSecondary, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(item.timeAgo, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
