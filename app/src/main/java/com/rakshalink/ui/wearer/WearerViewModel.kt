package com.rakshalink.ui.wearer

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakshalink.domain.model.EmergencyContactModel
import com.rakshalink.domain.model.FallState
import com.rakshalink.domain.model.LocationModel
import com.rakshalink.domain.model.PendantConnectionState
import com.rakshalink.domain.model.SafeZoneModel
import com.rakshalink.domain.model.SosState
import com.rakshalink.domain.repository.AuthRepository
import com.rakshalink.domain.repository.BlePendantRepository
import com.rakshalink.domain.repository.EmergencyContactRepository
import com.rakshalink.domain.repository.LocationRepository
import com.rakshalink.domain.repository.SafeZoneRepository
import com.rakshalink.domain.repository.SosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import com.rakshalink.data.remote.dto.EmergencyAlertDto
import com.rakshalink.data.remote.dto.ZoneEventDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Dispatchers
import java.util.UUID

data class GuardianModel(
    val id: String,
    val name: String,
    val email: String,
    val phone: String = "+91 98765 43210",
    val isPrimary: Boolean = false,
    val status: String = "ACTIVE"
)

data class WearerDashboardUiState(
    val userName: String = "Likhit Bhat",
    val wearerEmail: String = "likhit@rakshalink.com",
    val wearerPairingCode: String = "RL-9842-WK",
    val greeting: String = "GOOD EVENING",
    val isProtected: Boolean = true,
    val safetyScore: Int = 85,
    val isGpsActive: Boolean = true,
    val isNetworkActive: Boolean = true,
    val isPendantConnected: Boolean = true,
    val batteryLevel: Int = 78,
    val phoneBatteryLevel: Int = 100,
    val isPhoneCharging: Boolean = true,
    val guardianCount: Int = 2,
    val alertsThisMonth: Int = 1,
    val isInsideSafeZone: Boolean = false,
    val safeZoneStatusText: String = "Outside zones",
    val isVoiceSosActive: Boolean = false,
    val isDeadManActive: Boolean = false,
    val deadManRemainingSeconds: Int = 1800,
    val lastLocation: LocationModel? = null,
    val recentActivityTitle: String = "Sos Alert",
    val recentActivityTime: String = "8/11/2026, 1:44:59 PM",
    val recentActivityStatus: String = "CANCELLED",
    val isLoading: Boolean = false
)

