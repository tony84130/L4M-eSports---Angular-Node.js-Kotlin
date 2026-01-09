package com.example.l4m_esports_mobile.data.model.response

data class EventsDataResponse(
    val events: List<EventResponse>
)

data class EventDataResponse(
    val event: EventResponse
)

data class BracketDataResponse(
    val bracket: Any?
)

data class EventRegistrationsDataResponse(
    val registrations: List<EventRegistrationResponse>
)

data class EventRegistrationDataResponse(
    val registration: EventRegistrationResponse
)

