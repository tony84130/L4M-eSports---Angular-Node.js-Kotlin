package com.example.l4m_esports_mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.l4m_esports_mobile.data.model.request.CreateTeamRequest
import com.example.l4m_esports_mobile.data.model.request.UpdateTeamRequest
import com.example.l4m_esports_mobile.data.model.response.TeamResponse
import com.example.l4m_esports_mobile.data.remote.SocketService
import com.example.l4m_esports_mobile.data.repository.TeamRepository
import com.example.l4m_esports_mobile.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val socketService: SocketService
) : ViewModel() {

    private val _teams = MutableStateFlow<List<TeamResponse>>(emptyList())
    val teams: StateFlow<List<TeamResponse>> = _teams.asStateFlow()

    private val _selectedTeam = MutableStateFlow<TeamResponse?>(null)
    val selectedTeam: StateFlow<TeamResponse?> = _selectedTeam.asStateFlow()

    private val _uiState = MutableStateFlow<TeamUiState>(TeamUiState.Idle)
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    fun loadTeams(game: String? = null, status: String? = null) {
        viewModelScope.launch {
            _uiState.value = TeamUiState.Loading
            when (val result = teamRepository.getAllTeams(game, status)) {
                is Result.Success -> {
                    _teams.value = result.data
                    _uiState.value = TeamUiState.Idle
                    setupSocketListeners()
                }
                is Result.Error -> {
                    _uiState.value = TeamUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    private fun setupSocketListeners() {
        socketService.connect()

        // Listen for team updates
        socketService.on("team:updated") {
            loadTeams()
        }

        // Listen for team creations
        socketService.on("team:created") {
            loadTeams()
        }

        // Listen for team deletions
        socketService.on("team:deleted") { args ->
            val data = args[0] as? Map<*, *>
            val teamId = data?.get("teamId") as? String
            if (teamId != null) {
                _teams.value = _teams.value.filter { it.id != teamId }
            }
        }

        // Listen for captain transfers
        socketService.on("team:captainTransferred") {
            loadTeams()
        }
    }

    fun loadTeamById(id: String) {
        viewModelScope.launch {
            _uiState.value = TeamUiState.Loading
            when (val result = teamRepository.getTeamById(id)) {
                is Result.Success -> {
                    _selectedTeam.value = result.data
                    _uiState.value = TeamUiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = TeamUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun createTeam(name: String, game: String, description: String?, logo: String?, maxMembers: Int?) {
        viewModelScope.launch {
            _uiState.value = TeamUiState.Loading
            val request = CreateTeamRequest(
                name = name,
                game = game,
                description = description,
                logo = logo,
                maxMembers = maxMembers
            )
            when (val result = teamRepository.createTeam(request)) {
                is Result.Success -> {
                    // Recharger la liste des équipes pour avoir les données à jour
                    loadTeams()
                    _uiState.value = TeamUiState.Success("Équipe créée avec succès")
                }
                is Result.Error -> {
                    _uiState.value = TeamUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun updateTeam(id: String, name: String?, description: String?, logo: String?, status: String?, maxMembers: Int?) {
        viewModelScope.launch {
            _uiState.value = TeamUiState.Loading
            val request = UpdateTeamRequest(
                name = name,
                description = description,
                logo = logo,
                status = status,
                maxMembers = maxMembers
            )
            when (val result = teamRepository.updateTeam(id, request)) {
                is Result.Success -> {
                    _teams.value = _teams.value.map { if (it.id == id) result.data else it }
                    // Recharger l'équipe pour avoir les données à jour
                    loadTeamById(id)
                    _uiState.value = TeamUiState.Success("Équipe mise à jour avec succès")
                }
                is Result.Error -> {
                    _uiState.value = TeamUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun deleteTeam(id: String) {
        viewModelScope.launch {
            _uiState.value = TeamUiState.Loading
            when (val result = teamRepository.deleteTeam(id)) {
                is Result.Success -> {
                    _teams.value = _teams.value.filter { it.id != id }
                    _selectedTeam.value = null
                    // Recharger la liste des équipes pour mettre à jour
                    loadTeams()
                    _uiState.value = TeamUiState.Success("Équipe supprimée avec succès")
                }
                is Result.Error -> {
                    _uiState.value = TeamUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun inviteMember(teamId: String, gamertag: String) {
        viewModelScope.launch {
            _uiState.value = TeamUiState.Loading
            when (val result = teamRepository.inviteMember(teamId, gamertag)) {
                is Result.Success -> {
                    loadTeamById(teamId) // Reload team to get updated members
                    _uiState.value = TeamUiState.Success("Invitation envoyée")
                }
                is Result.Error -> {
                    _uiState.value = TeamUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun removeMember(teamId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = TeamUiState.Loading
            when (val result = teamRepository.removeMember(teamId, userId)) {
                is Result.Success -> {
                    loadTeamById(teamId) // Reload team to get updated members
                    _uiState.value = TeamUiState.Success("Membre retiré")
                }
                is Result.Error -> {
                    _uiState.value = TeamUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun transferCaptain(teamId: String, newCaptainId: String) {
        viewModelScope.launch {
            _uiState.value = TeamUiState.Loading
            when (val result = teamRepository.transferCaptain(teamId, newCaptainId)) {
                is Result.Success -> {
                    loadTeamById(teamId) // Reload team to get updated captain
                    loadTeams() // Reload teams list
                    _uiState.value = TeamUiState.Success("Capitaine transféré")
                }
                is Result.Error -> {
                    _uiState.value = TeamUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun leaveTeam(teamId: String) {
        viewModelScope.launch {
            _uiState.value = TeamUiState.Loading
            when (val result = teamRepository.leaveTeam(teamId)) {
                is Result.Success -> {
                    loadTeams() // Reload teams list
                    _uiState.value = TeamUiState.Success("Vous avez quitté l'équipe")
                }
                is Result.Error -> {
                    _uiState.value = TeamUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = TeamUiState.Idle
    }
}

sealed class TeamUiState {
    object Idle : TeamUiState()
    object Loading : TeamUiState()
    data class Success(val message: String) : TeamUiState()
    data class Error(val message: String) : TeamUiState()
}

