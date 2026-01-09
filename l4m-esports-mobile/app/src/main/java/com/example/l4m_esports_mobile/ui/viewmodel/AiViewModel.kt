package com.example.l4m_esports_mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.l4m_esports_mobile.data.model.request.AiRequestContext
import com.example.l4m_esports_mobile.data.model.response.AiResponse
import com.example.l4m_esports_mobile.data.repository.AiRepository
import com.example.l4m_esports_mobile.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiViewModel @Inject constructor(
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    private val _answer = MutableStateFlow<String?>(null)
    val answer: StateFlow<String?> = _answer.asStateFlow()

    fun askQuestion(question: String, context: AiRequestContext? = null) {
        if (question.trim().isEmpty()) {
            _uiState.value = AiUiState.Error("Pose une question.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AiUiState.Loading
            _answer.value = null
            when (val result = aiRepository.askAssistant(question.trim(), context)) {
                is Result.Success -> {
                    _answer.value = result.data.answer
                    _uiState.value = AiUiState.Success
                }
                is Result.Error -> {
                    _uiState.value = AiUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun clearState() {
        _uiState.value = AiUiState.Idle
        _answer.value = null
    }
}

sealed class AiUiState {
    object Idle : AiUiState()
    object Loading : AiUiState()
    object Success : AiUiState()
    data class Error(val message: String) : AiUiState()
}

