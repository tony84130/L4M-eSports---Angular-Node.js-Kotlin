package com.example.l4m_esports_mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.l4m_esports_mobile.data.model.response.MatchResponse
import com.example.l4m_esports_mobile.data.repository.MatchRepository
import com.example.l4m_esports_mobile.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _matches = MutableStateFlow<List<MatchResponse>>(emptyList())
    val matches: StateFlow<List<MatchResponse>> = _matches.asStateFlow()

    private val _selectedMatch = MutableStateFlow<MatchResponse?>(null)
    val selectedMatch: StateFlow<MatchResponse?> = _selectedMatch.asStateFlow()

    private val _uiState = MutableStateFlow<MatchUiState>(MatchUiState.Idle)
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    fun loadMatches(event: String? = null, status: String? = null, team: String? = null) {
        viewModelScope.launch {
            _uiState.value = MatchUiState.Loading
            when (val result = matchRepository.getAllMatches(event, status, team)) {
                is Result.Success -> {
                    _matches.value = result.data
                    _uiState.value = MatchUiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = MatchUiState.Error(result.message)
                }
                else -> {
                    _uiState.value = MatchUiState.Idle
                }
            }
        }
    }

    fun loadMatchById(id: String) {
        viewModelScope.launch {
            _uiState.value = MatchUiState.Loading
            when (val result = matchRepository.getMatchById(id)) {
                is Result.Success -> {
                    _selectedMatch.value = result.data
                    _uiState.value = MatchUiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = MatchUiState.Error(result.message)
                }
                else -> {
                    _uiState.value = MatchUiState.Idle
                }
            }
        }
    }

    fun loadMatchesByEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.value = MatchUiState.Loading
            when (val result = matchRepository.getMatchesByEvent(eventId)) {
                is Result.Success -> {
                    _matches.value = result.data
                    _uiState.value = MatchUiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = MatchUiState.Error(result.message)
                }
                else -> {
                    _uiState.value = MatchUiState.Idle
                }
            }
        }
    }

    fun loadMatchesByTeam(teamId: String) {
        viewModelScope.launch {
            _uiState.value = MatchUiState.Loading
            when (val result = matchRepository.getMatchesByTeam(teamId)) {
                is Result.Success -> {
                    _matches.value = result.data
                    _uiState.value = MatchUiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = MatchUiState.Error(result.message)
                }
                else -> {
                    _uiState.value = MatchUiState.Idle
                }
            }
        }
    }

    fun updateMatchStatus(id: String, status: String) {
        viewModelScope.launch {
            _uiState.value = MatchUiState.Loading
            when (val result = matchRepository.updateMatchStatus(id, status)) {
                is Result.Success -> {
                    _selectedMatch.value = result.data
                    // Mettre à jour aussi dans la liste
                    _matches.value = _matches.value.map { if (it.id == id) result.data else it }
                    _uiState.value = MatchUiState.Success("Statut du match mis à jour avec succès")
                }
                is Result.Error -> {
                    _uiState.value = MatchUiState.Error(result.message)
                }
                else -> {
                    _uiState.value = MatchUiState.Idle
                }
            }
        }
    }

    fun updateMatchScore(id: String, team1Score: Int, team2Score: Int) {
        viewModelScope.launch {
            _uiState.value = MatchUiState.Loading
            when (val result = matchRepository.updateMatchScore(id, team1Score, team2Score)) {
                is Result.Success -> {
                    _selectedMatch.value = result.data
                    // Mettre à jour aussi dans la liste
                    _matches.value = _matches.value.map { if (it.id == id) result.data else it }
                    _uiState.value = MatchUiState.Success("Score du match mis à jour avec succès")
                }
                is Result.Error -> {
                    _uiState.value = MatchUiState.Error(result.message)
                }
                else -> {
                    _uiState.value = MatchUiState.Idle
                }
            }
        }
    }

    fun validateMatchResult(id: String) {
        viewModelScope.launch {
            _uiState.value = MatchUiState.Loading
            when (val result = matchRepository.validateMatchResult(id)) {
                is Result.Success -> {
                    _selectedMatch.value = result.data
                    // Mettre à jour aussi dans la liste
                    _matches.value = _matches.value.map { if (it.id == id) result.data else it }
                    _uiState.value = MatchUiState.Success("Résultat du match validé avec succès")
                }
                is Result.Error -> {
                    _uiState.value = MatchUiState.Error(result.message)
                }
                else -> {
                    _uiState.value = MatchUiState.Idle
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = MatchUiState.Idle
    }
}

sealed class MatchUiState {
    object Idle : MatchUiState()
    object Loading : MatchUiState()
    data class Success(val message: String) : MatchUiState()
    data class Error(val message: String) : MatchUiState()
}

