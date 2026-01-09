package com.example.l4m_esports_mobile.data.model.response

import com.google.gson.annotations.SerializedName

data class GameResponse(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val description: String?,
    val logo: String?,
    val rules: String?,
    val formats: List<String>?,
    val isActive: Boolean,
    @SerializedName("createdBy")
    val createdBy: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?
)

