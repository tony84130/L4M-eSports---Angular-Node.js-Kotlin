package com.example.l4m_esports_mobile.data.model.response

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    @SerializedName("_id")
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val read: Boolean,
    @SerializedName("readAt")
    val readAt: String?,
    @SerializedName("relatedEntity")
    val relatedEntity: RelatedEntity?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?
)

data class RelatedEntity(
    @SerializedName("entityType")
    val entityType: String?,
    @SerializedName("entityId")
    val entityId: String?
)

data class UnreadCountResponse(
    val count: Int
)

