package com.example.l4m_esports_mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.l4m_esports_mobile.data.model.request.CreateGameRequest
import com.example.l4m_esports_mobile.data.model.request.UpdateGameRequest
import com.example.l4m_esports_mobile.data.model.response.GameResponse
import com.example.l4m_esports_mobile.data.remote.SocketService
import com.example.l4m_esports_mobile.data.repository.GameRepository
import com.example.l4m_esports_mobile.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val socketService: SocketService
) : ViewModel() {

    private val _games = MutableStateFlow<List<GameResponse>>(emptyList())
    val games: StateFlow<List<GameResponse>> = _games.asStateFlow()

    private val _selectedGame = MutableStateFlow<GameResponse?>(null)
    val selectedGame: StateFlow<GameResponse?> = _selectedGame.asStateFlow()

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Idle)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun loadGames() {
        viewModelScope.launch {
            _uiState.value = GameUiState.Loading
            when (val result = gameRepository.getAllGames()) {
                is Result.Success -> {
                    _games.value = result.data
                    _uiState.value = GameUiState.Idle
                    setupSocketListeners()
                }
                is Result.Error -> {
                    _uiState.value = GameUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    private fun setupSocketListeners() {
        socketService.connect()

        // Listen for game updates
        socketService.on("game:updated") {
            loadGames()
        }

        // Listen for game creations
        socketService.on("game:created") {
            loadGames()
        }

        // Listen for game deletions
        socketService.on("game:deleted") { args ->
            val data = args[0] as? Map<*, *>
            val gameId = data?.get("gameId") as? String
            if (gameId != null) {
                _games.value = _games.value.filter { it.id != gameId }
            }
        }
    }

    fun loadGameById(id: String) {
        viewModelScope.launch {
            _uiState.value = GameUiState.Loading
            when (val result = gameRepository.getGameById(id)) {
                is Result.Success -> {
                    _selectedGame.value = result.data
                    _uiState.value = GameUiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = GameUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun createGame(name: String, description: String?, logo: String?, rules: String?, formats: List<String>?) {
        viewModelScope.launch {
            _uiState.value = GameUiState.Loading
            val request = CreateGameRequest(
                name = name,
                description = description,
                logo = logo,
                rules = rules,
                formats = formats
            )
            when (val result = gameRepository.createGame(request)) {
                is Result.Success -> {
                    _games.value = _games.value + result.data
                    _uiState.value = GameUiState.Success("Jeu créé avec succès")
                }
                is Result.Error -> {
                    _uiState.value = GameUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun updateGame(id: String, name: String?, description: String?, logo: String?, rules: String?, isActive: Boolean?) {
        viewModelScope.launch {
            _uiState.value = GameUiState.Loading
            val request = UpdateGameRequest(
                name = name,
                description = description,
                logo = logo,
                rules = rules,
                isActive = isActive
            )
            when (val result = gameRepository.updateGame(id, request)) {
                is Result.Success -> {
                    _games.value = _games.value.map { if (it.id == id) result.data else it }
                    _selectedGame.value = result.data
                    _uiState.value = GameUiState.Success("Jeu mis à jour avec succès")
                }
                is Result.Error -> {
                    _uiState.value = GameUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun deleteGame(id: String) {
        viewModelScope.launch {
            _uiState.value = GameUiState.Loading
            when (val result = gameRepository.deleteGame(id)) {
                is Result.Success -> {
                    _games.value = _games.value.filter { it.id != id }
                    _uiState.value = GameUiState.Success("Jeu supprimé avec succès")
                }
                is Result.Error -> {
                    _uiState.value = GameUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = GameUiState.Idle
    }
}

sealed class GameUiState {
    object Idle : GameUiState()
    object Loading : GameUiState()
    data class Success(val message: String) : GameUiState()
    data class Error(val message: String) : GameUiState()
}

