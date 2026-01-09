package com.example.l4m_esports_mobile.data.repository

import com.example.l4m_esports_mobile.data.local.PreferencesManager
import com.example.l4m_esports_mobile.data.remote.AuthApiService
import com.example.l4m_esports_mobile.data.model.request.SignInRequest
import com.example.l4m_esports_mobile.data.model.request.SignUpRequest
import com.example.l4m_esports_mobile.util.NetworkErrorHandler
import com.example.l4m_esports_mobile.util.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val preferencesManager: PreferencesManager
) {
    suspend fun signUp(request: SignUpRequest): Result<Pair<String, String>> {
        return try {
            val response = authApiService.signUp(request)
            if (response.isSuccessful && response.body() != null) {
                val authData = response.body()!!.data
                preferencesManager.saveAuthToken(authData.token)
                preferencesManager.saveUserId(authData.user.id)
                Result.Success(Pair(authData.token, authData.user.id))
            } else {
                // Utiliser HttpException pour permettre à NetworkErrorHandler d'extraire le message d'erreur
                Result.Error(NetworkErrorHandler.handleError(
                    retrofit2.HttpException(response)
                ))
            }
        } catch (e: retrofit2.HttpException) {
            Result.Error(NetworkErrorHandler.handleError(e))
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun signIn(request: SignInRequest): Result<Pair<String, String>> {
        return try {
            val response = authApiService.signIn(request)
            if (response.isSuccessful && response.body() != null) {
                val authData = response.body()!!.data
                preferencesManager.saveAuthToken(authData.token)
                preferencesManager.saveUserId(authData.user.id)
                Result.Success(Pair(authData.token, authData.user.id))
            } else {
                // Utiliser HttpException pour permettre à NetworkErrorHandler d'extraire le message d'erreur
                Result.Error(NetworkErrorHandler.handleError(
                    retrofit2.HttpException(response)
                ))
            }
        } catch (e: retrofit2.HttpException) {
            Result.Error(NetworkErrorHandler.handleError(e))
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            val response = authApiService.signOut()
            if (response.isSuccessful) {
                preferencesManager.clearAuthData()
                Result.Success(Unit)
            } else {
                // Même en cas d'erreur, on efface les données locales
                preferencesManager.clearAuthData()
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            // Même en cas d'erreur, on efface les données locales
            preferencesManager.clearAuthData()
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun isAuthenticated(): Boolean {
        return preferencesManager.authToken.first() != null
    }
}

