package com.example.l4m_esports_mobile.data.remote

import com.example.l4m_esports_mobile.data.model.request.CreateEventRequest
import com.example.l4m_esports_mobile.data.model.request.UpdateEventRequest
import com.example.l4m_esports_mobile.data.model.response.ApiResponse
import com.example.l4m_esports_mobile.data.model.response.BracketDataResponse
import com.example.l4m_esports_mobile.data.model.response.EventDataResponse
import com.example.l4m_esports_mobile.data.model.response.EventsDataResponse
import retrofit2.Response
import retrofit2.http.*

interface EventApiService {
    @GET("events")
    suspend fun getAllEvents(
        @Query("game") game: String? = null,
        @Query("status") status: String? = null,
        @Query("upcoming") upcoming: String? = null
    ): Response<ApiResponse<EventsDataResponse>>

    @GET("events/{id}")
    suspend fun getEventById(@Path("id") id: String): Response<ApiResponse<EventDataResponse>>

    @GET("events/nearby")
    suspend fun getEventsNearby(
        @Query("long") longitude: String,
        @Query("lat") latitude: String,
        @Query("distance") distance: String? = null
    ): Response<ApiResponse<EventsDataResponse>>

    @GET("events/{id}/bracket")
    suspend fun getEventBracket(@Path("id") id: String): Response<ApiResponse<BracketDataResponse>>

    @POST("events")
    suspend fun createEvent(@Body request: CreateEventRequest): Response<ApiResponse<EventDataResponse>>

    @PUT("events/{id}")
    suspend fun updateEvent(
        @Path("id") id: String,
        @Body request: UpdateEventRequest
    ): Response<ApiResponse<EventDataResponse>>

    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") id: String): Response<ApiResponse<Unit>>

    @POST("events/{id}/generate-bracket")
    suspend fun generateBracket(@Path("id") id: String): Response<ApiResponse<BracketDataResponse>>
}

