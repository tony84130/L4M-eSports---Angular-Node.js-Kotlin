package com.example.l4m_esports_mobile.data.model.request

data class CreateGameRequest(
    val name: String,
    val description: String?,
    val logo: String?,
    val rules: String?,
    val formats: List<String>?
)

data class UpdateGameRequest(
    val name: String?,
    val description: String?,
    val logo: String?,
    val rules: String?,
    val isActive: Boolean?
)

