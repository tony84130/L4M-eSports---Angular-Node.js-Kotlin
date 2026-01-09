package com.example.l4m_esports_mobile.data.remote

import com.example.l4m_esports_mobile.data.model.response.ApiResponse
import com.example.l4m_esports_mobile.data.model.response.NotificationResponse
import com.example.l4m_esports_mobile.data.model.response.UnreadCountResponse
import retrofit2.Response
import retrofit2.http.*

interface NotificationApiService {
    @GET("notifications")
    suspend fun getAllNotifications(
        @Query("read") read: Boolean? = null,
        @Query("type") type: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<ApiResponse<List<NotificationResponse>>>

    @GET("notifications/{id}")
    suspend fun getNotificationById(@Path("id") id: String): Response<ApiResponse<NotificationResponse>>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): Response<ApiResponse<UnreadCountResponse>>

    @PUT("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): Response<ApiResponse<NotificationResponse>>

    @PUT("notifications/read-all")
    suspend fun markAllAsRead(): Response<ApiResponse<Map<String, Int>>>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): Response<ApiResponse<Unit>>
}

