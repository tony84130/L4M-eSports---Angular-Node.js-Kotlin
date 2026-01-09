package com.example.l4m_esports_mobile.data.repository

import com.example.l4m_esports_mobile.data.model.request.CreateEventRegistrationRequest
import com.example.l4m_esports_mobile.data.model.request.UpdateEventRegistrationRequest
import com.example.l4m_esports_mobile.data.model.response.EventRegistrationDataResponse
import com.example.l4m_esports_mobile.data.model.response.EventRegistrationResponse
import com.example.l4m_esports_mobile.data.model.response.EventRegistrationsDataResponse
import com.example.l4m_esports_mobile.data.remote.EventRegistrationApiService
import com.example.l4m_esports_mobile.util.NetworkErrorHandler
import com.example.l4m_esports_mobile.util.Result
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRegistrationRepository @Inject constructor(
    private val eventRegistrationApiService: EventRegistrationApiService
) {
    suspend fun getRegistrations(
        event: String? = null,
        team: String? = null
    ): Result<List<EventRegistrationResponse>> {
        return try {
            val response = eventRegistrationApiService.getRegistrations(event, team)
            if (response.isSuccessful && response.body() != null) {
                // Le serveur retourne { success: true, data: { registrations: [...] } }
                val data = response.body()!!.data as? EventRegistrationsDataResponse
                Result.Success(data?.registrations ?: emptyList())
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun getRegistrationsByEvent(eventId: String): Result<List<EventRegistrationResponse>> {
        return try {
            val response = eventRegistrationApiService.getRegistrationsByEvent(eventId)
            if (response.isSuccessful && response.body() != null) {
                // Le serveur retourne { success: true, data: { registrations: [...] } }
                val data = response.body()!!.data as? EventRegistrationsDataResponse
                Result.Success(data?.registrations ?: emptyList())
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun getRegistrationsByTeam(teamId: String): Result<List<EventRegistrationResponse>> {
        return try {
            val response = eventRegistrationApiService.getRegistrationsByTeam(teamId)
            if (response.isSuccessful && response.body() != null) {
                // Le serveur retourne { success: true, data: { registrations: [...] } }
                val data = response.body()!!.data as? EventRegistrationsDataResponse
                Result.Success(data?.registrations ?: emptyList())
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun getRegistrationById(id: String): Result<EventRegistrationResponse> {
        return try {
            val response = eventRegistrationApiService.getRegistrationById(id)
            if (response.isSuccessful && response.body() != null) {
                // Le serveur retourne { success: true, data: { registration: {...} } }
                val data = response.body()!!.data as? EventRegistrationDataResponse
                Result.Success(data?.registration ?: throw Exception("Registration not found"))
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun createRegistration(request: CreateEventRegistrationRequest): Result<EventRegistrationResponse> {
        return try {
            val response = eventRegistrationApiService.createRegistration(request)
            if (response.isSuccessful && response.body() != null) {
                // Le serveur retourne { success: true, data: { registration: {...} } }
                val data = response.body()!!.data as? EventRegistrationDataResponse
                Result.Success(data?.registration ?: throw Exception("Registration not found"))
            } else {
                // Convertir la réponse non réussie en HttpException pour extraire le message d'erreur
                val httpException = HttpException(response)
                Result.Error(NetworkErrorHandler.handleError(httpException))
            }
        } catch (e: HttpException) {
            Result.Error(NetworkErrorHandler.handleError(e))
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun updateRegistration(
        id: String,
        request: UpdateEventRegistrationRequest
    ): Result<EventRegistrationResponse> {
        return try {
            val response = eventRegistrationApiService.updateRegistration(id, request)
            if (response.isSuccessful && response.body() != null) {
                // Le serveur retourne { success: true, data: { registration: {...} } }
                val data = response.body()!!.data as? EventRegistrationDataResponse
                Result.Success(data?.registration ?: throw Exception("Registration not found"))
            } else {
                // Convertir la réponse non réussie en HttpException pour extraire le message d'erreur
                val httpException = HttpException(response)
                Result.Error(NetworkErrorHandler.handleError(httpException))
            }
        } catch (e: HttpException) {
            Result.Error(NetworkErrorHandler.handleError(e))
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun deleteRegistration(id: String): Result<Unit> {
        return try {
            val response = eventRegistrationApiService.deleteRegistration(id)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                // Convertir la réponse non réussie en HttpException pour extraire le message d'erreur
                val httpException = HttpException(response)
                Result.Error(NetworkErrorHandler.handleError(httpException))
            }
        } catch (e: HttpException) {
            Result.Error(NetworkErrorHandler.handleError(e))
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }
}

