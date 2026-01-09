package com.example.l4m_esports_mobile.data.remote

import com.example.l4m_esports_mobile.data.model.request.CreateTeamRequestRequest
import com.example.l4m_esports_mobile.data.model.response.ApiResponse
import com.example.l4m_esports_mobile.data.model.response.TeamRequestResponse
import retrofit2.Response
import retrofit2.http.*

interface TeamRequestApiService {
    @GET("team-requests")
    suspend fun getAllTeamRequests(
        @Query("team") team: String? = null,
        @Query("user") user: String? = null,
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<TeamRequestResponse>>>

    @GET("team-requests/team/{teamId}")
    suspend fun getTeamRequestsByTeam(@Path("teamId") teamId: String): Response<ApiResponse<List<TeamRequestResponse>>>

    @GET("team-requests/{id}")
    suspend fun getTeamRequestById(@Path("id") id: String): Response<ApiResponse<TeamRequestResponse>>

    @POST("team-requests")
    suspend fun createTeamRequest(@Body request: CreateTeamRequestRequest): Response<ApiResponse<TeamRequestResponse>>

    @PUT("team-requests/{id}/accept")
    suspend fun acceptTeamRequest(@Path("id") id: String): Response<ApiResponse<TeamRequestResponse>>

    @PUT("team-requests/{id}/reject")
    suspend fun rejectTeamRequest(@Path("id") id: String): Response<ApiResponse<TeamRequestResponse>>

    @DELETE("team-requests/{id}")
    suspend fun deleteTeamRequest(@Path("id") id: String): Response<ApiResponse<Unit>>
}

