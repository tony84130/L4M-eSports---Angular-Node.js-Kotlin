package com.example.l4m_esports_mobile.data.repository

import com.example.l4m_esports_mobile.data.model.request.UpdateMatchScoreRequest
import com.example.l4m_esports_mobile.data.model.request.UpdateMatchStatusRequest
import com.example.l4m_esports_mobile.data.model.response.MatchDataResponse
import com.example.l4m_esports_mobile.data.model.response.MatchResponse
import com.example.l4m_esports_mobile.data.model.response.MatchesDataResponse
import com.example.l4m_esports_mobile.data.remote.MatchApiService
import com.example.l4m_esports_mobile.util.NetworkErrorHandler
import com.example.l4m_esports_mobile.util.Result
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepository @Inject constructor(
    private val matchApiService: MatchApiService
) {
    suspend fun getAllMatches(
        event: String? = null,
        status: String? = null,
        team: String? = null
    ): Result<List<MatchResponse>> {
        return try {
            val response = matchApiService.getAllMatches(event, status, team)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data as? MatchesDataResponse
                Result.Success(data?.matches ?: emptyList())
            } else {
                Result.Error(NetworkErrorHandler.handleError(HttpException(response)))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun getMatchById(id: String): Result<MatchResponse> {
        return try {
            val response = matchApiService.getMatchById(id)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data as? MatchDataResponse
                Result.Success(data?.match ?: throw Exception("Match data is null"))
            } else {
                Result.Error(NetworkErrorHandler.handleError(HttpException(response)))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun getMatchesByEvent(eventId: String): Result<List<MatchResponse>> {
        return try {
            val response = matchApiService.getMatchesByEvent(eventId)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data as? MatchesDataResponse
                Result.Success(data?.matches ?: emptyList())
            } else {
                Result.Error(NetworkErrorHandler.handleError(HttpException(response)))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun getMatchesByTeam(teamId: String): Result<List<MatchResponse>> {
        return try {
            val response = matchApiService.getMatchesByTeam(teamId)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data as? MatchesDataResponse
                Result.Success(data?.matches ?: emptyList())
            } else {
                Result.Error(NetworkErrorHandler.handleError(HttpException(response)))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun updateMatchStatus(id: String, status: String): Result<MatchResponse> {
        return try {
            val response = matchApiService.updateMatchStatus(id, UpdateMatchStatusRequest(status))
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data as? MatchDataResponse
                Result.Success(data?.match ?: throw Exception("Match data is null"))
            } else {
                Result.Error(NetworkErrorHandler.handleError(HttpException(response)))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun updateMatchScore(id: String, team1Score: Int, team2Score: Int): Result<MatchResponse> {
        return try {
            val response = matchApiService.updateMatchScore(
                id,
                UpdateMatchScoreRequest(
                    com.example.l4m_esports_mobile.data.model.request.ScoreRequest(team1Score, team2Score)
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data as? MatchDataResponse
                Result.Success(data?.match ?: throw Exception("Match data is null"))
            } else {
                Result.Error(NetworkErrorHandler.handleError(HttpException(response)))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun validateMatchResult(id: String): Result<MatchResponse> {
        return try {
            val response = matchApiService.validateMatchResult(id)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data as? MatchDataResponse
                Result.Success(data?.match ?: throw Exception("Match data is null"))
            } else {
                Result.Error(NetworkErrorHandler.handleError(HttpException(response)))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }
}

