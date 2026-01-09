package com.example.l4m_esports_mobile.data.remote

import com.example.l4m_esports_mobile.data.model.request.UpdateMatchScoreRequest
import com.example.l4m_esports_mobile.data.model.request.UpdateMatchStatusRequest
import com.example.l4m_esports_mobile.data.model.response.ApiResponse
import com.example.l4m_esports_mobile.data.model.response.MatchDataResponse
import com.example.l4m_esports_mobile.data.model.response.MatchesDataResponse
import retrofit2.Response
import retrofit2.http.*

interface MatchApiService {
    @GET("matches")
    suspend fun getAllMatches(
        @Query("event") event: String? = null,
        @Query("status") status: String? = null,
        @Query("team") team: String? = null
    ): Response<ApiResponse<MatchesDataResponse>>

    @GET("matches/{id}")
    suspend fun getMatchById(@Path("id") id: String): Response<ApiResponse<MatchDataResponse>>

    @GET("matches/event/{eventId}")
    suspend fun getMatchesByEvent(@Path("eventId") eventId: String): Response<ApiResponse<MatchesDataResponse>>

    @GET("matches/team/{teamId}")
    suspend fun getMatchesByTeam(@Path("teamId") teamId: String): Response<ApiResponse<MatchesDataResponse>>

    @PUT("matches/{id}/status")
    suspend fun updateMatchStatus(
        @Path("id") id: String,
        @Body request: UpdateMatchStatusRequest
    ): Response<ApiResponse<MatchDataResponse>>

    @PUT("matches/{id}/score")
    suspend fun updateMatchScore(
        @Path("id") id: String,
        @Body request: UpdateMatchScoreRequest
    ): Response<ApiResponse<MatchDataResponse>>

    @POST("matches/{id}/validate")
    suspend fun validateMatchResult(@Path("id") id: String): Response<ApiResponse<MatchDataResponse>>
}

