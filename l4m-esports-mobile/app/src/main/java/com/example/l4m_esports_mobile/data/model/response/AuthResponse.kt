package com.example.l4m_esports_mobile.data.model.response

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthDataResponse
)

data class AuthDataResponse(
    val user: UserResponse,
    val token: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T
)

