package com.example.l4m_esports_mobile.data.model.request

data class UpdateMatchScoreRequest(
    val score: ScoreRequest
)

data class ScoreRequest(
    val team1: Int,
    val team2: Int
)

