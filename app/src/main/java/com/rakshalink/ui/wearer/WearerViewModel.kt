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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

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
    private val fallDetectionManager: com.rakshalink.services.FallDetectionManager
) : ViewModel() {

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

    private val _guardiansList = MutableStateFlow<List<GuardianModel>>(
        listOf(
            GuardianModel("g1", "Ramesh Bhat (Dad)", "ramesh@rakshalink.com", "+91 98450 12345", isPrimary = true),
            GuardianModel("g2", "Priya Bhat (Sister)", "priya@rakshalink.com", "+91 98450 67890", isPrimary = false)
        )
    )
    val guardiansList: StateFlow<List<GuardianModel>> = _guardiansList.asStateFlow()

    fun inviteGuardian(emailOrPhone: String, onSuccess: (String) -> Unit) {
        if (emailOrPhone.isBlank()) return
        val newGuardian = GuardianModel(
            id = "g_${System.currentTimeMillis()}",
            name = if (emailOrPhone.contains("@")) emailOrPhone.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } else emailOrPhone,
            email = if (emailOrPhone.contains("@")) emailOrPhone else "$emailOrPhone@rakshalink.com",
            phone = if (emailOrPhone.contains("@")) "+91 98000 11223" else emailOrPhone,
            isPrimary = false
        )
        _guardiansList.value = _guardiansList.value + newGuardian
        onSuccess("Guardian invitation sent to $emailOrPhone!")
    }

    fun removeGuardian(id: String) {
        _guardiansList.value = _guardiansList.value.filter { it.id != id }
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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val safeZonesState: StateFlow<List<SafeZoneModel>> = safeZoneRepository.getActiveSafeZones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val defaultContacts = listOf(
        EmergencyContactModel(
            id = "c1",
            name = "Ramesh Bhat",
            phoneNumber = "+91 98450 12345",
            relationship = "Father",
            isPrimary = true,
            isVerified = true
        ),
        EmergencyContactModel(
            id = "c2",
            name = "Priya Bhat",
            phoneNumber = "+91 98450 67890",
            relationship = "Sister",
            isPrimary = false,
            isVerified = true
        ),
        EmergencyContactModel(
            id = "c3",
            name = "Dr. Ananya Sharma",
            phoneNumber = "+91 98111 22334",
            relationship = "Family Doctor",
            isPrimary = false,
            isVerified = false
        )
    )

    private val _contactsList = MutableStateFlow<List<EmergencyContactModel>>(defaultContacts)
    val contactsState: StateFlow<List<EmergencyContactModel>> = _contactsList.asStateFlow()

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

    private val _isForceOffline = MutableStateFlow(false)
    val isForceOffline: StateFlow<Boolean> = _isForceOffline.asStateFlow()

    fun toggleForceOffline() {
        _isForceOffline.value = !_isForceOffline.value
    }

    init {
        fallDetectionManager.startMonitoring()
    }

    val dashboardUiState: StateFlow<WearerDashboardUiState> = combine(
        locationState,
        pendantBattery,
        pendantConnectionState,
        safeZonesState,
        _isVoiceSosActive,
        _isDeadManActive,
        _deadManSeconds,
        _isForceOffline
    ) { args: Array<Any?> ->
        val loc = args[0] as? LocationModel
        val pBattery = (args[1] as? Int) ?: 78
        val pConnection = (args[2] as? PendantConnectionState) ?: PendantConnectionState.CONNECTED
        @Suppress("UNCHECKED_CAST")
        val safeZones = (args[3] as? List<SafeZoneModel>) ?: emptyList()
        val voiceSos = (args[4] as? Boolean) ?: false
        val deadMan = (args[5] as? Boolean) ?: false
        val dSecs = (args[6] as? Int) ?: 1800
        val forceOffline = (args[7] as? Boolean) ?: false

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

        WearerDashboardUiState(
            userName = "Likhit Bhat",
            greeting = greetingText,
            isProtected = score >= 50,
            safetyScore = score,
            isGpsActive = loc != null,
            isNetworkActive = !forceOffline,
            isPendantConnected = pConnection == PendantConnectionState.CONNECTED,
            batteryLevel = pBattery,
            phoneBatteryLevel = phoneBat,
            isPhoneCharging = isCharging,
            guardianCount = 2,
            alertsThisMonth = 1,
            isInsideSafeZone = isInsideZone,
            safeZoneStatusText = zoneStatus,
            isVoiceSosActive = voiceSos,
            isDeadManActive = deadMan,
            deadManRemainingSeconds = dSecs,
            lastLocation = loc,
            recentActivityTitle = "Sos Alert",
            recentActivityTime = "8/11/2026, 1:44:59 PM",
            recentActivityStatus = "CANCELLED",
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WearerDashboardUiState(isLoading = true)
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
