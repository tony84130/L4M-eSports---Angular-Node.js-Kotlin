package com.example.l4m_esports_mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.l4m_esports_mobile.data.model.request.CreateTeamRequestRequest
import com.example.l4m_esports_mobile.data.model.response.TeamRequestResponse
import com.example.l4m_esports_mobile.data.repository.TeamRequestRepository
import com.example.l4m_esports_mobile.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamRequestViewModel @Inject constructor(
    private val teamRequestRepository: TeamRequestRepository
) : ViewModel() {

    private val _teamRequests = MutableStateFlow<List<TeamRequestResponse>>(emptyList())
    val teamRequests: StateFlow<List<TeamRequestResponse>> = _teamRequests.asStateFlow()

    private val _uiState = MutableStateFlow<TeamRequestUiState>(TeamRequestUiState.Idle)
    val uiState: StateFlow<TeamRequestUiState> = _uiState.asStateFlow()

    fun loadTeamRequests(team: String? = null, user: String? = null, status: String? = null) {
        viewModelScope.launch {
            _uiState.value = TeamRequestUiState.Loading
            when (val result = teamRequestRepository.getAllTeamRequests(team, user, status)) {
                is Result.Success -> {
                    _teamRequests.value = result.data
                    _uiState.value = TeamRequestUiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = TeamRequestUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun createTeamRequest(teamId: String, message: String?) {
        viewModelScope.launch {
            _uiState.value = TeamRequestUiState.Loading
            val request = CreateTeamRequestRequest(
                team = teamId,
                message = message
            )
            when (val result = teamRequestRepository.createTeamRequest(request)) {
                is Result.Success -> {
                    // Recharger les demandes pour avoir les données à jour
                    loadTeamRequests(team = teamId)
                    _uiState.value = TeamRequestUiState.Success("Demande créée avec succès")
                }
                is Result.Error -> {
                    _uiState.value = TeamRequestUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun acceptTeamRequest(id: String, teamId: String? = null) {
        viewModelScope.launch {
            _uiState.value = TeamRequestUiState.Loading
            when (val result = teamRequestRepository.acceptTeamRequest(id)) {
                is Result.Success -> {
                    _teamRequests.value = _teamRequests.value.map { if (it.id == id) result.data else it }
                    // Recharger les demandes si on a un teamId (pour mettre à jour la liste)
                    teamId?.let { loadTeamRequestsByTeam(it) }
                    _uiState.value = TeamRequestUiState.Success("Demande acceptée")
                }
                is Result.Error -> {
                    _uiState.value = TeamRequestUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun rejectTeamRequest(id: String, teamId: String? = null) {
        viewModelScope.launch {
            _uiState.value = TeamRequestUiState.Loading
            when (val result = teamRequestRepository.rejectTeamRequest(id)) {
                is Result.Success -> {
                    _teamRequests.value = _teamRequests.value.map { if (it.id == id) result.data else it }
                    // Recharger les demandes si on a un teamId (pour mettre à jour la liste)
                    teamId?.let { loadTeamRequestsByTeam(it) }
                    _uiState.value = TeamRequestUiState.Success("Demande rejetée")
                }
                is Result.Error -> {
                    _uiState.value = TeamRequestUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun deleteTeamRequest(id: String) {
        viewModelScope.launch {
            _uiState.value = TeamRequestUiState.Loading
            when (val result = teamRequestRepository.deleteTeamRequest(id)) {
                is Result.Success -> {
                    _teamRequests.value = _teamRequests.value.filter { it.id != id }
                    _uiState.value = TeamRequestUiState.Success("Demande supprimée")
                }
                is Result.Error -> {
                    _uiState.value = TeamRequestUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun loadTeamRequestsByTeam(teamId: String) {
        viewModelScope.launch {
            _uiState.value = TeamRequestUiState.Loading
            when (val result = teamRequestRepository.getTeamRequestsByTeam(teamId)) {
                is Result.Success -> {
                    _teamRequests.value = result.data
                    _uiState.value = TeamRequestUiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = TeamRequestUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = TeamRequestUiState.Idle
    }
}

sealed class TeamRequestUiState {
    object Idle : TeamRequestUiState()
    object Loading : TeamRequestUiState()
    data class Success(val message: String) : TeamRequestUiState()
    data class Error(val message: String) : TeamRequestUiState()
}

