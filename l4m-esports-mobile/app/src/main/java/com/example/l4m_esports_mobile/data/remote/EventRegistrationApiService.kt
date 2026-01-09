package com.example.l4m_esports_mobile.data.remote

import com.example.l4m_esports_mobile.data.model.request.CreateEventRegistrationRequest
import com.example.l4m_esports_mobile.data.model.request.UpdateEventRegistrationRequest
import com.example.l4m_esports_mobile.data.model.response.ApiResponse
import com.example.l4m_esports_mobile.data.model.response.EventRegistrationDataResponse
import com.example.l4m_esports_mobile.data.model.response.EventRegistrationResponse
import com.example.l4m_esports_mobile.data.model.response.EventRegistrationsDataResponse
import retrofit2.Response
import retrofit2.http.*

interface EventRegistrationApiService {
    @GET("event-registrations")
    suspend fun getRegistrations(
        @Query("event") event: String? = null,
        @Query("team") team: String? = null
    ): Response<ApiResponse<EventRegistrationsDataResponse>>

    @GET("event-registrations/event/{eventId}")
    suspend fun getRegistrationsByEvent(@Path("eventId") eventId: String): Response<ApiResponse<EventRegistrationsDataResponse>>

    @GET("event-registrations/team/{teamId}")
    suspend fun getRegistrationsByTeam(@Path("teamId") teamId: String): Response<ApiResponse<EventRegistrationsDataResponse>>

    @GET("event-registrations/{id}")
    suspend fun getRegistrationById(@Path("id") id: String): Response<ApiResponse<EventRegistrationDataResponse>>

    @POST("event-registrations")
    suspend fun createRegistration(@Body request: CreateEventRegistrationRequest): Response<ApiResponse<EventRegistrationDataResponse>>

    @PUT("event-registrations/{id}")
    suspend fun updateRegistration(
        @Path("id") id: String,
        @Body request: UpdateEventRegistrationRequest
    ): Response<ApiResponse<EventRegistrationDataResponse>>

    @DELETE("event-registrations/{id}")
    suspend fun deleteRegistration(@Path("id") id: String): Response<ApiResponse<Unit>>
}

