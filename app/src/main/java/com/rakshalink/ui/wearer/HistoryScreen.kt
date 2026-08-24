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

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size

data class HistoryEventItem(
    val id: String,
    val title: String,
    val description: String,
    val timeAgo: String,
    val isEmergency: Boolean
)

@Composable
fun HistoryScreen(
    viewModel: WearerViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val historyItems by viewModel.safetyEventHistory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        RakshaTopBar(title = "Safety Event History", onBackClick = onBackClick)

        if (historyItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "No Events",
                        tint = TextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No safety events recorded yet",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Emergency SOS alerts and safe zone events will appear here in real time.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
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
}
