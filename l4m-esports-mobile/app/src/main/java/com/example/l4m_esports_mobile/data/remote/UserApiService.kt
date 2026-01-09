package com.example.l4m_esports_mobile.data.remote

import com.example.l4m_esports_mobile.data.model.request.UpdateRoleRequest
import com.example.l4m_esports_mobile.data.model.request.UpdateUserRequest
import com.example.l4m_esports_mobile.data.model.response.ApiResponse
import com.example.l4m_esports_mobile.data.model.response.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {
    @GET("users")
    suspend fun getAllUsers(
        @Query("limit") limit: Int? = null
    ): Response<ApiResponse<List<UserResponse>>>

    @GET("users/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserResponse>>

    @PUT("users/me")
    suspend fun updateCurrentUser(@Body request: UpdateUserRequest): Response<ApiResponse<UserResponse>>

    @DELETE("users/me")
    suspend fun deleteCurrentUser(): Response<ApiResponse<Unit>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<ApiResponse<UserResponse>>

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body request: UpdateUserRequest): Response<ApiResponse<UserResponse>>

    @PUT("users/{id}/role")
    suspend fun updateUserRole(@Path("id") id: String, @Body request: UpdateRoleRequest): Response<ApiResponse<UserResponse>>

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<ApiResponse<Unit>>
}

