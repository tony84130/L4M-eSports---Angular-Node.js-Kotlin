package com.example.l4m_esports_mobile.data.model.response

import com.google.gson.annotations.SerializedName

data class TwitchUserResponse(
    @SerializedName("display_name")
    val displayName: String,
    val description: String?,
    @SerializedName("profile_image_url")
    val profileImageUrl: String?,
    @SerializedName("view_count")
    val viewCount: Int?,
    val followers: Int?,
    @SerializedName("is_live")
    val isLive: Boolean?
)

