package com.example.l4m_esports_mobile.data.model.response

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class EventRegistrationResponse(
    @SerializedName("_id")
    val id: String,
    @JsonAdapter(EventReferenceDeserializer::class)
    val event: EventInfo,
    val team: TeamInfo,
    val status: String,
    @SerializedName("registeredBy")
    val registeredBy: UserInfo?,
    @SerializedName("participatingMembers")
    val participatingMembers: List<UserInfo>?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?
)

// EventInfo est maintenant défini dans CommonInfo.kt

/**
 * Deserializer pour gérer event qui peut être soit un String (ID) soit un objet Event
 */
class EventReferenceDeserializer : JsonDeserializer<EventInfo> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): EventInfo {
        return when {
            json == null -> EventInfo("", null, null, null, null, null, null)
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                EventInfo(json.asString, null, null, null, null, null, null)
            }
            json.isJsonObject -> {
                val obj = json.asJsonObject
                val id = if (obj.has("_id")) obj.get("_id").asString else ""
                val name = if (obj.has("name") && !obj.get("name").isJsonNull) {
                    obj.get("name").asString
                } else null
                val startDate = if (obj.has("startDate") && !obj.get("startDate").isJsonNull) {
                    obj.get("startDate").asString
                } else null
                val endDate = if (obj.has("endDate") && !obj.get("endDate").isJsonNull) {
                    obj.get("endDate").asString
                } else null
                val game = if (obj.has("game") && !obj.get("game").isJsonNull) {
                    val gameElement = obj.get("game")
                    when {
                        gameElement.isJsonPrimitive && gameElement.asJsonPrimitive.isString -> {
                            GameInfo(gameElement.asString, null, null)
                        }
                        gameElement.isJsonObject -> {
                            val gameObj = gameElement.asJsonObject
                            val gameId = if (gameObj.has("_id")) gameObj.get("_id").asString else ""
                            val gameName = if (gameObj.has("name") && !gameObj.get("name").isJsonNull) {
                                gameObj.get("name").asString
                            } else null
                            val formats = if (gameObj.has("formats") && !gameObj.get("formats").isJsonNull) {
                                val formatsArray = gameObj.getAsJsonArray("formats")
                                formatsArray.mapNotNull { it.asString }.toList()
                            } else null
                            GameInfo(gameId, gameName, formats)
                        }
                        else -> null
                    }
                } else null
                val status = if (obj.has("status") && !obj.get("status").isJsonNull) {
                    obj.get("status").asString
                } else null
                val format = if (obj.has("format") && !obj.get("format").isJsonNull) {
                    obj.get("format").asString
                } else null
                EventInfo(id, name, startDate, endDate, game, status, format)
            }
            else -> EventInfo("", null, null, null, null, null, null)
        }
    }
}

