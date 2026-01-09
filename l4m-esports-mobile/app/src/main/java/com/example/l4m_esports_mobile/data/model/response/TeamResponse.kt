package com.example.l4m_esports_mobile.data.model.response

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonArray
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class TeamResponse(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val logo: String?,
    @JsonAdapter(GameReferenceDeserializer::class)
    val game: GameInfo, // Game ID et nom (peut être un objet ou une string)
    val description: String?,
    @JsonAdapter(CaptainDeserializer::class)
    val captain: CaptainInfo, // User ID ou objet User
    @JsonAdapter(MembersListDeserializer::class)
    val members: List<MemberInfo>, // User IDs ou objets User
    val status: String,
    val maxMembers: Int,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?
)

data class CaptainInfo(
    val id: String,
    val gamertag: String?
)

data class MemberInfo(
    val id: String,
    val gamertag: String?
)

/**
 * Deserializer pour gérer game qui peut être soit un String (ID) soit un objet Game
 */
class GameReferenceDeserializer : JsonDeserializer<GameInfo> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): GameInfo {
        return when {
            json == null -> GameInfo("", null, null)
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> GameInfo(json.asString, null, null)
            json.isJsonObject -> {
                // Si c'est un objet, extraire l'ID, le nom et les formats
                val obj = json.asJsonObject
                val id = if (obj.has("_id")) obj.get("_id").asString else ""
                val name = if (obj.has("name") && !obj.get("name").isJsonNull) {
                    obj.get("name").asString
                } else null
                val formats = if (obj.has("formats") && !obj.get("formats").isJsonNull) {
                    val formatsArray = obj.getAsJsonArray("formats")
                    formatsArray.mapNotNull { it.asString }.toList()
                } else null
                GameInfo(id, name, formats)
            }
            else -> GameInfo("", null, null)
        }
    }
}

/**
 * Deserializer pour gérer captain qui peut être soit un String (ID) soit un objet User
 */
class CaptainDeserializer : JsonDeserializer<CaptainInfo> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): CaptainInfo {
        return when {
            json == null -> CaptainInfo("", null)
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> CaptainInfo(json.asString, null)
            json.isJsonObject -> {
                val obj = json.asJsonObject
                val id = if (obj.has("_id")) obj.get("_id").asString else ""
                val gamertag = if (obj.has("gamertag") && !obj.get("gamertag").isJsonNull) {
                    obj.get("gamertag").asString
                } else null
                CaptainInfo(id, gamertag)
            }
            else -> CaptainInfo("", null)
        }
    }
}

/**
 * Deserializer pour gérer members qui peut être une liste de Strings (IDs) ou une liste d'objets User
 */
class MembersListDeserializer : JsonDeserializer<List<MemberInfo>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<MemberInfo> {
        if (json == null || !json.isJsonArray) {
            return emptyList()
        }
        
        val array = json.asJsonArray
        return array.mapNotNull { element ->
            when {
                element.isJsonPrimitive && element.asJsonPrimitive.isString -> 
                    MemberInfo(element.asString, null)
                element.isJsonObject -> {
                    val obj = element.asJsonObject
                    val id = if (obj.has("_id")) obj.get("_id").asString else ""
                    val gamertag = if (obj.has("gamertag") && !obj.get("gamertag").isJsonNull) {
                        obj.get("gamertag").asString
                    } else null
                    MemberInfo(id, gamertag)
                }
                else -> null
            }
        }
    }
}

