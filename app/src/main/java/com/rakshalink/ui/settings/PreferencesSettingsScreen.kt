package com.rakshalink.ui.settings

import android.content.Context
import android.media.AudioManager
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.google.firebase.messaging.FirebaseMessaging
import com.rakshalink.data.preferences.UserPreferencesManager
import com.rakshalink.data.remote.dto.UserPreferencesDto
import com.rakshalink.data.remote.supabase.SupabaseClientProvider
import com.rakshalink.ui.components.GlassCard
import com.rakshalink.ui.components.RakshaTopBar
import com.rakshalink.ui.theme.BackgroundDark
import com.rakshalink.ui.theme.CyanAccent
import com.rakshalink.ui.theme.GlassBorder
import com.rakshalink.ui.theme.PrimaryRed
import com.rakshalink.ui.theme.StatusSafe
import com.rakshalink.ui.theme.TextPrimary
import com.rakshalink.ui.theme.TextSecondary
import com.rakshalink.ui.wearer.WearerViewModel
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PreferencesSettingsScreen(
    viewModel: WearerViewModel,
    preferencesManager: UserPreferencesManager,
    supabaseProvider: SupabaseClientProvider,
    onBackClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isPushEnabled by remember { mutableStateOf(true) }
    var isShareLocationEnabled by remember { mutableStateOf(true) }
    var isDarkMode by remember { mutableStateOf(true) }
    var isSoundEnabled by remember { mutableStateOf(true) }
    var isVibrationEnabled by remember { mutableStateOf(true) }
    var alertVolume by remember { mutableFloatStateOf(80f) }

    // Fetch initial user preferences from Supabase & DataStore
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val userId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""
            if (userId.isNotEmpty()) {
                try {
                    val remotePref = supabaseProvider.db.from("user_preferences")
                        .select {
                            filter { eq("user_id", userId) }
                        }.decodeSingleOrNull<UserPreferencesDto>()

                    if (remotePref != null) {
                        isPushEnabled = remotePref.pushEnabled
                        isShareLocationEnabled = remotePref.shareLocationEnabled
                        isDarkMode = remotePref.theme == "dark"
                        isSoundEnabled = remotePref.alertSoundEnabled
                        isVibrationEnabled = remotePref.vibrationEnabled
                        alertVolume = remotePref.alertVolume.toFloat()

                        // Sync to local DataStore
                        preferencesManager.setPushEnabled(remotePref.pushEnabled)
                        preferencesManager.setShareLocationEnabled(remotePref.shareLocationEnabled)
                        preferencesManager.setTheme(remotePref.theme)
                        preferencesManager.setSoundEnabled(remotePref.alertSoundEnabled)
                        preferencesManager.setVibrationEnabled(remotePref.vibrationEnabled)
                        preferencesManager.setAlertVolume(remotePref.alertVolume)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Helper to sync preferences to Supabase & DataStore
    fun syncPreferences(
        push: Boolean = isPushEnabled,
        shareLoc: Boolean = isShareLocationEnabled,
        dark: Boolean = isDarkMode,
        sound: Boolean = isSoundEnabled,
        vib: Boolean = isVibrationEnabled,
        vol: Float = alertVolume
    ) {
        scope.launch(Dispatchers.IO) {
            val userId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""
            val themeStr = if (dark) "dark" else "light"

            // Local DataStore
            preferencesManager.setPushEnabled(push)
            preferencesManager.setShareLocationEnabled(shareLoc)
            preferencesManager.setTheme(themeStr)
            preferencesManager.setSoundEnabled(sound)
            preferencesManager.setVibrationEnabled(vib)
            preferencesManager.setAlertVolume(vol.toInt())

            // Supabase Database Sync
            if (userId.isNotEmpty()) {
                try {
                    val dto = UserPreferencesDto(
                        userId = userId,
                        pushEnabled = push,
                        shareLocationEnabled = shareLoc,
                        theme = themeStr,
                        language = "en",
                        alertSoundEnabled = sound,
                        vibrationEnabled = vib,
                        alertVolume = vol.toInt()
                    )
                    supabaseProvider.db.from("user_preferences").upsert(dto)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        RakshaTopBar(
            title = "Preferences & Settings",
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 1: SYSTEM & NOTIFICATION PREFERENCES
        SectionTitle("NOTIFICATION & SAFETY PREFERENCES")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                // 1. Push Notifications Toggle (FCM Topic Subscribe / Unsubscribe)
                SettingToggleRow(
                    icon = Icons.Default.Notifications,
                    title = "Push notifications",
                    subtitle = "Receive live emergency SOS & fall alerts",
                    isChecked = isPushEnabled,
                    onCheckedChange = { enabled ->
                        isPushEnabled = enabled
                        val userId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: "global"
                        if (enabled) {
                            try { FirebaseMessaging.getInstance().subscribeToTopic("user_$userId") } catch (e: Exception) {}
                            Toast.makeText(context, "FCM Push Notifications Subscribed", Toast.LENGTH_SHORT).show()
                        } else {
                            try { FirebaseMessaging.getInstance().unsubscribeFromTopic("user_$userId") } catch (e: Exception) {}
                            Toast.makeText(context, "FCM Push Notifications Unsubscribed", Toast.LENGTH_SHORT).show()
                        }
                        syncPreferences(push = enabled)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Share Live Location Toggle (When OFF, LocationService stops writing to locations table)
                SettingToggleRow(
                    icon = Icons.Default.LocationOn,
                    title = "Share live location",
                    subtitle = if (isShareLocationEnabled)
                        "Active · Location updates written to Supabase"
                    else
                        "OFF · Location updates completely STOPPED to Supabase",
                    isChecked = isShareLocationEnabled,
                    onCheckedChange = { enabled ->
                        isShareLocationEnabled = enabled
                        syncPreferences(shareLoc = enabled)
                        val msg = if (enabled) "Live location sharing enabled." else "Location sharing disabled. Supabase location writes STOPPED."
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Theme Toggle (Persists to DataStore & Supabase)
                SettingToggleRow(
                    icon = Icons.Default.NightlightRound,
                    title = "Dark theme",
                    subtitle = "Persists across reinstalls via Supabase sync",
                    isChecked = isDarkMode,
                    onCheckedChange = { enabled ->
                        isDarkMode = enabled
                        syncPreferences(dark = enabled)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Language Row (Static English Label)
                // FUTURE_LOCALE_INSERTION_POINT: Add LocaleSwitcher dropdown here when multi-language support (es, hi, fr) is implemented
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = "Language", tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Language", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("English (Default)", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F293B))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("ENGLISH ONLY", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 2: ALARM SOUND & AUDIO CONTROLS
        SectionTitle("ALARM SOUND & AUDIO CONTROLS")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                SettingToggleRow(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = "Alert sound",
                    subtitle = "Loud siren sound during active SOS trigger",
                    isChecked = isSoundEnabled,
                    onCheckedChange = { enabled ->
                        isSoundEnabled = enabled
                        syncPreferences(sound = enabled)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingToggleRow(
                    icon = Icons.Default.Vibration,
                    title = "Haptic vibration",
                    subtitle = "Vibration pattern during emergency alarms",
                    isChecked = isVibrationEnabled,
                    onCheckedChange = { enabled ->
                        isVibrationEnabled = enabled
                        syncPreferences(vib = enabled)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Alert Volume Slider (Writes to AudioManager.STREAM_ALARM)
                Text(
                    text = "Alert Volume: ${alertVolume.toInt()}%",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Controls physical system STREAM_ALARM volume",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Slider(
                    value = alertVolume,
                    onValueChange = { newVol ->
                        alertVolume = newVol
                        // Write to AudioManager STREAM_ALARM
                        try {
                            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                            val targetVol = (maxVol * (newVol / 100f)).toInt()
                            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, targetVol, 0)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    onValueChangeFinished = {
                        syncPreferences(vol = alertVolume)
                    },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanAccent,
                        activeTrackColor = CyanAccent,
                        inactiveTrackColor = GlassBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 6. Test Alarm Sound & Vibration Button (Triggers real EmergencyActiveScreen siren & vibrator)
                Button(
                    onClick = {
                        viewModel.playTestFeedbackSoundAndVibration()
                        Toast.makeText(context, "🔊 Testing real Emergency Alarm Sound & Vibrator!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Test", tint = StatusSafe, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Alarm Sound & Vibration 🔊", color = StatusSafe, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SECTION 3: SIGN OUT ACTION
        // 7. Sign Out: Clears DataStore, unsubscribes FCM, invalidates Supabase session server-side
        OutlinedButton(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    val userId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""
                    // Unsubscribe FCM
                    if (userId.isNotEmpty()) {
                        try { FirebaseMessaging.getInstance().unsubscribeFromTopic("user_$userId") } catch (e: Exception) {}
                    }
                    // Invalidate Supabase session
                    try { supabaseProvider.auth.signOut() } catch (e: Exception) {}
                    // Clear DataStore session
                    preferencesManager.clearAuthSession()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Signed out successfully.", Toast.LENGTH_SHORT).show()
                        onSignOutClick()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, PrimaryRed.copy(alpha = 0.6f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryRed)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out", tint = PrimaryRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of RakshaLink", color = PrimaryRed, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = title, tint = CyanAccent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextSecondary, fontSize = 11.sp)
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = StatusSafe)
        )
    }
}
