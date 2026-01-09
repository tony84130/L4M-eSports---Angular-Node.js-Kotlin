package com.example.l4m_esports_mobile.data.repository

import com.example.l4m_esports_mobile.data.model.request.CreateEventRequest
import com.example.l4m_esports_mobile.data.model.request.UpdateEventRequest
import com.example.l4m_esports_mobile.data.model.response.EventDataResponse
import com.example.l4m_esports_mobile.data.model.response.EventResponse
import com.example.l4m_esports_mobile.data.model.response.EventsDataResponse
import com.example.l4m_esports_mobile.data.model.response.BracketDataResponse
import com.example.l4m_esports_mobile.data.remote.EventApiService
import com.example.l4m_esports_mobile.util.NetworkErrorHandler
import com.example.l4m_esports_mobile.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val eventApiService: EventApiService
) {
    suspend fun getAllEvents(
        game: String? = null,
        status: String? = null,
        upcoming: String? = null
    ): Result<List<EventResponse>> {
        return try {
            val response = eventApiService.getAllEvents(game, status, upcoming)
            if (response.isSuccessful && response.body() != null) {
                // Le serveur retourne { success: true, data: { events: [...] } }
                val data = response.body()!!.data as? EventsDataResponse
                Result.Success(data?.events ?: emptyList())
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun getEventById(id: String): Result<EventResponse> {
        return try {
            val response = eventApiService.getEventById(id)
            if (response.isSuccessful && response.body() != null) {
                // Le serveur retourne { success: true, data: { event: {...} } }
                val data = response.body()!!.data as? EventDataResponse
                Result.Success(data?.event ?: throw Exception("Event not found"))
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun getEventsNearby(
        longitude: String,
        latitude: String,
        distance: String? = null
    ): Result<List<EventResponse>> {
        return try {
            val response = eventApiService.getEventsNearby(longitude, latitude, distance)
            if (response.isSuccessful && response.body() != null) {
                // Le serveur retourne { success: true, data: { events: [...] } }
                val data = response.body()!!.data as? EventsDataResponse
                Result.Success(data?.events ?: emptyList())
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun getEventBracket(id: String): Result<Any?> {
        return try {
            val response = eventApiService.getEventBracket(id)
            if (response.isSuccessful && response.body() != null) {
                // Le serveur retourne { success: true, data: { bracket: {...} } }
                val data = response.body()!!.data as? BracketDataResponse
                Result.Success(data?.bracket)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun createEvent(request: CreateEventRequest): Result<EventResponse> {
        return try {
            val response = eventApiService.createEvent(request)
            if (response.isSuccessful && response.body() != null) {
                // Le serveur retourne { success: true, data: { event: {...} } }
                val data = response.body()!!.data as? EventDataResponse
                Result.Success(data?.event ?: throw Exception("Event not found"))
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun updateEvent(id: String, request: UpdateEventRequest): Result<EventResponse> {
        return try {
            val response = eventApiService.updateEvent(id, request)
            if (response.isSuccessful && response.body() != null) {
                // Le serveur retourne { success: true, data: { event: {...} } }
                val data = response.body()!!.data as? EventDataResponse
                Result.Success(data?.event ?: throw Exception("Event not found"))
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun deleteEvent(id: String): Result<Unit> {
        return try {
            val response = eventApiService.deleteEvent(id)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun generateBracket(id: String): Result<Any?> {
        return try {
            val response = eventApiService.generateBracket(id)
            if (response.isSuccessful && response.body() != null) {
                // Le serveur retourne { success: true, data: { bracket: {...} } }
                val data = response.body()!!.data as? BracketDataResponse
                Result.Success(data?.bracket)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }
}

