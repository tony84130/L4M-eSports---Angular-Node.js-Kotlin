package com.example.l4m_esports_mobile.data.model.response

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class TeamRequestResponse(
    @SerializedName("_id")
    val id: String,
    @JsonAdapter(TeamReferenceDeserializer::class)
    val team: String, // Team ID (peut être un objet ou une string)
    @JsonAdapter(UserRequestDeserializer::class)
    val user: UserRequestInfo, // User ID ou objet User
    val status: String,
    val message: String?,
    @JsonAdapter(UserRequestDeserializer::class)
    @SerializedName("reviewedBy")
    val reviewedBy: UserRequestInfo?, // User ID ou objet User
    @SerializedName("reviewedAt")
    val reviewedAt: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?
)

data class UserRequestInfo(
    val id: String,
    val gamertag: String?
)

/**
 * Deserializer pour gérer team qui peut être soit un String (ID) soit un objet Team
 */
class TeamReferenceDeserializer : JsonDeserializer<String> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): String {
        return when {
            json == null -> ""
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> json.asString
            json.isJsonObject -> {
                val obj = json.asJsonObject
                if (obj.has("_id")) {
                    obj.get("_id").asString
                } else {
                    ""
                }
            }
            else -> ""
        }
    }
}

/**
 * Deserializer pour gérer user qui peut être soit un String (ID) soit un objet User
 */
class UserRequestDeserializer : JsonDeserializer<UserRequestInfo> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): UserRequestInfo {
        return when {
            json == null -> UserRequestInfo("", null)
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> UserRequestInfo(json.asString, null)
            json.isJsonObject -> {
                val obj = json.asJsonObject
                val id = if (obj.has("_id")) obj.get("_id").asString else ""
                val gamertag = if (obj.has("gamertag") && !obj.get("gamertag").isJsonNull) {
                    obj.get("gamertag").asString
                } else null
                UserRequestInfo(id, gamertag)
            }
            else -> UserRequestInfo("", null)
        }
    }
}

