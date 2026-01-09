package com.example.l4m_esports_mobile.data.remote

import com.example.l4m_esports_mobile.data.model.request.AiRequest
import com.example.l4m_esports_mobile.data.model.response.ApiResponse
import com.example.l4m_esports_mobile.data.model.response.AiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AiApiService {
    @POST("ai/assist")
    suspend fun askAssistant(@Body request: AiRequest): Response<ApiResponse<AiResponse>>
}

