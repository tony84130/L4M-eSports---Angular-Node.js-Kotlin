package com.example.l4m_esports_mobile.data.model.request

data class CreateEventRequest(
    val name: String,
    val game: String,
    val startDate: String,
    val endDate: String,
    val registrationStartDate: String,
    val registrationEndDate: String,
    val format: String,
    val rules: String?,
    val description: String?,
    val location: LocationRequest?,
    val maxTeams: Int?
)

data class UpdateEventRequest(
    val name: String?,
    val startDate: String?,
    val endDate: String?,
    val registrationStartDate: String?,
    val registrationEndDate: String?,
    val format: String?,
    val rules: String?,
    val description: String?,
    val location: LocationRequest?,
    val maxTeams: Int?,
    val status: String?
)

data class LocationRequest(
    val type: String,
    val address: String?,
    val coordinates: CoordinatesRequest?
)

data class CoordinatesRequest(
    val latitude: Double?,
    val longitude: Double?
)

data class CreateEventRegistrationRequest(
    val teamId: String,
    val eventId: String,
    val participatingMemberIds: List<String>? = null
)

data class UpdateEventRegistrationRequest(
    val status: String
)

