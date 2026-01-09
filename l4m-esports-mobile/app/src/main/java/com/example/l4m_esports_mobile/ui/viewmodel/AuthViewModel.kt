package com.example.l4m_esports_mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.l4m_esports_mobile.data.model.request.SignInRequest
import com.example.l4m_esports_mobile.data.model.request.SignUpRequest
import com.example.l4m_esports_mobile.data.repository.AuthRepository
import com.example.l4m_esports_mobile.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signUp(email: String, password: String, firstName: String, lastName: String, gamertag: String, twitchUsername: String?) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.signUp(
                SignUpRequest(
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    gamertag = gamertag,
                    twitchUsername = twitchUsername
                )
            )
            _uiState.value = when (result) {
                is Result.Success -> AuthUiState.Success("Inscription réussie")
                is Result.Error -> AuthUiState.Error(result.message)
                else -> AuthUiState.Idle
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.signIn(
                SignInRequest(
                    email = email,
                    password = password
                )
            )
            _uiState.value = when (result) {
                is Result.Success -> AuthUiState.Success("Connexion réussie")
                is Result.Error -> AuthUiState.Error(result.message)
                else -> AuthUiState.Idle
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.signOut()
            _uiState.value = when (result) {
                is Result.Success -> AuthUiState.Success("Déconnexion réussie")
                is Result.Error -> AuthUiState.Error(result.message)
                else -> AuthUiState.Idle
            }
        }
    }

    fun clearError() {
        _uiState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

