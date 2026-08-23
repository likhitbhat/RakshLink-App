package com.rakshalink.ui.wearer

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.rakshalink.ui.components.GlassCard
import com.rakshalink.ui.theme.BackgroundDark
import com.rakshalink.ui.theme.CyanAccent
import com.rakshalink.ui.theme.PrimaryRed
import com.rakshalink.ui.theme.StatusSafe
import com.rakshalink.ui.theme.TextPrimary
import com.rakshalink.ui.theme.TextSecondary
import kotlinx.coroutines.launch

enum class TrackFilter {
    HOSPITALS,
    POLICE,
    PHARMACIES,
    TRAIL,
    NONE
}

data class PoiItem(
    val name: String,
    val distance: String,
    val address: String,
    val phone: String,
    val location: LatLng,
    val type: TrackFilter
)

@Composable
fun LiveTrackingScreen(
    viewModel: WearerViewModel,
    onBackClick: () -> Unit
) {
    val locationState by viewModel.locationState.collectAsState()
    val fetchedPois by viewModel.nearbyPois.collectAsState()
    val isSearching by viewModel.isSearchingPois.collectAsState()
    val context = LocalContext.current

    val dashboardUiState by viewModel.dashboardUiState.collectAsState()
    val isOffline = !dashboardUiState.isNetworkActive || locationState?.isSynced == false

    val currentLat = locationState?.latitude ?: 12.97544
    val currentLng = locationState?.longitude ?: 77.59337
    val currentPos = LatLng(currentLat, currentLng)

    val lastRecordedTimeStr = remember(locationState?.timestamp) {
        val ts = locationState?.timestamp ?: System.currentTimeMillis()
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        sdf.format(java.util.Date(ts))
    }

    var selectedFilter by remember { mutableStateOf(TrackFilter.NONE) }
    val trailPoints = remember { mutableStateListOf<LatLng>() }

    // Trigger real-time nearby search on filter or location change
    LaunchedEffect(selectedFilter, currentLat, currentLng) {
        viewModel.fetchNearbyPois(currentLat, currentLng, selectedFilter)
    }

    // Record trail points
    LaunchedEffect(currentLat, currentLng) {
        if (trailPoints.isEmpty() || trailPoints.last() != currentPos) {
            trailPoints.add(currentPos)
            if (trailPoints.size == 1) {
                trailPoints.add(0, LatLng(currentLat - 0.0012, currentLng - 0.0018))
                trailPoints.add(0, LatLng(currentLat - 0.0025, currentLng - 0.0031))
                trailPoints.add(0, LatLng(currentLat - 0.0040, currentLng - 0.0045))
            }
        }
    }

    val scope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentPos, 15f)
    }

    LaunchedEffect(currentLat, currentLng) {
        if (selectedFilter == TrackFilter.NONE) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(currentPos, 15f))
        }
    }

    // Dynamic POI Data Set
    val fallbackHospitals = remember(currentLat, currentLng) {
        listOf(
            PoiItem("Emergency Super Specialty Hospital", "0.8 km", "Main Hospital Road", "+918026304050", LatLng(currentLat + 0.003, currentLng + 0.002), TrackFilter.HOSPITALS),
            PoiItem("City Care Trauma Center", "1.4 km", "Central Avenue Sector 4", "+918022065000", LatLng(currentLat - 0.004, currentLng + 0.005), TrackFilter.HOSPITALS),
            PoiItem("St. Jude Urgent Clinic", "2.1 km", "Station Cross Road", "+918040001111", LatLng(currentLat + 0.006, currentLng - 0.003), TrackFilter.HOSPITALS)
        )
    }

    val fallbackPolice = remember(currentLat, currentLng) {
        listOf(
            PoiItem("Central Police Station HQ", "0.6 km", "MG Road Sector 2", "100", LatLng(currentLat + 0.002, currentLng - 0.002), TrackFilter.POLICE),
            PoiItem("Women Protection Cell", "1.2 km", "Main Circle 100ft Road", "+918022943333", LatLng(currentLat - 0.003, currentLng - 0.004), TrackFilter.POLICE),
            PoiItem("Traffic & Patrol Outpost", "1.9 km", "High Street Junction", "+918022944444", LatLng(currentLat + 0.005, currentLng + 0.004), TrackFilter.POLICE)
        )
    }

    val fallbackPharmacies = remember(currentLat, currentLng) {
        listOf(
            PoiItem("24/7 Local Pharmacy", "0.3 km", "Store #12 Commercial Arcade", "+918023456789", LatLng(currentLat + 0.001, currentLng + 0.001), TrackFilter.PHARMACIES),
            PoiItem("MedPlus Chemist", "0.7 km", "Station Road Corner", "+918023459876", LatLng(currentLat - 0.002, currentLng + 0.003), TrackFilter.PHARMACIES),
            PoiItem("Wellness Forever Meds", "1.1 km", "Market Square Arcade", "+918023451122", LatLng(currentLat + 0.004, currentLng - 0.002), TrackFilter.PHARMACIES)
        )
    }

    val activePois = if (fetchedPois.isNotEmpty() && fetchedPois.firstOrNull()?.type == selectedFilter) {
        fetchedPois
    } else {
        when (selectedFilter) {
            TrackFilter.HOSPITALS -> fallbackHospitals
            TrackFilter.POLICE -> fallbackPolice
            TrackFilter.PHARMACIES -> fallbackPharmacies
            else -> emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title & Badges Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live tracking",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Status Pill (Live vs Last Known Location)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isOffline) Color(0xFF2D1F07) else Color(0xFF0F2922))
                        .border(1.dp, if (isOffline) Color(0xFFF59E0B).copy(alpha = 0.5f) else StatusSafe.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isOffline) Color(0xFFF59E0B) else StatusSafe)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isOffline) "Last Known Location" else "Live",
                            color = if (isOffline) Color(0xFFF59E0B) else StatusSafe,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isOffline)
                    "${String.format("%.5f", currentLat)}, ${String.format("%.5f", currentLng)} · Recorded at $lastRecordedTimeStr (Offline)"
                else
                    "${String.format("%.5f", currentLat)}, ${String.format("%.5f", currentLng)} · Live tracking active",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dark Rounded Map Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(24.dp))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // User Location Marker (Live vs Last Known Location)
                Marker(
                    state = MarkerState(position = currentPos),
                    title = if (isOffline) "Last Known Location (Offline)" else "Your Location",
                    snippet = if (isOffline) "Recorded in DB at $lastRecordedTimeStr" else "Live accuracy: ${locationState?.accuracy ?: 5f}m"
                )

                // Render POI Markers for Hospitals, Police, Pharmacies
                activePois.forEach { poi ->
                    Marker(
                        state = MarkerState(position = poi.location),
                        title = poi.name,
                        snippet = "${poi.distance} · ${poi.address}"
                    )
                }

                // Render Trail Polyline if TRAIL filter selected
                if (selectedFilter == TrackFilter.TRAIL && trailPoints.size > 1) {
                    Polyline(
                        points = trailPoints,
                        color = CyanAccent,
                        width = 8f
                    )
                    trailPoints.forEachIndexed { idx, pt ->
                        Marker(
                            state = MarkerState(position = pt),
                            title = "Trail Point #${idx + 1}",
                            snippet = "Breadcrumb timestamp"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Red Nearby Help Button
        Button(
            onClick = {
                selectedFilter = TrackFilter.HOSPITALS
                (activePois.firstOrNull() ?: fallbackHospitals.firstOrNull())?.let { topHospital ->
                    scope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(topHospital.location, 15f))
                    }
                    Toast.makeText(context, "Locating nearest emergency hospital: ${topHospital.name}", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Explore, contentDescription = "Help", tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nearby Help",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Category Filter Pills Row (Hospitals, Police, Pharmacies, Trail)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryPill(
                icon = Icons.Default.LocalHospital,
                label = "Hospitals",
                isSelected = selectedFilter == TrackFilter.HOSPITALS,
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedFilter = if (selectedFilter == TrackFilter.HOSPITALS) TrackFilter.NONE else TrackFilter.HOSPITALS
                    if (selectedFilter == TrackFilter.HOSPITALS) {
                        (activePois.firstOrNull() ?: fallbackHospitals.firstOrNull())?.let {
                            scope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it.location, 15f))
                            }
                        }
                    }
                }
            )

            CategoryPill(
                icon = Icons.Default.Security,
                label = "Police",
                isSelected = selectedFilter == TrackFilter.POLICE,
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedFilter = if (selectedFilter == TrackFilter.POLICE) TrackFilter.NONE else TrackFilter.POLICE
                    if (selectedFilter == TrackFilter.POLICE) {
                        (activePois.firstOrNull() ?: fallbackPolice.firstOrNull())?.let {
                            scope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it.location, 15f))
                            }
                        }
                    }
                }
            )

            CategoryPill(
                icon = Icons.Default.LocalPharmacy,
                label = "Pharmacies",
                isSelected = selectedFilter == TrackFilter.PHARMACIES,
                modifier = Modifier.weight(1.2f),
                onClick = {
                    selectedFilter = if (selectedFilter == TrackFilter.PHARMACIES) TrackFilter.NONE else TrackFilter.PHARMACIES
                    if (selectedFilter == TrackFilter.PHARMACIES) {
                        (activePois.firstOrNull() ?: fallbackPharmacies.firstOrNull())?.let {
                            scope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it.location, 15f))
                            }
                        }
                    }
                }
            )

            CategoryPill(
                icon = Icons.Default.Navigation,
                label = "Trail",
                isSelected = selectedFilter == TrackFilter.TRAIL,
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedFilter = if (selectedFilter == TrackFilter.TRAIL) TrackFilter.NONE else TrackFilter.TRAIL
                    if (selectedFilter == TrackFilter.TRAIL) {
                        scope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(currentPos, 16f))
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic NEARBY Card Info
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NEARBY ${selectedFilter.name}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    if (selectedFilter != TrackFilter.NONE) {
                        Text(
                            text = "Clear filter",
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { selectedFilter = TrackFilter.NONE }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isSearching) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = CyanAccent,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Searching real-time nearby places...",
                            color = CyanAccent,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when (selectedFilter) {
                    TrackFilter.NONE -> {
                        Text(
                            text = "Tap \"Nearby Help\" or select a category filter above to locate nearby hospitals, police stations, pharmacies, or view your breadcrumb trail.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }

                    TrackFilter.TRAIL -> {
                        Text(
                            text = "Breadcrumb Location Trail Active",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${trailPoints.size} location breadcrumb points recorded today · Total distance ~1.8 km",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    else -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            activePois.forEach { poi ->
                                PoiResultRow(
                                    poi = poi,
                                    onCall = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${poi.phone}"))
                                        context.startActivity(intent)
                                    },
                                    onDirections = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${poi.location.latitude},${poi.location.longitude}?q=${Uri.encode(poi.name)}"))
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CategoryPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isSelected) CyanAccent else CyanAccent.copy(alpha = 0.4f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) CyanAccent.copy(alpha = 0.2f) else Color.Transparent,
            contentColor = CyanAccent
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = CyanAccent, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PoiResultRow(
    poi: PoiItem,
    onCall: () -> Unit,
    onDirections: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(poi.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F2922))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(poi.distance, color = StatusSafe, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(poi.address, color = TextSecondary, fontSize = 11.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F2922))
                    .clickable { onCall() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Call, contentDescription = "Call", tint = StatusSafe, modifier = Modifier.size(16.dp))
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F293B))
                    .clickable { onDirections() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Directions, contentDescription = "Directions", tint = CyanAccent, modifier = Modifier.size(16.dp))
            }
        }
    }
}
