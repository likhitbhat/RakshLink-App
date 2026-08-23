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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakshalink.domain.model.SafeZoneModel
import com.rakshalink.ui.components.EmptyState
import com.rakshalink.ui.components.GlassButton
import com.rakshalink.ui.components.GlassCard
import com.rakshalink.ui.components.GlassBottomSheet
import com.rakshalink.ui.components.RakshaTopBar
import com.rakshalink.ui.theme.BackgroundDark
import com.rakshalink.ui.theme.CyanAccent
import com.rakshalink.ui.theme.GlassBorder
import com.rakshalink.ui.theme.PrimaryRed
import com.rakshalink.ui.theme.TextPrimary
import com.rakshalink.ui.theme.TextSecondary

@Composable
fun SafeZonesScreen(
    viewModel: WearerViewModel,
    onBackClick: () -> Unit
) {
    val safeZones by viewModel.safeZonesState.collectAsState()
    var showAddModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        RakshaTopBar(
            title = "Safe Zones",
            onBackClick = onBackClick,
            actions = {
                IconButton(onClick = { showAddModal = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Safe Zone", tint = CyanAccent)
                }
            }
        )

        if (safeZones.isEmpty()) {
            EmptyState(
                title = "No Safe Zones Configured",
                description = "Create geofenced safe zones (e.g. Home, Office, School). Guardians receive alerts when you enter or exit.",
                icon = Icons.Default.Security,
                actionText = "Create First Safe Zone",
                onActionClick = { showAddModal = true },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(safeZones) { zone ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = zone.name, tint = CyanAccent)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(zone.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Radius: ${zone.radiusMeters.toInt()} meters", color = TextSecondary, fontSize = 12.sp)
                            }
                            IconButton(onClick = { viewModel.deleteSafeZone(zone.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PrimaryRed)
                            }
                        }
                    }
                }
            }
        }

        if (showAddModal) {
            AddSafeZoneBottomSheet(
                onAdd = { newZone ->
                    viewModel.addSafeZone(newZone)
                    showAddModal = false
                },
                onDismiss = { showAddModal = false }
            )
        }
    }
}

@Composable
private fun AddSafeZoneBottomSheet(
    onAdd: (SafeZoneModel) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var radius by remember { mutableFloatStateOf(200f) }

    GlassBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("Create Safe Zone", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Zone Name (e.g. Home)") },
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

            Text("Geofence Radius: ${radius.toInt()} meters", color = TextSecondary, fontSize = 14.sp)
            Slider(
                value = radius,
                onValueChange = { radius = it },
                valueRange = 50f..1000f,
                colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
            )

            Spacer(modifier = Modifier.height(24.dp))

            GlassButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(
                            SafeZoneModel(
                                name = name,
                                latitude = 12.9716,
                                longitude = 77.5946,
                                radiusMeters = radius
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Safe Zone")
            }
        }
    }
}
