package com.rakshalink.ui.guardian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakshalink.data.remote.dto.GuardianInviteDto
import com.rakshalink.data.remote.dto.WearerGuardianLinkDto
import com.rakshalink.data.remote.supabase.SupabaseClientProvider
import com.rakshalink.domain.model.AlertModel
import com.rakshalink.domain.model.WearerModel
import com.rakshalink.domain.repository.GuardianRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

import kotlinx.coroutines.flow.first

@HiltViewModel
class GuardianViewModel @Inject constructor(
    private val guardianRepository: GuardianRepository,
    private val supabaseProvider: SupabaseClientProvider,
    private val userPreferencesManager: com.rakshalink.data.preferences.UserPreferencesManager
) : ViewModel() {

    val linkedWearersState: StateFlow<List<WearerModel>> = guardianRepository.getLinkedWearers()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val alertInboxState: StateFlow<List<AlertModel>> = guardianRepository.getAlertInbox()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _pendingInvitesState = MutableStateFlow<List<GuardianInviteDto>>(emptyList())
    val pendingInvitesState: StateFlow<List<GuardianInviteDto>> = _pendingInvitesState.asStateFlow()

    private val _guardianInfo = MutableStateFlow(Pair("Guardian User", "guardian@rakshalink.com"))
    val guardianInfo: StateFlow<Pair<String, String>> = _guardianInfo.asStateFlow()

    init {
        listenToPendingInvites()
        loadGuardianInfo()
    }

    private fun loadGuardianInfo() {
        viewModelScope.launch {
            val supabaseUser = try { supabaseProvider.auth.currentSessionOrNull()?.user } catch (e: Exception) { null }
            val supabaseEmail = supabaseUser?.email ?: ""
            val supabasePhone = supabaseUser?.phone ?: ""
            val storedEmailOrPhone = try { userPreferencesManager.userPhoneOrEmailFlow.first() } catch (e: Exception) { "" }

            val activeEmail = when {
                supabaseEmail.isNotEmpty() -> supabaseEmail
                storedEmailOrPhone.isNotEmpty() -> storedEmailOrPhone
                supabasePhone.isNotEmpty() -> supabasePhone
                else -> "guardian@rakshalink.com"
            }

            val rawName = if (activeEmail.contains("@")) {
                activeEmail.substringBefore("@")
                    .split(".", "_", "-")
                    .joinToString(" ") { word -> word.lowercase().replaceFirstChar { char -> char.uppercase() } }
            } else if (activeEmail.isNotEmpty()) {
                activeEmail
            } else {
                "Guardian User"
            }

            _guardianInfo.value = Pair(rawName, activeEmail)
        }
    }

    private fun listenToPendingInvites() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""

            suspend fun fetchPending(): List<GuardianInviteDto> {
                return try {
                    supabaseProvider.db.from("guardian_invites")
                        .select(columns = Columns.ALL) {
                            filter {
                                eq("status", "pending")
                            }
                        }.decodeList<GuardianInviteDto>()
                } catch (e: Exception) {
                    emptyList()
                }
            }

            _pendingInvitesState.value = fetchPending()

            try {
                val channel = supabaseProvider.realtime.channel("guardian_invites_realtime")
                val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "guardian_invites"
                }
                changes.collect {
                    _pendingInvitesState.value = fetchPending()
                }
                channel.subscribe()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun acceptInvite(inviteId: String, wearerId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""
            try {
                val activeUserId = if (currentUserId.isNotEmpty()) currentUserId else "g_" + UUID.randomUUID().toString().take(8)

                // 1. Insert into wearer_guardian_links with status=active
                val linkDto = WearerGuardianLinkDto(
                    id = UUID.randomUUID().toString(),
                    wearerId = wearerId,
                    guardianId = activeUserId,
                    role = "secondary",
                    status = "active",
                    linkedAt = java.time.Instant.now().toString()
                )
                try {
                    supabaseProvider.db.from("wearer_guardian_links").insert(linkDto)
                } catch (e: Exception) {
                    try {
                        supabaseProvider.db.from("guardian_links").insert(linkDto)
                    } catch (e2: Exception) {}
                }

                // 2. Update guardian_invites setting status=accepted
                try {
                    supabaseProvider.db.from("guardian_invites").update(mapOf("status" to "accepted")) {
                        filter { eq("id", inviteId) }
                    }
                } catch (e: Exception) {}

                withContext(Dispatchers.Main) {
                    onResult(true, "Guardian invitation accepted! Active live map tracking enabled.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Failed to accept invitation: ${e.localizedMessage}")
                }
            }
        }
    }

    fun rejectInvite(inviteId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                supabaseProvider.db.from("guardian_invites").update(mapOf("status" to "rejected")) {
                    filter { eq("id", inviteId) }
                }
                withContext(Dispatchers.Main) {
                    onResult(true, "Invitation rejected.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Failed to reject invitation.")
                }
            }
        }
    }

    fun addWearerByCode(codeOrEmail: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""
            val activeUserId = if (currentUserId.isNotEmpty()) currentUserId else try { userPreferencesManager.userIdFlow.first() } catch (e: Exception) { "" }

            if (activeUserId.isEmpty()) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Please log in to link a wearer.")
                }
                return@launch
            }

            val cleaned = codeOrEmail.trim()
            if (cleaned.isEmpty()) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Please enter a valid wearer code or email.")
                }
                return@launch
            }

            try {
                // Find wearer profile matching code or email or ID
                val profiles = try {
                    supabaseProvider.db.from("users")
                        .select(columns = Columns.ALL)
                        .decodeList<com.rakshalink.data.remote.dto.UserProfileDto>()
                } catch (e: Exception) { emptyList() }

                val match = profiles.firstOrNull { prof ->
                    prof.email.equals(cleaned, ignoreCase = true) ||
                    prof.wearer_code.equals(cleaned, ignoreCase = true) ||
                    prof.id.equals(cleaned, ignoreCase = true)
                }

                val targetWearerId = match?.id ?: cleaned

                val linkDto = WearerGuardianLinkDto(
                    id = UUID.randomUUID().toString(),
                    wearerId = targetWearerId,
                    guardianId = activeUserId,
                    role = "primary",
                    status = "active",
                    linkedAt = java.time.Instant.now().toString()
                )

                try {
                    supabaseProvider.db.from("wearer_guardian_links").insert(linkDto)
                } catch (e: Exception) {
                    try {
                        supabaseProvider.db.from("guardian_links").insert(linkDto)
                    } catch (e2: Exception) {}
                }

                withContext(Dispatchers.Main) {
                    onResult(true, "Wearer successfully linked! Live location tracking enabled.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Failed to link wearer: ${e.localizedMessage}")
                }
            }
        }
    }

    fun removeWearer(wearerId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""
            try {
                try {
                    supabaseProvider.db.from("wearer_guardian_links")
                        .delete {
                            filter {
                                eq("wearer_id", wearerId)
                                if (currentUserId.isNotEmpty()) eq("guardian_id", currentUserId)
                            }
                        }
                } catch (e: Exception) {
                    try {
                        supabaseProvider.db.from("guardian_links")
                            .delete {
                                filter {
                                    eq("wearer_id", wearerId)
                                    if (currentUserId.isNotEmpty()) eq("guardian_id", currentUserId)
                                }
                            }
                    } catch (e2: Exception) {}
                }
                withContext(Dispatchers.Main) {
                    onResult(true, "Wearer successfully unlinked.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Failed to unlink wearer: ${e.localizedMessage}")
                }
            }
        }
    }

    fun markAlertAsRead(alertId: String) {
        viewModelScope.launch {
            guardianRepository.markAlertAsRead(alertId)
        }
    }
}
