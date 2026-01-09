package com.example.l4m_esports_mobile.data.model.request

data class CreateTeamRequest(
    val name: String,
    val logo: String?,
    val game: String, // Game ID
    val description: String?,
    val maxMembers: Int?
)

data class UpdateTeamRequest(
    val name: String?,
    val logo: String?,
    val description: String?,
    val status: String?,
    val maxMembers: Int?
)

data class InviteMemberRequest(
    val userId: String? = null,
    val gamertag: String? = null
)

data class TransferCaptainRequest(
    val newCaptainId: String
)

