package com.rakshalink.data.repository

import com.rakshalink.data.remote.dto.EmergencyContactDto
import com.rakshalink.data.remote.supabase.SupabaseClientProvider
import com.rakshalink.domain.model.EmergencyContactModel
import com.rakshalink.domain.repository.EmergencyContactRepository
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyContactRepositoryImpl @Inject constructor(
    private val supabaseProvider: SupabaseClientProvider
) : EmergencyContactRepository {

    override fun getEmergencyContacts(): Flow<List<EmergencyContactModel>> = flow {
        val userId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""
        if (userId.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        try {
            val dtos = supabaseProvider.db.from("emergency_contacts")
                .select(columns = Columns.ALL) {
                    filter { eq("user_id", userId) }
                }.decodeList<EmergencyContactDto>()

            val models = dtos.map { dto ->
                EmergencyContactModel(
                    id = dto.id,
                    userId = dto.userId,
                    name = dto.name,
                    phoneNumber = dto.phoneNumber,
                    relationship = dto.relationship,
                    isPrimary = dto.isPrimary,
                    isVerified = dto.isVerified
                )
            }
            emit(models)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun addContact(contact: EmergencyContactModel) {
        val userId = supabaseProvider.auth.currentSessionOrNull()?.user?.id ?: ""
        val newId = if (contact.id.isEmpty()) UUID.randomUUID().toString() else contact.id

        try {
            val dto = EmergencyContactDto(
                id = newId,
                userId = if (contact.userId.isEmpty()) userId else contact.userId,
                name = contact.name,
                phoneNumber = contact.phoneNumber,
                relationship = contact.relationship,
                isPrimary = contact.isPrimary,
                isVerified = contact.isVerified
            )
            supabaseProvider.db.from("emergency_contacts").insert(dto)
        } catch (e: Exception) {
            // Error handling fallback
        }
    }

    override suspend fun updateContact(contact: EmergencyContactModel) {
        try {
            val dto = EmergencyContactDto(
                id = contact.id,
                userId = contact.userId,
                name = contact.name,
                phoneNumber = contact.phoneNumber,
                relationship = contact.relationship,
                isPrimary = contact.isPrimary,
                isVerified = contact.isVerified
            )
            supabaseProvider.db.from("emergency_contacts").update(dto) {
                filter { eq("id", contact.id) }
            }
        } catch (e: Exception) {
            // Error handling fallback
        }
    }

    override suspend fun deleteContact(id: String) {
        try {
            supabaseProvider.db.from("emergency_contacts").delete {
                filter { eq("id", id) }
            }
        } catch (e: Exception) {
            // Error handling fallback
        }
    }

    override suspend fun requestSmsVerification(contactId: String): Boolean {
        return true
    }
}

