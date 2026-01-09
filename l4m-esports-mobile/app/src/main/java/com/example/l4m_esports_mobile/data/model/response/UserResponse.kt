package com.example.l4m_esports_mobile.data.model.response

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("_id")
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val gamertag: String,
    val role: String,
    val twitchUsername: String?,
    val preferences: UserPreferencesResponse?,
    val location: UserLocationResponse?,
    val avatar: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?
)

data class UserPreferencesResponse(
    val favoriteGames: List<String>?,
    val notificationSettings: NotificationSettingsResponse?
)

data class NotificationSettingsResponse(
    val matchReminders: Boolean?,
    val eventNearby: Boolean?
)

data class UserLocationResponse(
    val latitude: Double?,
    val longitude: Double?
)

