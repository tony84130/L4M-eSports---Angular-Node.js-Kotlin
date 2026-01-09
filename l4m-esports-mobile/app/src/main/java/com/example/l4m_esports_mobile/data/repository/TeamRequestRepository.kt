package com.example.l4m_esports_mobile.data.repository

import com.example.l4m_esports_mobile.data.remote.TeamRequestApiService
import com.example.l4m_esports_mobile.data.model.request.CreateTeamRequestRequest
import com.example.l4m_esports_mobile.data.model.response.TeamRequestResponse
import com.example.l4m_esports_mobile.util.NetworkErrorHandler
import com.example.l4m_esports_mobile.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamRequestRepository @Inject constructor(
    private val teamRequestApiService: TeamRequestApiService
) {
    suspend fun getAllTeamRequests(team: String? = null, user: String? = null, status: String? = null): Result<List<TeamRequestResponse>> {
        return try {
            val response = teamRequestApiService.getAllTeamRequests(team, user, status)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun getTeamRequestById(id: String): Result<TeamRequestResponse> {
        return try {
            val response = teamRequestApiService.getTeamRequestById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun createTeamRequest(request: CreateTeamRequestRequest): Result<TeamRequestResponse> {
        return try {
            val response = teamRequestApiService.createTeamRequest(request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun acceptTeamRequest(id: String): Result<TeamRequestResponse> {
        return try {
            val response = teamRequestApiService.acceptTeamRequest(id)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun rejectTeamRequest(id: String): Result<TeamRequestResponse> {
        return try {
            val response = teamRequestApiService.rejectTeamRequest(id)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun deleteTeamRequest(id: String): Result<Unit> {
        return try {
            val response = teamRequestApiService.deleteTeamRequest(id)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun getTeamRequestsByTeam(teamId: String): Result<List<TeamRequestResponse>> {
        return try {
            val response = teamRequestApiService.getTeamRequestsByTeam(teamId)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }
}

