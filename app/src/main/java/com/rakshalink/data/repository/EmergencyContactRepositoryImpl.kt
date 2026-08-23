package com.rakshalink.data.repository

import android.util.Log
import com.rakshalink.data.local.dao.EmergencyContactDao
import com.rakshalink.data.local.entities.EmergencyContactEntity
import com.rakshalink.data.remote.dto.EmergencyContactDto
import com.rakshalink.data.remote.supabase.SupabaseClientProvider
import com.rakshalink.domain.model.EmergencyContactModel
import com.rakshalink.domain.repository.EmergencyContactRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyContactRepositoryImpl @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider,
    private val contactDao: EmergencyContactDao
) : EmergencyContactRepository {

    companion object {
        private const val TAG = "RakshaOTP"
    }

    override fun getEmergencyContacts(): Flow<List<EmergencyContactModel>> = channelFlow {
        val userId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""
        Log.d(TAG, "[UI State Update] Starting getEmergencyContacts stream for userId: '$userId'")

        if (userId.isEmpty()) {
            send(emptyList())
            return@channelFlow
        }

        // 1. Fetch initial dataset from Supabase & insert into Room
        launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "[Supabase Fetch] Fetching emergency_contacts for userId='$userId'")
                val dtos = supabaseProvider.db.from("emergency_contacts")
                    .select(columns = Columns.ALL) {
                        filter { eq("user_id", userId) }
                    }.decodeList<EmergencyContactDto>()

                Log.d(TAG, "[Supabase Fetch Success] Found ${dtos.size} remote contacts")
                val roomEntities = dtos.map { dto ->
                    EmergencyContactEntity(
                        id = dto.id,
                        wearerId = dto.userId,
                        name = dto.name,
                        phone = dto.phoneNumber,
                        relationship = dto.relationship,
                        isPrimary = dto.isPrimary,
                        isVerified = dto.isVerified
                    )
                }
                contactDao.insertContacts(roomEntities)
                Log.d(TAG, "[Room Sync] Synced ${roomEntities.size} contacts to local Room cache")
            } catch (e: Exception) {
                Log.e(TAG, "[Supabase Fetch Error] ${e.message}", e)
            }
        }

        // 2. Subscribe to Supabase Realtime channel for live table updates
        launch(Dispatchers.IO) {
            try {
                val channel = supabaseProvider.realtime.channel("emergency_contacts_realtime")
                val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "emergency_contacts"
                }
                channel.subscribe()
                Log.d(TAG, "[Supabase Realtime] Subscribed to emergency_contacts_realtime channel")

                changes.collect { action ->
                    Log.d(TAG, "[Supabase Realtime Event] Received table action: $action")
                    // Re-sync remote contacts on realtime table mutation
                    try {
                        val dtos = supabaseProvider.db.from("emergency_contacts")
                            .select(columns = Columns.ALL) {
                                filter { eq("user_id", userId) }
                            }.decodeList<EmergencyContactDto>()

                        val roomEntities = dtos.map { dto ->
                            EmergencyContactEntity(
                                id = dto.id,
                                wearerId = dto.userId,
                                name = dto.name,
                                phone = dto.phoneNumber,
                                relationship = dto.relationship,
                                isPrimary = dto.isPrimary,
                                isVerified = dto.isVerified
                            )
                        }
                        contactDao.insertContacts(roomEntities)
                        Log.d(TAG, "[Room Sync Realtime] Updated Room with ${roomEntities.size} contacts")
                    } catch (e: Exception) {
                        Log.e(TAG, "[Supabase Realtime Sync Error] ${e.message}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "[Supabase Realtime Setup Error] ${e.message}", e)
            }
        }

        // 3. Observe local Room DAO Flow & emit UI state models instantly
        contactDao.getContactsForWearer(userId).collectLatest { entities ->
            val models = entities.map { entity ->
                EmergencyContactModel(
                    id = entity.id,
                    userId = entity.wearerId,
                    name = entity.name,
                    phoneNumber = entity.phone,
                    relationship = entity.relationship,
                    isPrimary = entity.isPrimary,
                    isVerified = entity.isVerified
                )
            }
            Log.d(TAG, "[UI State Update Trigger] Emitting ${models.size} contacts to UI (Verified: ${models.count { it.isVerified }})")
            send(models)
        }
    }

    override suspend fun addContact(contact: EmergencyContactModel) {
        val userId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""
        val newId = if (contact.id.isEmpty()) UUID.randomUUID().toString() else contact.id
        val resolvedUserId = if (contact.userId.isEmpty()) userId else contact.userId

        Log.d(TAG, "[Room Insert] Inserting contact to Room: id='$newId', name='${contact.name}', phone='${contact.phoneNumber}', isVerified=${contact.isVerified}")
        val roomEntity = EmergencyContactEntity(
            id = newId,
            wearerId = resolvedUserId,
            name = contact.name,
            phone = contact.phoneNumber,
            relationship = contact.relationship,
            isPrimary = contact.isPrimary,
            isVerified = contact.isVerified
        )
        contactDao.insertContact(roomEntity)
        Log.d(TAG, "[Room Insert Success] Contact '$newId' saved to Room database")

        if (resolvedUserId.isNotEmpty()) {
            val dto = EmergencyContactDto(
                id = newId,
                userId = resolvedUserId,
                name = contact.name,
                phoneNumber = contact.phoneNumber,
                relationship = contact.relationship,
                isPrimary = contact.isPrimary,
                isVerified = contact.isVerified
            )
            Log.d(TAG, "[Supabase Insert Call] Sending DTO to Supabase emergency_contacts: $dto")
            try {
                supabaseProvider.db.from("emergency_contacts").insert(dto)
                Log.d(TAG, "[Supabase Insert Success] Contact successfully created in Supabase table emergency_contacts")
            } catch (e: Exception) {
                Log.e(TAG, "[Supabase Insert ERROR] Failed to insert contact into Supabase: ${e.message}", e)
            }
        } else {
            Log.w(TAG, "[Supabase Insert Skipped] User ID is empty. Session may be unauthenticated.")
        }
    }

    override suspend fun updateContact(contact: EmergencyContactModel) {
        Log.d(TAG, "[Room Update] Updating contact in Room: id='${contact.id}', isVerified=${contact.isVerified}")
        val roomEntity = EmergencyContactEntity(
            id = contact.id,
            wearerId = contact.userId,
            name = contact.name,
            phone = contact.phoneNumber,
            relationship = contact.relationship,
            isPrimary = contact.isPrimary,
            isVerified = contact.isVerified
        )
        contactDao.insertContact(roomEntity)
        Log.d(TAG, "[Room Update Success] Contact '${contact.id}' updated in Room")

        val dto = EmergencyContactDto(
            id = contact.id,
            userId = contact.userId,
            name = contact.name,
            phoneNumber = contact.phoneNumber,
            relationship = contact.relationship,
            isPrimary = contact.isPrimary,
            isVerified = contact.isVerified
        )
        Log.d(TAG, "[Supabase Update Call] Updating Supabase row for id='${contact.id}'")
        try {
            supabaseProvider.db.from("emergency_contacts").update(dto) {
                filter { eq("id", contact.id) }
            }
            Log.d(TAG, "[Supabase Update Success] Contact '${contact.id}' updated in Supabase")
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase Update ERROR] Failed to update contact: ${e.message}", e)
        }
    }

    override suspend fun deleteContact(id: String) {
        Log.d(TAG, "[Room Delete] Deleting contact id='$id' from Room")
        contactDao.deleteContact(id)

        Log.d(TAG, "[Supabase Delete Call] Deleting contact id='$id' from Supabase")
        try {
            supabaseProvider.db.from("emergency_contacts").delete {
                filter { eq("id", id) }
            }
            Log.d(TAG, "[Supabase Delete Success] Deleted contact id='$id'")
        } catch (e: Exception) {
            Log.e(TAG, "[Supabase Delete ERROR] Failed to delete contact: ${e.message}", e)
        }
    }

    override suspend fun requestSmsVerification(contactId: String): Boolean {
        Log.d(TAG, "[Request SMS Verification] Triggered for contactId='$contactId'")
        return true
    }
}
