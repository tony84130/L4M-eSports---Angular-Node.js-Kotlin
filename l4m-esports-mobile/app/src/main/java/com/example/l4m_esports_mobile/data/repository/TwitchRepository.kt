package com.example.l4m_esports_mobile.data.repository

import com.example.l4m_esports_mobile.data.remote.TwitchApiService
import com.example.l4m_esports_mobile.data.model.response.TwitchUserResponse
import com.example.l4m_esports_mobile.util.NetworkErrorHandler
import com.example.l4m_esports_mobile.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TwitchRepository @Inject constructor(
    private val twitchApiService: TwitchApiService
) {
    suspend fun getTwitchUser(username: String): Result<TwitchUserResponse> {
        return try {
            val response = twitchApiService.getTwitchUser(username)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                val errorMessage = response.body()?.message ?: "Utilisateur Twitch introuvable"
                Result.Error(NetworkErrorHandler.handleError(Exception(errorMessage)))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }
}

