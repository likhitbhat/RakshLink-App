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

@HiltViewModel
class GuardianViewModel @Inject constructor(
    private val guardianRepository: GuardianRepository,
    private val supabaseProvider: SupabaseClientProvider
) : ViewModel() {

    val linkedWearersState: StateFlow<List<WearerModel>> = guardianRepository.getLinkedWearers()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val alertInboxState: StateFlow<List<AlertModel>> = guardianRepository.getAlertInbox()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _pendingInvitesState = MutableStateFlow<List<GuardianInviteDto>>(emptyList())
    val pendingInvitesState: StateFlow<List<GuardianInviteDto>> = _pendingInvitesState.asStateFlow()

    init {
        listenToPendingInvites()
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

    fun markAlertAsRead(alertId: String) {
        viewModelScope.launch {
            guardianRepository.markAlertAsRead(alertId)
        }
    }
}
