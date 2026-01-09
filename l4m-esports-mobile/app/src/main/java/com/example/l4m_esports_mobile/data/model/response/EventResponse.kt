package com.example.l4m_esports_mobile.data.model.response

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class EventResponse(
    @SerializedName("_id")
    val id: String,
    val name: String,
    @JsonAdapter(GameReferenceDeserializer::class)
    val game: GameInfo,
    @SerializedName("startDate")
    val startDate: String,
    @SerializedName("endDate")
    val endDate: String,
    @SerializedName("registrationStartDate")
    val registrationStartDate: String,
    @SerializedName("registrationEndDate")
    val registrationEndDate: String,
    val format: String,
    val rules: String?,
    val description: String?,
    val status: String,
    val location: LocationInfo?,
    @SerializedName("maxTeams")
    val maxTeams: Int,
    @JsonAdapter(UserReferenceDeserializer::class)
    @SerializedName("createdBy")
    val createdBy: UserInfo?,
    val bracket: BracketInfo?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?
)

data class LocationInfo(
    val type: String,
    val address: String?,
    val coordinates: CoordinatesInfo?
)

data class CoordinatesInfo(
    val type: String,
    val coordinates: List<Double>
)

data class BracketInfo(
    val rounds: List<RoundInfo>?
)

data class RoundInfo(
    @SerializedName("roundNumber")
    val roundNumber: Int,
    val matches: List<MatchInfo>?
)

data class MatchInfo(
    @JsonAdapter(TeamInfoDeserializer::class)
    val team1: TeamInfo?,
    @JsonAdapter(TeamInfoDeserializer::class)
    val team2: TeamInfo?,
    @JsonAdapter(TeamInfoDeserializer::class)
    val winner: TeamInfo?
)

/**
 * Deserializer pour gérer TeamInfo qui peut être soit un String (ID) soit un objet Team
 */
class TeamInfoDeserializer : JsonDeserializer<TeamInfo> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): TeamInfo? {
        return when {
            json == null -> null
            json.isJsonNull -> null
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                // Si c'est juste un ID string, créer un TeamInfo avec seulement l'ID
                TeamInfo(json.asString, null, null, null)
            }
            json.isJsonObject -> {
                // Si c'est un objet, extraire tous les champs
                val obj = json.asJsonObject
                val id = if (obj.has("_id")) obj.get("_id").asString else ""
                val name = if (obj.has("name") && !obj.get("name").isJsonNull) {
                    obj.get("name").asString
                } else null
                val logo = if (obj.has("logo") && !obj.get("logo").isJsonNull) {
                    obj.get("logo").asString
                } else null
                
                // Gérer le champ captain (optionnel, présent dans les matchs)
                val captain = if (obj.has("captain") && !obj.get("captain").isJsonNull) {
                    val captainJson = obj.get("captain")
                    when {
                        captainJson.isJsonPrimitive && captainJson.asJsonPrimitive.isString -> {
                            // Si c'est juste un ID string
                            TeamCaptainInfo(captainJson.asString, null)
                        }
                        captainJson.isJsonObject -> {
                            // Si c'est un objet User
                            val captainObj = captainJson.asJsonObject
                            val captainId = if (captainObj.has("_id")) {
                                captainObj.get("_id").asString
                            } else ""
                            val captainGamertag = if (captainObj.has("gamertag") && !captainObj.get("gamertag").isJsonNull) {
                                captainObj.get("gamertag").asString
                            } else null
                            TeamCaptainInfo(captainId, captainGamertag)
                        }
                        else -> null
                    }
                } else null
                
                TeamInfo(id, name, logo, captain)
            }
            else -> null
        }
    }
}

/**
 * Deserializer pour gérer createdBy qui peut être soit un String (ID) soit un objet User
 */
class UserReferenceDeserializer : JsonDeserializer<UserInfo> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): UserInfo? {
        return when {
            json == null -> null
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                // Si c'est juste un ID string, créer un UserInfo avec seulement l'ID
                UserInfo(json.asString, null, null, null, null, null)
            }
            json.isJsonObject -> {
                // Si c'est un objet, extraire tous les champs
                val obj = json.asJsonObject
                val id = if (obj.has("_id")) obj.get("_id").asString else ""
                val firstName = if (obj.has("firstName") && !obj.get("firstName").isJsonNull) {
                    obj.get("firstName").asString
                } else null
                val lastName = if (obj.has("lastName") && !obj.get("lastName").isJsonNull) {
                    obj.get("lastName").asString
                } else null
                val email = if (obj.has("email") && !obj.get("email").isJsonNull) {
                    obj.get("email").asString
                } else null
                val avatar = if (obj.has("avatar") && !obj.get("avatar").isJsonNull) {
                    obj.get("avatar").asString
                } else null
                val gamertag = if (obj.has("gamertag") && !obj.get("gamertag").isJsonNull) {
                    obj.get("gamertag").asString
                } else null
                UserInfo(id, firstName, lastName, email, avatar, gamertag)
            }
            else -> null
        }
    }
}

