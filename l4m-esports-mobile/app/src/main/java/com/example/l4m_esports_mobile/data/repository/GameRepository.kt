package com.example.l4m_esports_mobile.data.repository

import com.example.l4m_esports_mobile.data.remote.GameApiService
import com.example.l4m_esports_mobile.data.model.request.CreateGameRequest
import com.example.l4m_esports_mobile.data.model.request.UpdateGameRequest
import com.example.l4m_esports_mobile.data.model.response.GameResponse
import com.example.l4m_esports_mobile.util.NetworkErrorHandler
import com.example.l4m_esports_mobile.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val gameApiService: GameApiService
) {
    suspend fun getAllGames(): Result<List<GameResponse>> {
        return try {
            val response = gameApiService.getAllGames()
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun getGameById(id: String): Result<GameResponse> {
        return try {
            val response = gameApiService.getGameById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun createGame(request: CreateGameRequest): Result<GameResponse> {
        return try {
            val response = gameApiService.createGame(request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun updateGame(id: String, request: UpdateGameRequest): Result<GameResponse> {
        return try {
            val response = gameApiService.updateGame(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun deleteGame(id: String): Result<Unit> {
        return try {
            val response = gameApiService.deleteGame(id)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }
}

