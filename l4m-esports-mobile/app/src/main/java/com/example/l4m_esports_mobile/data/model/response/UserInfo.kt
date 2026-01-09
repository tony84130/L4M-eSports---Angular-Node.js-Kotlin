package com.example.l4m_esports_mobile.data.model.response

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("_id")
    val id: String,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val avatar: String?,
    val gamertag: String?
)

