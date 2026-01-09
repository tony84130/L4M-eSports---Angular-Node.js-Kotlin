package com.example.l4m_esports_mobile.data.model.request

data class SignUpRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val gamertag: String,
    val twitchUsername: String?
)

data class SignInRequest(
    val email: String,
    val password: String
)

