package com.example.l4m_esports_mobile.data.model.response

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class MatchResponse(
    @SerializedName("_id")
    val id: String,
    @JsonAdapter(EventInfoDeserializer::class)
    val event: EventInfo,
    @JsonAdapter(TeamInfoListDeserializer::class)
    val teams: List<TeamInfo>,
    @SerializedName("scheduledTime")
    val scheduledTime: String,
    @SerializedName("actualStartTime")
    val actualStartTime: String?,
    @SerializedName("actualEndTime")
    val actualEndTime: String?,
    val status: String,
    val score: ScoreInfo,
    @SerializedName("bracketPosition")
    val bracketPosition: BracketPositionInfo?,
    @JsonAdapter(TeamInfoDeserializer::class)
    val winner: TeamInfo?,
    @JsonAdapter(ValidationInfoListDeserializer::class)
    @SerializedName("validatedBy")
    val validatedBy: List<ValidationInfo>?,
    val notes: String?
)

// EventInfo est maintenant défini dans CommonInfo.kt

data class ScoreInfo(
    val team1: Int,
    val team2: Int
)

data class BracketPositionInfo(
    val round: Int,
    @SerializedName("matchNumber")
    val matchNumber: Int,
    @SerializedName("bracketSide")
    val bracketSide: String?
)

data class ValidationInfo(
    @JsonAdapter(UserReferenceDeserializer::class)
    val user: UserInfo,
    @SerializedName("validatedAt")
    val validatedAt: String
)

data class MatchDataResponse(
    val match: MatchResponse
)

data class MatchesDataResponse(
    val matches: List<MatchResponse>
)

// Désérialiseur pour EventInfo (peut être un ID string ou un objet complet)
class EventInfoDeserializer : JsonDeserializer<EventInfo> {
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

/**
 * Deserializer pour gérer une liste de TeamInfo
 */
class TeamInfoListDeserializer : JsonDeserializer<List<TeamInfo>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<TeamInfo> {
        return when {
            json == null -> emptyList()
            json.isJsonNull -> emptyList()
            json.isJsonArray -> {
                val array = json.asJsonArray
                array.mapNotNull { element ->
                    TeamInfoDeserializer().deserialize(element, null, context)
                }
            }
            else -> emptyList()
        }
    }
}

/**
 * Deserializer pour gérer une liste de ValidationInfo
 */
class ValidationInfoListDeserializer : JsonDeserializer<List<ValidationInfo>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<ValidationInfo> {
        return when {
            json == null -> emptyList()
            json.isJsonNull -> emptyList()
            json.isJsonArray -> {
                val array = json.asJsonArray
                array.mapNotNull { element ->
                    if (element.isJsonObject) {
                        val obj = element.asJsonObject
                        val userJson = obj.get("user")
                        val user = UserReferenceDeserializer().deserialize(userJson, null, context)
                            ?: return@mapNotNull null
                        val validatedAt = if (obj.has("validatedAt") && !obj.get("validatedAt").isJsonNull) {
                            obj.get("validatedAt").asString
                        } else ""
                        ValidationInfo(user, validatedAt)
                    } else null
                }
            }
            else -> emptyList()
        }
    }
}

