package com.example.l4m_esports_mobile.data.model.request

data class UpdateUserRequest(
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val password: String?,
    val twitchUsername: String?,
    val location: UserLocationRequest?,
    val avatar: String?
)

data class UserLocationRequest(
    val latitude: Double?,
    val longitude: Double?
)

data class UpdateRoleRequest(
    val role: String
)