@HiltViewModel
class WearerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sosRepository: SosRepository,
    private val locationRepository: LocationRepository,
    private val safeZoneRepository: SafeZoneRepository,
    private val bleRepository: BlePendantRepository,
    private val contactRepository: EmergencyContactRepository,
    private val authRepository: AuthRepository,
    private val nearbyPlacesRepository: com.rakshalink.data.repository.NearbyPlacesRepository,
    private val fallDetectionManager: com.rakshalink.services.FallDetectionManager,
    private val supabaseProvider: com.rakshalink.data.remote.supabase.SupabaseClientProvider,
    private val twilioAuthApi: com.rakshalink.data.remote.api.TwilioAuthApi,
    private val userPreferencesManager: com.rakshalink.data.preferences.UserPreferencesManager
) : ViewModel() {

    private val _userInfo = MutableStateFlow(Pair("Wearer User", "wearer@rakshalink.com"))
    val userInfo: StateFlow<Pair<String, String>> = _userInfo.asStateFlow()

    private val _userPairingCode = MutableStateFlow("RL-9842-WK")
    val userPairingCode: StateFlow<String> = _userPairingCode.asStateFlow()

    private fun loadUserInfo() {
        viewModelScope.launch {
            val supabaseUser = try { supabaseProvider.auth.currentSessionOrNull()?.user } catch (e: Exception) { null }
            val supabaseEmail = supabaseUser?.email ?: ""
            val supabasePhone = supabaseUser?.phone ?: ""
            val storedEmailOrPhone = try { userPreferencesManager.userPhoneOrEmailFlow.first() } catch (e: Exception) { "" }
            val storedUserId = try { userPreferencesManager.userIdFlow.first() } catch (e: Exception) { "" }
            val activeUid = if (supabaseUser?.id?.isNotEmpty() == true) supabaseUser.id else storedUserId

            // Query Supabase for authentic user profile
            val dbProfile = if (activeUid.isNotEmpty()) {
                try {
                    supabaseProvider.db.from("users")
                        .select(columns = Columns.ALL) { filter { eq("id", activeUid) } }
                        .decodeSingleOrNull<com.rakshalink.data.remote.dto.UserProfileDto>()
                } catch (e: Exception) { null }
            } else null

            val realEmail = when {
                !dbProfile?.email.isNullOrBlank() -> dbProfile!!.email
                supabaseEmail.isNotEmpty() -> supabaseEmail
                storedEmailOrPhone.contains("@") -> storedEmailOrPhone
                else -> if (supabasePhone.isNotEmpty()) supabasePhone else storedEmailOrPhone.ifBlank { "User Account" }
            }

            val rawName = when {
                !dbProfile?.full_name.isNullOrBlank() -> dbProfile!!.full_name
                realEmail.contains("@") -> realEmail.substringBefore("@")
                    .split(".", "_", "-")
                    .joinToString(" ") { word -> word.lowercase().replaceFirstChar { char -> char.uppercase() } }
                else -> realEmail
            }

            _userInfo.value = Pair(rawName, realEmail)

            if (activeUid.isNotEmpty()) {
                val codeHash = activeUid.take(4).uppercase()
                _userPairingCode.value = "RL-$codeHash-WK"
            }
        }
    }

    private val _nearbyPois = MutableStateFlow<List<PoiItem>>(emptyList())
    val nearbyPois: StateFlow<List<PoiItem>> = _nearbyPois.asStateFlow()

    private val _isSearchingPois = MutableStateFlow(false)
    val isSearchingPois: StateFlow<Boolean> = _isSearchingPois.asStateFlow()

    fun fetchNearbyPois(lat: Double, lng: Double, filter: TrackFilter) {
        if (filter == TrackFilter.NONE || filter == TrackFilter.TRAIL) {
            _nearbyPois.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearchingPois.value = true
            val pois = nearbyPlacesRepository.fetchNearbyPlaces(lat, lng, filter)
            _nearbyPois.value = pois
            _isSearchingPois.value = false
        }
    }

    suspend fun sendTwilioOtp(phone: String): com.rakshalink.data.remote.dto.SendOtpResponse {
        return twilioAuthApi.sendOtp(phone)
    }

    suspend fun verifyTwilioOtp(phone: String, otp: String): com.rakshalink.data.remote.dto.VerifyOtpResponse {
        return twilioAuthApi.verifyOtp(phone, otp)
    }

    private val _guardiansList = MutableStateFlow<List<GuardianModel>>(emptyList())
    val guardiansList: StateFlow<List<GuardianModel>> = _guardiansList.asStateFlow()

    private fun getFallbackGuardians(): List<GuardianModel> {
        return emptyList()
    }

    private val defaultContacts = emptyList<EmergencyContactModel>()

    private val _contactsList = MutableStateFlow<List<EmergencyContactModel>>(defaultContacts)
    val contactsState: StateFlow<List<EmergencyContactModel>> = _contactsList.asStateFlow()

    private fun listenToEmergencyContacts() {
        viewModelScope.launch {
            contactRepository.getEmergencyContacts().collect { list ->
                _contactsList.value = list
            }
        }
    }

    private fun listenToRealtimeGuardians() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val currentUserId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""

            suspend fun fetchActiveGuardians(): List<GuardianModel> {
                if (currentUserId.isEmpty()) return getFallbackGuardians()
                return try {
                    val links: List<com.rakshalink.data.remote.dto.WearerGuardianLinkDto> = try {
                        supabaseProvider.db.from("wearer_guardian_links")
                            .select(columns = io.github.jan.supabase.postgrest.query.Columns.ALL) {
                                filter {
                                    eq("wearer_id", currentUserId)
                                    eq("status", "active")
                                }
                            }.decodeList<com.rakshalink.data.remote.dto.WearerGuardianLinkDto>()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    if (links.isEmpty()) return getFallbackGuardians()

                    links.mapIndexed { idx, link ->
                        GuardianModel(
                            id = link.guardianId.ifBlank { "g_$idx" },
                            name = "Guardian ${idx + 1}",
                            email = "guardian${idx + 1}@rakshalink.com",
                            phone = "+91 98450 12345",
                            isPrimary = link.role == "primary",
                            status = link.status.uppercase()
                        )
                    }
                } catch (e: Exception) {
                    getFallbackGuardians()
                }
            }

            _guardiansList.value = fetchActiveGuardians()

            try {
                val channel = supabaseProvider.realtime.channel("wearer_guardians_realtime")
                val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "wearer_guardian_links"
                }
                channel.subscribe()
                changes.collect {
                    val updated = fetchActiveGuardians()
                    _guardiansList.value = updated
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun inviteGuardian(emailOrPhone: String, onSuccess: (String) -> Unit) {
        if (emailOrPhone.isBlank()) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val currentUserId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""
            val inviteId = java.util.UUID.randomUUID().toString()

            try {
                if (currentUserId.isNotEmpty()) {
                    val inviteDto = com.rakshalink.data.remote.dto.GuardianInviteDto(
                        id = inviteId,
                        wearerId = currentUserId,
                        inviteeContact = emailOrPhone,
                        status = "pending",
                        createdAt = java.time.Instant.now().toString(),
                        expiresAt = java.time.Instant.now().plusSeconds(48 * 3600).toString()
                    )
                    supabaseProvider.db.from("guardian_invites").insert(inviteDto)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val res = twilioAuthApi.sendGuardianInvite(
                wearerId = currentUserId,
                wearerName = "Likhit Bhat",
                inviteeContact = emailOrPhone,
                inviteId = inviteId
            )

            val newLocal = GuardianModel(
                id = inviteId,
                name = if (emailOrPhone.contains("@")) emailOrPhone.substringBefore("@") else emailOrPhone,
                email = if (emailOrPhone.contains("@")) emailOrPhone else "$emailOrPhone@rakshalink.com",
                phone = if (emailOrPhone.contains("@")) "+91 98450 12345" else emailOrPhone,
                isPrimary = false,
                status = "PENDING"
            )
            _guardiansList.value = _guardiansList.value + newLocal
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onSuccess(res.message.ifBlank { "Guardian invitation sent to $emailOrPhone via SMS! (Expires in 48 hours)" })
            }
        }
    }

    fun removeGuardian(id: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val currentUserId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""
            try {
                if (currentUserId.isNotEmpty()) {
                    supabaseProvider.db.from("wearer_guardian_links")
                        .update(mapOf("status" to "removed")) {
                            filter {
                                eq("wearer_id", currentUserId)
                                eq("guardian_id", id)
                            }
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _guardiansList.value = _guardiansList.value.filter { it.id != id }
        }
    }

    fun playTestFeedbackSoundAndVibration() {
        try {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator?.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }

            // Play notification ringtone chime
            val notificationUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = android.media.RingtoneManager.getRingtone(context, notificationUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val sosState: StateFlow<SosState> = sosRepository.sosState

    val locationState: StateFlow<LocationModel?> = locationRepository.getLatestLocation()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val safeZonesState: StateFlow<List<SafeZoneModel>> = safeZoneRepository.getActiveSafeZones()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())



    val pendantConnectionState: StateFlow<PendantConnectionState> = bleRepository.connectionState
    val pendantBattery: StateFlow<Int> = bleRepository.batteryLevel

    val fallState: StateFlow<FallState> = fallDetectionManager.fallState

    private val _isVoiceSosActive = MutableStateFlow(false)
    val isVoiceSosActive: StateFlow<Boolean> = _isVoiceSosActive.asStateFlow()

    private val _isDeadManActive = MutableStateFlow(false)
    val isDeadManActive: StateFlow<Boolean> = _isDeadManActive.asStateFlow()

    private val _deadManSeconds = MutableStateFlow(1800)
    val deadManSeconds: StateFlow<Int> = _deadManSeconds.asStateFlow()

    private var deadManJob: Job? = null

    private val _safetyEventHistory = MutableStateFlow<List<HistoryEventItem>>(emptyList())
    val safetyEventHistory: StateFlow<List<HistoryEventItem>> = _safetyEventHistory.asStateFlow()

    private suspend fun resolveCurrentUserId(): String {
        val supabaseUid = try { supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: "" } catch (e: Exception) { "" }
        if (supabaseUid.isNotEmpty()) return supabaseUid
        val storedUid = try { userPreferencesManager.userIdFlow.first() } catch (e: Exception) { "" }
        if (storedUid.isNotEmpty()) return storedUid
        return ""
    }

    private fun listenToSafetyHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserId = resolveCurrentUserId()
            if (currentUserId.isEmpty()) return@launch

            suspend fun fetchEvents(): List<HistoryEventItem> {
                val items = mutableListOf<HistoryEventItem>()
                try {
                    val alerts = supabaseProvider.db.from("emergency_alerts")
                        .select(columns = Columns.ALL) {
                            filter { eq("wearer_id", currentUserId) }
                        }.decodeList<EmergencyAlertDto>()

                    alerts.forEach { alert ->
                        items.add(
                            HistoryEventItem(
                                id = alert.id,
                                title = if (alert.title.isNotEmpty()) alert.title else "EMERGENCY SOS ALERT",
                                description = if (alert.message.isNotEmpty()) alert.message else "Emergency alert triggered",
                                timeAgo = if (alert.createdAt.isNotEmpty()) alert.createdAt else "Recently",
                                isEmergency = true
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Fallback
                }

                try {
                    val zoneEvs = supabaseProvider.db.from("zone_events")
                        .select(columns = Columns.ALL) {
                            filter { eq("user_id", currentUserId) }
                        }.decodeList<ZoneEventDto>()

                    zoneEvs.forEach { ze ->
                        val isEnter = ze.eventType.equals("enter", ignoreCase = true)
                        items.add(
                            HistoryEventItem(
                                id = if (ze.id.isNotEmpty()) ze.id else UUID.randomUUID().toString(),
                                title = if (isEnter) "Safe Zone Entry" else "Safe Zone Exit",
                                description = if (isEnter) "Entered designated safe zone" else "Exited designated safe zone",
                                timeAgo = if (ze.createdAt.isNotEmpty()) ze.createdAt else "Recently",
                                isEmergency = false
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Fallback
                }

                return items.sortedByDescending { it.id }
            }

            _safetyEventHistory.value = fetchEvents()

            try {
                val alertsChannel = supabaseProvider.client.realtime.channel("realtime-history-$currentUserId")
                val changeFlow = alertsChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "emergency_alerts"
                }
                alertsChannel.subscribe()
                changeFlow.collect {
                    _safetyEventHistory.value = fetchEvents()
                }
            } catch (e: Exception) {
                // Realtime fallback
            }
        }
    }

    init {
        loadUserInfo()
        fallDetectionManager.startMonitoring()
        listenToRealtimeGuardians()
        listenToEmergencyContacts()
        listenToSafetyHistory()
    }

    val dashboardUiState: StateFlow<WearerDashboardUiState> = combine(
        locationState,
        pendantBattery,
        pendantConnectionState,
        safeZonesState,
        _isVoiceSosActive,
        _isDeadManActive,
        _deadManSeconds,
        _userInfo,
        _userPairingCode,
        guardiansList,
        safetyEventHistory
    ) { args: Array<Any?> ->
        val loc = args[0] as? LocationModel
        val pBattery = (args[1] as? Int) ?: 78
        val pConnection = (args[2] as? PendantConnectionState) ?: PendantConnectionState.CONNECTED
        @Suppress("UNCHECKED_CAST")
        val safeZones = (args[3] as? List<SafeZoneModel>) ?: emptyList()
        val voiceSos = (args[4] as? Boolean) ?: false
        val deadMan = (args[5] as? Boolean) ?: false
        val dSecs = (args[6] as? Int) ?: 1800
        @Suppress("UNCHECKED_CAST")
        val info = (args[7] as? Pair<String, String>) ?: Pair("Wearer User", "wearer@rakshalink.com")
        val code = (args[8] as? String) ?: "RL-9842-WK"
        @Suppress("UNCHECKED_CAST")
        val guardians = (args[9] as? List<GuardianModel>) ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val historyEvents = (args[10] as? List<HistoryEventItem>) ?: emptyList()

        // Calculate dynamic Greeting
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val greetingText = when {
            hour in 5..11 -> "GOOD MORNING"
            hour in 12..16 -> "GOOD AFTERNOON"
            hour in 17..20 -> "GOOD EVENING"
            else -> "GOOD NIGHT"
        }

        // Calculate Phone Battery
        val (phoneBat, isCharging) = getPhoneBatteryInfo()

        // Evaluate Safe Zone Geofence
        val isInsideZone = isLocationInAnyZone(loc, safeZones)
        val zoneStatus = if (isInsideZone) "Inside Safe Zone" else "Outside zones"

        // Calculate Dynamic Safety Score (0 - 100)
        var score = 0
        if (loc != null) score += 25
        if (pConnection == PendantConnectionState.CONNECTED) score += 25
        if (phoneBat > 20) score += 25
        if (safeZones.isNotEmpty() && isInsideZone) score += 25 else if (loc != null) score += 10

        val recentEvent = historyEvents.firstOrNull()
        val recentTitle = recentEvent?.title ?: "System Safe"
        val recentTime = recentEvent?.timeAgo ?: "No alerts recorded"
        val recentStatus = if (recentEvent == null) "SAFE" else if (recentEvent.isEmergency) "ALERT" else "NORMAL"

        WearerDashboardUiState(
            userName = info.first,
            wearerEmail = info.second,
            wearerPairingCode = code,
            greeting = greetingText,
            isProtected = score >= 50,
            safetyScore = score,
            isGpsActive = loc != null,
            isNetworkActive = true,
            isPendantConnected = pConnection == PendantConnectionState.CONNECTED,
            batteryLevel = pBattery,
            phoneBatteryLevel = phoneBat,
            isPhoneCharging = isCharging,
            guardianCount = guardians.size,
            alertsThisMonth = historyEvents.count { it.isEmergency },
            isInsideSafeZone = isInsideZone,
            safeZoneStatusText = zoneStatus,
            isVoiceSosActive = voiceSos,
            isDeadManActive = deadMan,
            deadManRemainingSeconds = dSecs,
            lastLocation = loc,
            recentActivityTitle = recentTitle,
            recentActivityTime = recentTime,
            recentActivityStatus = recentStatus,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        WearerDashboardUiState(isLoading = false)
    )

    private fun getPhoneBatteryInfo(): Pair<Int, Boolean> {
        return try {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            val pct = if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else 95
            Pair(pct, isCharging)
        } catch (e: Exception) {
            Pair(95, true)
        }
    }

    private fun isLocationInAnyZone(location: LocationModel?, zones: List<SafeZoneModel>): Boolean {
        if (location == null || zones.isEmpty()) return false
        for (zone in zones) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                location.latitude, location.longitude,
                zone.latitude, zone.longitude,
                results
            )
            if (results[0] <= zone.radiusMeters) {
                return true
            }
        }
        return false
    }

    // Interactive Voice SOS Toggle
    fun toggleVoiceSos() {
        _isVoiceSosActive.value = !_isVoiceSosActive.value
    }

    // Interactive Dead-Man Timer Toggle & Check-in
    fun toggleDeadManTimer() {
        if (_isDeadManActive.value) {
            _isDeadManActive.value = false
            deadManJob?.cancel()
            _deadManSeconds.value = 1800
        } else {
            _isDeadManActive.value = true
            _deadManSeconds.value = 1800
            startDeadManCountdown()
        }
    }

    fun checkInDeadMan() {
        _deadManSeconds.value = 1800
    }

    private fun startDeadManCountdown() {
        deadManJob?.cancel()
        deadManJob = viewModelScope.launch {
            while (_isDeadManActive.value && _deadManSeconds.value > 0) {
                delay(1000L)
                _deadManSeconds.value -= 1
            }
            if (_isDeadManActive.value && _deadManSeconds.value <= 0) {
                triggerActiveSos()
            }
        }
    }

    // SOS Actions
    fun onHoldSos() {
        sosRepository.armSos()
        sosRepository.showConfirmation()
    }

    fun triggerActiveSos() {
        viewModelScope.launch {
            val loc = locationState.value
            sosRepository.triggerActiveSos(loc?.latitude, loc?.longitude)
        }
    }

    private fun getReverseGeocodedAddress(lat: Double, lng: Double): String {
        return try {
            val geocoder = android.location.Geocoder(context)
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            val addr = addresses?.firstOrNull()
            val area = addr?.subLocality ?: addr?.locality ?: addr?.subAdminArea ?: "Current Location"
            val street = addr?.thoroughfare ?: addr?.subThoroughfare ?: "Main Street"
            "$street, $area"
        } catch (e: Exception) {
            "Lat: ${String.format("%.5f", lat)}, Lng: ${String.format("%.5f", lng)}"
        }
    }

    fun dispatchEmergencySms(context: Context, onResult: (Int, String) -> Unit) {
        val contacts = _contactsList.value
        val loc = locationState.value
        val lat = loc?.latitude
        val lng = loc?.longitude

        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            onResult(0, "Permission DENIED: SEND_SMS permission is required to send real emergency SMS messages.")
            return
        }

        val streetAddr = if (lat != null && lng != null) getReverseGeocodedAddress(lat, lng) else "Location Unknown"

        val mapsUrl = if (lat != null && lng != null) {
            "https://maps.google.com/?q=$lat,$lng"
        } else {
            "https://maps.google.com"
        }

        val deepLinkUrl = if (lat != null && lng != null) {
            "rakshalink://track?lat=$lat&lng=$lng"
        } else {
            "rakshalink://track"
        }

        val webTrackUrl = if (lat != null && lng != null) {
            "https://rakshlink-app.onrender.com/track?lat=$lat&lng=$lng"
        } else {
            "https://rakshlink-app.onrender.com"
        }

        val smsMessage = """
🚨 RAKSHALINK EMERGENCY SOS ALERT!
Likhit Bhat requires immediate help!

📍 Live Location: $streetAddr
📍 Coordinates: ${if (lat != null && lng != null) "$lat, $lng" else "N/A"}
🗺️ Google Maps: $mapsUrl
📱 Track Live in App: $deepLinkUrl
🌐 Web Live Track: $webTrackUrl

- Sent automatically by RakshaLink Safety App
""".trimIndent()

        var sentCount = 0
        val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.getSystemService(android.telephony.SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            android.telephony.SmsManager.getDefault()
        }

        contacts.forEach { contact ->
            val cleanPhone = contact.phoneNumber.trim()
            if (cleanPhone.isNotBlank()) {
                try {
                    val parts = smsManager.divideMessage(smsMessage)
                    if (parts.size > 1) {
                        smsManager.sendMultipartTextMessage(cleanPhone, null, parts, null, null)
                    } else {
                        smsManager.sendTextMessage(cleanPhone, null, smsMessage, null, null)
                    }
                    sentCount++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (sentCount > 0) {
            onResult(sentCount, "🚨 EMERGENCY SOS BROADCAST: Live Location SMS sent to $sentCount emergency contact(s)!")
        } else {
            onResult(0, "No valid emergency contact numbers found to dispatch SMS.")
        }
    }

    fun cancelActiveSos(alertId: String, wasFalseAlarm: Boolean) {
        viewModelScope.launch {
            sosRepository.resolveSos(alertId, wasFalseAlarm)
        }
    }

    // Safe Zone Actions
    fun addSafeZone(zone: SafeZoneModel) {
        viewModelScope.launch {
            safeZoneRepository.addSafeZone(zone)
        }
    }

    fun deleteSafeZone(id: String) {
        viewModelScope.launch {
            safeZoneRepository.deleteSafeZone(id)
        }
    }

    // Emergency Contacts Actions
    fun addContact(contact: EmergencyContactModel) {
        val updated = if (contact.isPrimary) {
            _contactsList.value.map { it.copy(isPrimary = false) } + contact
        } else {
            _contactsList.value + contact
        }
        _contactsList.value = updated
        viewModelScope.launch {
            contactRepository.addContact(contact)
        }
    }

    fun verifyContact(contactId: String) {
        _contactsList.value = _contactsList.value.map {
            if (it.id == contactId) it.copy(isVerified = true) else it
        }
    }

    fun setPrimaryContact(contactId: String) {
        _contactsList.value = _contactsList.value.map {
            it.copy(isPrimary = (it.id == contactId))
        }
    }

    fun deleteContact(id: String) {
        _contactsList.value = _contactsList.value.filter { it.id != id }
        viewModelScope.launch {
            contactRepository.deleteContact(id)
        }
    }

    // Fall Detection Actions
    fun simulateFallImpact() {
        fallDetectionManager.triggerFallAlertSequence()
    }

    fun cancelFall() {
        fallDetectionManager.cancelFall()
    }

    override fun onCleared() {
        super.onCleared()
        fallDetectionManager.stopMonitoring()
        deadManJob?.cancel()
    }
}
