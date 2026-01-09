package com.example.l4m_esports_mobile.data.repository

import com.example.l4m_esports_mobile.data.remote.NotificationApiService
import com.example.l4m_esports_mobile.data.model.response.NotificationResponse
import com.example.l4m_esports_mobile.data.model.response.UnreadCountResponse
import com.example.l4m_esports_mobile.util.NetworkErrorHandler
import com.example.l4m_esports_mobile.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationApiService: NotificationApiService
) {
    suspend fun getAllNotifications(
        read: Boolean? = null,
        type: String? = null,
        limit: Int? = null,
        offset: Int? = null
    ): Result<List<NotificationResponse>> {
        return try {
            val response = notificationApiService.getAllNotifications(read, type, limit, offset)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun getNotificationById(id: String): Result<NotificationResponse> {
        return try {
            val response = notificationApiService.getNotificationById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun getUnreadCount(): Result<Int> {
        return try {
            val response = notificationApiService.getUnreadCount()
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data.count)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun markAsRead(id: String): Result<NotificationResponse> {
        return try {
            val response = notificationApiService.markAsRead(id)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun markAllAsRead(): Result<Unit> {
        return try {
            val response = notificationApiService.markAllAsRead()
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }

    suspend fun deleteNotification(id: String): Result<Unit> {
        return try {
            val response = notificationApiService.deleteNotification(id)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(NetworkErrorHandler.handleError(Exception(response.message())))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.handleError(e))
        }
    }
}

