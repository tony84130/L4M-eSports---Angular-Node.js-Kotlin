package com.example.l4m_esports_mobile.data.model.request

data class AiRequest(
    val question: String,
    val context: AiRequestContext? = null
)

data class AiRequestContext(
    val page: String? = null,
    val role: String? = null,
    val extra: Map<String, Any>? = null
)

