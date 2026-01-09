package com.example.l4m_esports_mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.l4m_esports_mobile.data.local.PreferencesManager
import com.example.l4m_esports_mobile.data.model.request.UpdateUserRequest
import com.example.l4m_esports_mobile.data.model.request.UserLocationRequest
import com.example.l4m_esports_mobile.data.model.response.TwitchUserResponse
import com.example.l4m_esports_mobile.data.model.response.UserResponse
import com.example.l4m_esports_mobile.data.remote.SocketService
import com.example.l4m_esports_mobile.data.repository.TwitchRepository
import com.example.l4m_esports_mobile.data.repository.UserRepository
import com.example.l4m_esports_mobile.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val twitchRepository: TwitchRepository,
    private val preferencesManager: PreferencesManager,
    private val socketService: SocketService
) : ViewModel() {

    private val _currentUser = MutableStateFlow<UserResponse?>(null)
    val currentUser: StateFlow<UserResponse?> = _currentUser.asStateFlow()

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Idle)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    private val _twitchUser = MutableStateFlow<TwitchUserResponse?>(null)
    val twitchUser: StateFlow<TwitchUserResponse?> = _twitchUser.asStateFlow()

    private val _twitchState = MutableStateFlow<TwitchUiState>(TwitchUiState.Idle)
    val twitchState: StateFlow<TwitchUiState> = _twitchState.asStateFlow()

    private val _allUsers = MutableStateFlow<List<UserResponse>>(emptyList())
    val allUsers: StateFlow<List<UserResponse>> = _allUsers.asStateFlow()

    fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            when (val result = userRepository.getCurrentUser()) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    _uiState.value = UserUiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = UserUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun updateCurrentUser(
        firstName: String?,
        lastName: String?,
        email: String?,
        password: String?,
        twitchUsername: String?,
        latitude: Double?,
        longitude: Double?,
        avatar: String?
    ) {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            val request = UpdateUserRequest(
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password,
                twitchUsername = twitchUsername,
                location = if (latitude != null && longitude != null) {
                    UserLocationRequest(latitude, longitude)
                } else null,
                avatar = avatar
            )
            when (val result = userRepository.updateCurrentUser(request)) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    _uiState.value = UserUiState.Success("Profil mis à jour avec succès")
                }
                is Result.Error -> {
                    _uiState.value = UserUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun deleteCurrentUser() {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            when (val result = userRepository.deleteCurrentUser()) {
                is Result.Success -> {
                    // Effacer les données d'authentification locales
                    preferencesManager.clearAuthData()
                    _currentUser.value = null
                    _uiState.value = UserUiState.Success("Compte supprimé avec succès")
                }
                is Result.Error -> {
                    _uiState.value = UserUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = UserUiState.Idle
    }

    fun fetchTwitchUser(username: String) {
        viewModelScope.launch {
            _twitchState.value = TwitchUiState.Loading
            when (val result = twitchRepository.getTwitchUser(username)) {
                is Result.Success -> {
                    _twitchUser.value = result.data
                    _twitchState.value = TwitchUiState.Success("Utilisateur Twitch trouvé. Sauvegarde pour lier.")
                }
                is Result.Error -> {
                    _twitchUser.value = null
                    _twitchState.value = TwitchUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun clearTwitchState() {
        _twitchState.value = TwitchUiState.Idle
        _twitchUser.value = null
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            when (val result = userRepository.getAllUsers()) {
                is Result.Success -> {
                    _allUsers.value = result.data
                    _uiState.value = UserUiState.Idle
                    setupSocketListeners()
                }
                is Result.Error -> {
                    _uiState.value = UserUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    private fun setupSocketListeners() {
        socketService.connect()

        // Listen for user updates
        socketService.on("user:updated") {
            loadAllUsers()
        }

        // Listen for user deletions
        socketService.on("user:deleted") { args ->
            val data = args[0] as? Map<*, *>
            val userId = data?.get("userId") as? String
            if (userId != null) {
                _allUsers.value = _allUsers.value.filter { it.id != userId }
            }
        }

        // Listen for role updates
        socketService.on("user:roleUpdated") {
            loadAllUsers()
        }
    }

    fun deleteUser(userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            when (val result = userRepository.deleteUser(userId)) {
                is Result.Success -> {
                    // Recharger la liste des utilisateurs
                    loadAllUsers()
                    _uiState.value = UserUiState.Success("Utilisateur supprimé avec succès")
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.value = UserUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }
}

sealed class UserUiState {
    object Idle : UserUiState()
    object Loading : UserUiState()
    data class Success(val message: String) : UserUiState()
    data class Error(val message: String) : UserUiState()
}

sealed class TwitchUiState {
    object Idle : TwitchUiState()
    object Loading : TwitchUiState()
    data class Success(val message: String) : TwitchUiState()
    data class Error(val message: String) : TwitchUiState()
}

