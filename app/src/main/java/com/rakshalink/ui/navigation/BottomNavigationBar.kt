package com.rakshalink.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rakshalink.domain.model.UserRole
import com.rakshalink.ui.theme.BackgroundDark
import com.rakshalink.ui.theme.CyanAccent
import com.rakshalink.ui.theme.GlassBorder
import com.rakshalink.ui.theme.PrimaryRed
import com.rakshalink.ui.theme.TextMuted
import com.rakshalink.ui.theme.TextPrimary

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector,
    val isSosButton: Boolean = false
)

val WearerNavItems = listOf(
    BottomNavItem("Home", Screen.WearerDashboard.route, Icons.Default.Home),
    BottomNavItem("Track", Screen.LiveTracking.route, Icons.Default.Map),
    BottomNavItem("SOS", Screen.EmergencyActive.route, Icons.Default.Shield, isSosButton = true),
    BottomNavItem("Device", Screen.PendantSettings.route, Icons.Default.Bluetooth),
    BottomNavItem("More", Screen.WearerSettings.route, Icons.Default.Settings)
)

val GuardianNavItems = listOf(
    BottomNavItem("Watch", Screen.GuardianDashboard.route, Icons.Default.People),
    BottomNavItem("Map", Screen.GuardianLiveMap.route, Icons.Default.Map),
    BottomNavItem("Alerts", Screen.AlertInbox.route, Icons.Default.Notifications),
    BottomNavItem("More", Screen.GuardianSettings.route, Icons.Default.Settings)
)

@Composable
fun RakshaBottomNavigationBar(
    navController: NavController,
    userRole: UserRole
) {
    val items = if (userRole == UserRole.WEARER) WearerNavItems else GuardianNavItems
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark.copy(alpha = 0.95f)),
        containerColor = BackgroundDark.copy(alpha = 0.95f),
        contentColor = TextPrimary
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route

            if (item.isSosButton) {
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route)
                        }
                    },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .offset(y = (-4).dp)
                                .clip(CircleShape)
                                .background(PrimaryRed)
                                .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = "SOS",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    label = { },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )
            } else {
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected) CyanAccent else TextMuted
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            color = if (isSelected) CyanAccent else TextMuted,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = GlassBorder
                    )
                )
            }
        }
    }
}
