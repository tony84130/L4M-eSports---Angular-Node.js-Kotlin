package com.example.l4m_esports_mobile.data.model.response

import com.google.gson.annotations.SerializedName

// Types communs utilisés dans plusieurs réponses
data class TeamInfo(
    @SerializedName("_id")
    val id: String,
    val name: String?,
    val logo: String?,
    val captain: TeamCaptainInfo? = null  // Optionnel, présent dans les matchs
)

// Info du capitaine pour les équipes dans les matchs
data class TeamCaptainInfo(
    @SerializedName("_id")
    val id: String,
    val gamertag: String?
)

data class GameInfo(
    val id: String,
    val name: String?,
    val formats: List<String>?
)

// Info d'événement (utilisé dans MatchResponse et EventRegistrationResponse)
data class EventInfo(
    @SerializedName("_id")
    val id: String,
    val name: String?,
    @SerializedName("startDate")
    val startDate: String?,
    @SerializedName("endDate")
    val endDate: String?,
    val game: GameInfo? = null,  // Optionnel, présent dans EventRegistrationResponse
    val status: String?,
    val format: String?
)

