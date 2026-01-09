package com.example.l4m_esports_mobile.data.remote

import com.example.l4m_esports_mobile.data.model.request.CreateTeamRequest
import com.example.l4m_esports_mobile.data.model.request.InviteMemberRequest
import com.example.l4m_esports_mobile.data.model.request.TransferCaptainRequest
import com.example.l4m_esports_mobile.data.model.request.UpdateTeamRequest
import com.example.l4m_esports_mobile.data.model.response.ApiResponse
import com.example.l4m_esports_mobile.data.model.response.TeamResponse
import retrofit2.Response
import retrofit2.http.*

interface TeamApiService {
    @GET("teams")
    suspend fun getAllTeams(
        @Query("game") game: String? = null,
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<TeamResponse>>>

    @GET("teams/{id}")
    suspend fun getTeamById(@Path("id") id: String): Response<ApiResponse<TeamResponse>>

    @POST("teams")
    suspend fun createTeam(@Body request: CreateTeamRequest): Response<ApiResponse<TeamResponse>>

    @PUT("teams/{id}")
    suspend fun updateTeam(@Path("id") id: String, @Body request: UpdateTeamRequest): Response<ApiResponse<TeamResponse>>

    @DELETE("teams/{id}")
    suspend fun deleteTeam(@Path("id") id: String): Response<ApiResponse<Unit>>

    @POST("teams/{id}/invite")
    suspend fun inviteMember(@Path("id") id: String, @Body request: InviteMemberRequest): Response<ApiResponse<TeamResponse>>

    @DELETE("teams/{id}/members/{userId}")
    suspend fun removeMember(@Path("id") id: String, @Path("userId") userId: String): Response<ApiResponse<TeamResponse>>

    @POST("teams/{id}/transfer-captain")
    suspend fun transferCaptain(@Path("id") id: String, @Body request: TransferCaptainRequest): Response<ApiResponse<TeamResponse>>

    @POST("teams/{id}/leave")
    suspend fun leaveTeam(@Path("id") id: String): Response<ApiResponse<TeamResponse>>
}

