package com.example.l4m_esports_mobile.data.remote

import com.example.l4m_esports_mobile.data.model.request.CreateGameRequest
import com.example.l4m_esports_mobile.data.model.request.UpdateGameRequest
import com.example.l4m_esports_mobile.data.model.response.ApiResponse
import com.example.l4m_esports_mobile.data.model.response.GameResponse
import retrofit2.Response
import retrofit2.http.*

interface GameApiService {
    @GET("games")
    suspend fun getAllGames(): Response<ApiResponse<List<GameResponse>>>

    @GET("games/{id}")
    suspend fun getGameById(@Path("id") id: String): Response<ApiResponse<GameResponse>>

    @POST("games")
    suspend fun createGame(@Body request: CreateGameRequest): Response<ApiResponse<GameResponse>>

    @PUT("games/{id}")
    suspend fun updateGame(@Path("id") id: String, @Body request: UpdateGameRequest): Response<ApiResponse<GameResponse>>

    @DELETE("games/{id}")
    suspend fun deleteGame(@Path("id") id: String): Response<ApiResponse<Unit>>
}

