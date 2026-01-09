package com.example.l4m_esports_mobile.data.repository

import com.example.l4m_esports_mobile.data.model.request.AiRequest
import com.example.l4m_esports_mobile.data.model.response.AiResponse
import com.example.l4m_esports_mobile.data.remote.AiApiService
import com.example.l4m_esports_mobile.util.NetworkErrorHandler
import com.example.l4m_esports_mobile.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepository @Inject constructor(
    private val aiApiService: AiApiService
) {
    suspend fun askAssistant(question: String, context: com.example.l4m_esports_mobile.data.model.request.AiRequestContext?): Result<AiResponse> {
        return try {
            val request = AiRequest(question = question, context = context)
            val response = aiApiService.askAssistant(request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                val errorMessage = response.body()?.message ?: "Impossible de joindre l'assistant."
                Result.Error(NetworkErrorHandler.handleError(Exception(errorMessage)))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }
}

