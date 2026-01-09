package com.example.l4m_esports_mobile.data.remote

import com.example.l4m_esports_mobile.data.model.request.SignInRequest
import com.example.l4m_esports_mobile.data.model.request.SignUpRequest
import com.example.l4m_esports_mobile.data.model.response.ApiResponse
import com.example.l4m_esports_mobile.data.model.response.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/sign-up")
    suspend fun signUp(@Body request: SignUpRequest): Response<AuthResponse>

    @POST("auth/sign-in")
    suspend fun signIn(@Body request: SignInRequest): Response<AuthResponse>

    @POST("auth/sign-out")
    suspend fun signOut(): Response<ApiResponse<Unit>>
}

