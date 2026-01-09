package com.example.l4m_esports_mobile.data.remote

import com.example.l4m_esports_mobile.data.model.response.ApiResponse
import com.example.l4m_esports_mobile.data.model.response.TwitchUserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TwitchApiService {
    @GET("twitch/user/{username}")
    suspend fun getTwitchUser(@Path("username") username: String): Response<ApiResponse<TwitchUserResponse>>
}

