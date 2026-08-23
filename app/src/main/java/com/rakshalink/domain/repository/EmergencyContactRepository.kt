package com.rakshalink.domain.repository

import com.rakshalink.domain.model.EmergencyContactModel
import kotlinx.coroutines.flow.Flow

interface EmergencyContactRepository {
    fun getEmergencyContacts(): Flow<List<EmergencyContactModel>>
    suspend fun addContact(contact: EmergencyContactModel)
    suspend fun updateContact(contact: EmergencyContactModel)
    suspend fun deleteContact(id: String)
    suspend fun requestSmsVerification(contactId: String): Boolean
}
