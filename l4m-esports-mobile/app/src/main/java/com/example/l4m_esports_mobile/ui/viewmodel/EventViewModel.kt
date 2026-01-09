package com.example.l4m_esports_mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.l4m_esports_mobile.data.model.request.CreateEventRequest
import com.example.l4m_esports_mobile.data.model.request.UpdateEventRequest
import com.example.l4m_esports_mobile.data.model.response.EventResponse
import com.example.l4m_esports_mobile.data.remote.SocketService
import com.example.l4m_esports_mobile.data.repository.EventRepository
import com.example.l4m_esports_mobile.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val socketService: SocketService
) : ViewModel() {

    private val _events = MutableStateFlow<List<EventResponse>>(emptyList())
    val events: StateFlow<List<EventResponse>> = _events.asStateFlow()

    private val _selectedEvent = MutableStateFlow<EventResponse?>(null)
    val selectedEvent: StateFlow<EventResponse?> = _selectedEvent.asStateFlow()

    private val _eventBracket = MutableStateFlow<com.example.l4m_esports_mobile.data.model.response.BracketInfo?>(null)
    val eventBracket: StateFlow<com.example.l4m_esports_mobile.data.model.response.BracketInfo?> = _eventBracket.asStateFlow()

    private val _uiState = MutableStateFlow<EventUiState>(EventUiState.Idle)
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    private var socketListenersSetup = false

    fun loadEvents(game: String? = null, status: String? = null, upcoming: String? = null) {
        viewModelScope.launch {
            _uiState.value = EventUiState.Loading
            when (val result = eventRepository.getAllEvents(game, status, upcoming)) {
                is Result.Success -> {
                    _events.value = result.data
                    _uiState.value = EventUiState.Idle
                    setupSocketListeners()
                }
                is Result.Error -> {
                    _uiState.value = EventUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    private fun setupSocketListeners() {
        // Éviter de configurer les listeners plusieurs fois
        if (socketListenersSetup) return
        socketListenersSetup = true
        
        socketService.connect()

        // Listen for event updates
        socketService.on("event:updated") { args ->
            val data = args[0] as? Map<*, *>
            val eventId = data?.get("eventId") as? String
            
            // Recharger la liste des événements
            loadEvents()
            
            // Si l'événement mis à jour est celui actuellement sélectionné, le recharger aussi
            val selectedEventId = _selectedEvent.value?.id
            if (eventId != null && eventId == selectedEventId) {
                loadEventById(eventId)
                // Recharger aussi le bracket si l'événement a un bracket
                _selectedEvent.value?.let { event ->
                    if (event.bracket != null) {
                        getEventBracket(event.id)
                    }
                }
            }
        }

        // Listen for event creations
        socketService.on("event:created") {
            loadEvents()
        }

        // Listen for event deletions
        socketService.on("event:deleted") { args ->
            val data = args[0] as? Map<*, *>
            val eventId = data?.get("eventId") as? String
            if (eventId != null) {
                _events.value = _events.value.filter { it.id != eventId }
                // Si l'événement supprimé est celui actuellement sélectionné, le vider
                if (_selectedEvent.value?.id == eventId) {
                    _selectedEvent.value = null
                }
            }
        }
    }

    fun loadEventById(id: String) {
        viewModelScope.launch {
            _uiState.value = EventUiState.Loading
            when (val result = eventRepository.getEventById(id)) {
                is Result.Success -> {
                    _selectedEvent.value = result.data
                    _uiState.value = EventUiState.Idle
                    // Configurer les listeners Socket.io si ce n'est pas déjà fait
                    setupSocketListeners()
                }
                is Result.Error -> {
                    _uiState.value = EventUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun createEvent(
        name: String,
        game: String,
        startDate: String,
        endDate: String,
        registrationStartDate: String,
        registrationEndDate: String,
        format: String,
        rules: String?,
        description: String?,
        locationType: String?,
        address: String?,
        latitude: Double?,
        longitude: Double?,
        maxTeams: Int?
    ) {
        viewModelScope.launch {
            _uiState.value = EventUiState.Loading
            val request = CreateEventRequest(
                name = name,
                game = game,
                startDate = startDate,
                endDate = endDate,
                registrationStartDate = registrationStartDate,
                registrationEndDate = registrationEndDate,
                format = format,
                rules = rules,
                description = description,
                location = if (locationType != null) {
                    com.example.l4m_esports_mobile.data.model.request.LocationRequest(
                        type = locationType,
                        address = address,
                        coordinates = if (latitude != null && longitude != null) {
                            com.example.l4m_esports_mobile.data.model.request.CoordinatesRequest(
                                latitude = latitude,
                                longitude = longitude
                            )
                        } else null
                    )
                } else null,
                maxTeams = maxTeams
            )
            when (val result = eventRepository.createEvent(request)) {
                is Result.Success -> {
                    // Ajouter l'événement créé à la liste locale
                    _events.value = _events.value + result.data
                    _uiState.value = EventUiState.Success("Événement créé avec succès")
                }
                is Result.Error -> {
                    _uiState.value = EventUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun updateEvent(
        id: String,
        name: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        registrationStartDate: String? = null,
        registrationEndDate: String? = null,
        format: String? = null,
        rules: String? = null,
        description: String? = null,
        locationType: String? = null,
        address: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        maxTeams: Int? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = EventUiState.Loading
            val request = UpdateEventRequest(
                name = name,
                startDate = startDate,
                endDate = endDate,
                registrationStartDate = registrationStartDate,
                registrationEndDate = registrationEndDate,
                format = format,
                rules = rules,
                description = description,
                location = if (locationType != null) {
                    com.example.l4m_esports_mobile.data.model.request.LocationRequest(
                        type = locationType,
                        address = address,
                        coordinates = if (latitude != null && longitude != null) {
                            com.example.l4m_esports_mobile.data.model.request.CoordinatesRequest(
                                latitude = latitude,
                                longitude = longitude
                            )
                        } else null
                    )
                } else null,
                maxTeams = maxTeams,
                status = status
            )
            when (val result = eventRepository.updateEvent(id, request)) {
                is Result.Success -> {
                    // Mettre à jour l'événement dans la liste
                    _events.value = _events.value.map { if (it.id == id) result.data else it }
                    // Mettre à jour l'événement sélectionné
                    if (_selectedEvent.value?.id == id) {
                        _selectedEvent.value = result.data
                    }
                    _uiState.value = EventUiState.Success("Événement mis à jour avec succès")
                }
                is Result.Error -> {
                    _uiState.value = EventUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun deleteEvent(id: String) {
        viewModelScope.launch {
            _uiState.value = EventUiState.Loading
            when (val result = eventRepository.deleteEvent(id)) {
                is Result.Success -> {
                    // Retirer l'événement de la liste
                    _events.value = _events.value.filter { it.id != id }
                    // Retirer l'événement sélectionné s'il correspond
                    if (_selectedEvent.value?.id == id) {
                        _selectedEvent.value = null
                    }
                    _uiState.value = EventUiState.Success("Événement supprimé avec succès")
                }
                is Result.Error -> {
                    _uiState.value = EventUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun getEventsNearby(
        longitude: String,
        latitude: String,
        distance: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = EventUiState.Loading
            when (val result = eventRepository.getEventsNearby(longitude, latitude, distance)) {
                is Result.Success -> {
                    _events.value = result.data
                    _uiState.value = EventUiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = EventUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun getEventBracket(id: String) {
        viewModelScope.launch {
            _uiState.value = EventUiState.Loading
            when (val result = eventRepository.getEventBracket(id)) {
                is Result.Success -> {
                    // Le bracket est retourné dans result.data
                    val bracket = result.data as? com.example.l4m_esports_mobile.data.model.response.BracketInfo
                    _eventBracket.value = bracket
                    _uiState.value = EventUiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = EventUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun generateBracket(id: String) {
        viewModelScope.launch {
            _uiState.value = EventUiState.Loading
            when (val result = eventRepository.generateBracket(id)) {
                is Result.Success -> {
                    // Recharger l'événement pour avoir le bracket à jour
                    loadEventById(id)
                    // Charger aussi le bracket directement
                    val bracket = result.data as? com.example.l4m_esports_mobile.data.model.response.BracketInfo
                    _eventBracket.value = bracket
                    _uiState.value = EventUiState.Success("Bracket généré avec succès")
                }
                is Result.Error -> {
                    _uiState.value = EventUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = EventUiState.Idle
    }
}

sealed class EventUiState {
    object Idle : EventUiState()
    object Loading : EventUiState()
    data class Success(val message: String) : EventUiState()
    data class Error(val message: String) : EventUiState()
}

