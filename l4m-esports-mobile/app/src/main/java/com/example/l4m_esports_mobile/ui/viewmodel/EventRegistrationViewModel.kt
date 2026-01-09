package com.example.l4m_esports_mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.l4m_esports_mobile.data.model.request.CreateEventRegistrationRequest
import com.example.l4m_esports_mobile.data.model.request.UpdateEventRegistrationRequest
import com.example.l4m_esports_mobile.data.model.response.EventRegistrationResponse
import com.example.l4m_esports_mobile.data.repository.EventRegistrationRepository
import com.example.l4m_esports_mobile.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventRegistrationViewModel @Inject constructor(
    private val eventRegistrationRepository: EventRegistrationRepository
) : ViewModel() {

    private val _registrations = MutableStateFlow<List<EventRegistrationResponse>>(emptyList())
    val registrations: StateFlow<List<EventRegistrationResponse>> = _registrations.asStateFlow()

    private val _uiState = MutableStateFlow<EventRegistrationUiState>(EventRegistrationUiState.Idle)
    val uiState: StateFlow<EventRegistrationUiState> = _uiState.asStateFlow()

    fun loadRegistrations(event: String? = null, team: String? = null) {
        viewModelScope.launch {
            _uiState.value = EventRegistrationUiState.Loading
            when (val result = eventRegistrationRepository.getRegistrations(event, team)) {
                is Result.Success -> {
                    _registrations.value = result.data
                    _uiState.value = EventRegistrationUiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = EventRegistrationUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun loadRegistrationsByEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.value = EventRegistrationUiState.Loading
            when (val result = eventRegistrationRepository.getRegistrationsByEvent(eventId)) {
                is Result.Success -> {
                    _registrations.value = result.data
                    _uiState.value = EventRegistrationUiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = EventRegistrationUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun loadRegistrationsByTeam(teamId: String) {
        viewModelScope.launch {
            _uiState.value = EventRegistrationUiState.Loading
            when (val result = eventRegistrationRepository.getRegistrationsByTeam(teamId)) {
                is Result.Success -> {
                    _registrations.value = result.data
                    _uiState.value = EventRegistrationUiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = EventRegistrationUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    private val _selectedRegistration = MutableStateFlow<EventRegistrationResponse?>(null)
    val selectedRegistration: StateFlow<EventRegistrationResponse?> = _selectedRegistration.asStateFlow()

    fun loadRegistrationById(id: String) {
        viewModelScope.launch {
            _uiState.value = EventRegistrationUiState.Loading
            when (val result = eventRegistrationRepository.getRegistrationById(id)) {
                is Result.Success -> {
                    _selectedRegistration.value = result.data
                    _uiState.value = EventRegistrationUiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = EventRegistrationUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun createRegistration(teamId: String, eventId: String, participatingMemberIds: List<String>? = null) {
        viewModelScope.launch {
            _uiState.value = EventRegistrationUiState.Loading
            val request = CreateEventRegistrationRequest(
                teamId = teamId, 
                eventId = eventId,
                participatingMemberIds = participatingMemberIds
            )
            when (val result = eventRegistrationRepository.createRegistration(request)) {
                is Result.Success -> {
                    _registrations.value = _registrations.value + result.data
                    _uiState.value = EventRegistrationUiState.Success("Inscription créée avec succès")
                }
                is Result.Error -> {
                    _uiState.value = EventRegistrationUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun updateRegistration(id: String, status: String) {
        viewModelScope.launch {
            _uiState.value = EventRegistrationUiState.Loading
            val request = UpdateEventRegistrationRequest(status = status)
            when (val result = eventRegistrationRepository.updateRegistration(id, request)) {
                is Result.Success -> {
                    _registrations.value = _registrations.value.map { 
                        if (it.id == id) result.data else it 
                    }
                    // Mettre à jour selectedRegistration si c'est celui qui a été modifié
                    if (_selectedRegistration.value?.id == id) {
                        _selectedRegistration.value = result.data
                    }
                    _uiState.value = EventRegistrationUiState.Success("Inscription mise à jour avec succès")
                }
                is Result.Error -> {
                    _uiState.value = EventRegistrationUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun deleteRegistration(id: String) {
        viewModelScope.launch {
            _uiState.value = EventRegistrationUiState.Loading
            when (val result = eventRegistrationRepository.deleteRegistration(id)) {
                is Result.Success -> {
                    _registrations.value = _registrations.value.filter { it.id != id }
                    _uiState.value = EventRegistrationUiState.Success("Inscription annulée avec succès")
                }
                is Result.Error -> {
                    _uiState.value = EventRegistrationUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = EventRegistrationUiState.Idle
    }
}

sealed class EventRegistrationUiState {
    object Idle : EventRegistrationUiState()
    object Loading : EventRegistrationUiState()
    data class Success(val message: String) : EventRegistrationUiState()
    data class Error(val message: String) : EventRegistrationUiState()
}

