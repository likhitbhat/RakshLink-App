package com.rakshalink.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakshalink.ui.components.GlassButton
import com.rakshalink.ui.components.GlassCard
import com.rakshalink.ui.components.RakshaTopBar
import com.rakshalink.ui.theme.BackgroundDark
import com.rakshalink.ui.theme.CyanAccent
import com.rakshalink.ui.theme.TextPrimary
import com.rakshalink.ui.theme.TextSecondary

@Composable
fun PermissionsScreen(
    onPermissionsGranted: () -> Unit
) {
    val permissionsToRequest = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        add(Manifest.permission.SEND_SMS)
        add(Manifest.permission.RECORD_AUDIO)
        add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }.toTypedArray()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        onPermissionsGranted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        RakshaTopBar(title = "App Permissions")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Required Permissions",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "RakshaLink requires permission access for continuous emergency tracking, voice activation, emergency camera snapshots, fall detection, and BLE pendant connectivity.",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            PermissionItemCard(
                icon = Icons.Default.LocationOn,
                title = "Location Access (Fine & Coarse)",
                description = "Required for real GPS tracking, emergency location sharing, and safe zone geofencing."
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionItemCard(
                icon = Icons.Default.Mic,
                title = "Voice & Microphone",
                description = "Enables hands-free voice emergency activation during critical situations."
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionItemCard(
                icon = Icons.Default.CameraAlt,
                title = "Camera Access",
                description = "Used to attach emergency visual evidence during active SOS events."
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionItemCard(
                icon = Icons.Default.Notifications,
                title = "Push Notifications",
                description = "High-importance emergency channels for SOS alerts, fall warnings, and battery alerts."
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionItemCard(
                icon = Icons.Default.Bluetooth,
                title = "Bluetooth & Nearby Devices",
                description = "Scans and maintains connection with your physical BLE RakshaLink safety pendant."
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionItemCard(
                icon = Icons.Default.Sensors,
                title = "Sensor Access",
                description = "Uses accelerometer & gyroscope for automatic impact and fall detection."
            )

            Spacer(modifier = Modifier.height(24.dp))

            GlassButton(
                onClick = {
                    permissionLauncher.launch(permissionsToRequest)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Grant Permissions & Continue")
            }
        }
    }
}

@Composable
private fun PermissionItemCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = CyanAccent
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
