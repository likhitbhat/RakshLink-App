package com.rakshalink.domain.repository

import com.rakshalink.domain.model.AlertModel
import com.rakshalink.domain.model.WearerModel
import kotlinx.coroutines.flow.Flow

interface GuardianRepository {
    fun getLinkedWearers(): Flow<List<WearerModel>>
    fun getAlertInbox(): Flow<List<AlertModel>>
    suspend fun markAlertAsRead(alertId: String)
}
