package com.example.l4m_esports_mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.l4m_esports_mobile.data.model.response.NotificationResponse
import com.example.l4m_esports_mobile.data.repository.NotificationRepository
import com.example.l4m_esports_mobile.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationResponse>>(emptyList())
    val notifications: StateFlow<List<NotificationResponse>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow<Int>(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Idle)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    fun loadNotifications(read: Boolean? = null, type: String? = null) {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            when (val result = notificationRepository.getAllNotifications(read, type)) {
                is Result.Success -> {
                    _notifications.value = result.data
                    _uiState.value = NotificationUiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = NotificationUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            when (val result = notificationRepository.getUnreadCount()) {
                is Result.Success -> {
                    _unreadCount.value = result.data
                }
                is Result.Error -> {
                    // Silently fail for unread count
                }
                else -> {}
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            when (val result = notificationRepository.markAsRead(id)) {
                is Result.Success -> {
                    _notifications.value = _notifications.value.map { 
                        if (it.id == id) result.data else it 
                    }
                    // Update unread count
                    loadUnreadCount()
                    _uiState.value = NotificationUiState.Success("Notification marquée comme lue")
                }
                is Result.Error -> {
                    _uiState.value = NotificationUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            when (val result = notificationRepository.markAllAsRead()) {
                is Result.Success -> {
                    // Reload notifications to update read status
                    loadNotifications()
                    loadUnreadCount()
                    _uiState.value = NotificationUiState.Success("Toutes les notifications marquées comme lues")
                }
                is Result.Error -> {
                    _uiState.value = NotificationUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            when (val result = notificationRepository.deleteNotification(id)) {
                is Result.Success -> {
                    _notifications.value = _notifications.value.filter { it.id != id }
                    // Update unread count
                    loadUnreadCount()
                    _uiState.value = NotificationUiState.Success("Notification supprimée")
                }
                is Result.Error -> {
                    _uiState.value = NotificationUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = NotificationUiState.Idle
    }
}

sealed class NotificationUiState {
    object Idle : NotificationUiState()
    object Loading : NotificationUiState()
    data class Success(val message: String) : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}

