package com.example.l4m_esports_mobile.data.repository

import com.example.l4m_esports_mobile.data.remote.TeamApiService
import com.example.l4m_esports_mobile.data.model.request.CreateTeamRequest
import com.example.l4m_esports_mobile.data.model.request.InviteMemberRequest
import com.example.l4m_esports_mobile.data.model.request.UpdateTeamRequest
import com.example.l4m_esports_mobile.data.model.request.TransferCaptainRequest
import com.example.l4m_esports_mobile.data.model.response.TeamResponse
import com.example.l4m_esports_mobile.util.NetworkErrorHandler
import com.example.l4m_esports_mobile.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamRepository @Inject constructor(
    private val teamApiService: TeamApiService
) {
    suspend fun getAllTeams(game: String? = null, status: String? = null): Result<List<TeamResponse>> {
        return try {
            val response = teamApiService.getAllTeams(game, status)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun getTeamById(id: String): Result<TeamResponse> {
        return try {
            val response = teamApiService.getTeamById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun createTeam(request: CreateTeamRequest): Result<TeamResponse> {
        return try {
            val response = teamApiService.createTeam(request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun updateTeam(id: String, request: UpdateTeamRequest): Result<TeamResponse> {
        return try {
            val response = teamApiService.updateTeam(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun deleteTeam(id: String): Result<Unit> {
        return try {
            val response = teamApiService.deleteTeam(id)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun inviteMember(teamId: String, gamertag: String): Result<TeamResponse> {
        return try {
            val response = teamApiService.inviteMember(teamId, InviteMemberRequest(gamertag = gamertag))
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun removeMember(teamId: String, userId: String): Result<TeamResponse> {
        return try {
            val response = teamApiService.removeMember(teamId, userId)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun transferCaptain(teamId: String, newCaptainId: String): Result<TeamResponse> {
        return try {
            val response = teamApiService.transferCaptain(teamId, TransferCaptainRequest(newCaptainId))
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun leaveTeam(teamId: String): Result<TeamResponse> {
        return try {
            val response = teamApiService.leaveTeam(teamId)
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

