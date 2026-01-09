package com.example.l4m_esports_mobile.data.repository

import com.example.l4m_esports_mobile.data.remote.UserApiService
import com.example.l4m_esports_mobile.data.model.request.UpdateRoleRequest
import com.example.l4m_esports_mobile.data.model.request.UpdateUserRequest
import com.example.l4m_esports_mobile.data.model.response.UserResponse
import com.example.l4m_esports_mobile.util.NetworkErrorHandler
import com.example.l4m_esports_mobile.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService
) {
    suspend fun getAllUsers(): Result<List<UserResponse>> {
        return try {
            // Passer une limite élevée pour récupérer tous les utilisateurs
            val response = userApiService.getAllUsers(limit = 1000)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun getCurrentUser(): Result<UserResponse> {
        return try {
            val response = userApiService.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun updateCurrentUser(request: UpdateUserRequest): Result<UserResponse> {
        return try {
            val response = userApiService.updateCurrentUser(request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun getUserById(id: String): Result<UserResponse> {
        return try {
            val response = userApiService.getUserById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun updateUser(id: String, request: UpdateUserRequest): Result<UserResponse> {
        return try {
            val response = userApiService.updateUser(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun updateUserRole(id: String, role: String): Result<UserResponse> {
        return try {
            val response = userApiService.updateUserRole(id, UpdateRoleRequest(role))
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun deleteCurrentUser(): Result<Unit> {
        return try {
            val response = userApiService.deleteCurrentUser()
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun deleteUser(id: String): Result<Unit> {
        return try {
            val response = userApiService.deleteUser(id)
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

