package com.rakshalink.ui.guardian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakshalink.domain.model.AlertModel
import com.rakshalink.domain.model.WearerModel
import com.rakshalink.domain.repository.GuardianRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GuardianViewModel @Inject constructor(
    private val guardianRepository: GuardianRepository
) : ViewModel() {

    val linkedWearersState: StateFlow<List<WearerModel>> = guardianRepository.getLinkedWearers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alertInboxState: StateFlow<List<AlertModel>> = guardianRepository.getAlertInbox()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markAlertAsRead(alertId: String) {
        viewModelScope.launch {
            guardianRepository.markAlertAsRead(alertId)
        }
    }
}
