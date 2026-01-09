package com.example.l4m_esports_mobile.util

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

object NetworkErrorHandler {
    private val gson = Gson()
    
    fun handleError(throwable: Throwable): String {
        return when (throwable) {
            is HttpException -> {
                // Essayer d'extraire le message d'erreur depuis le body de la réponse
                val errorMessage = extractErrorMessage(throwable)
                if (errorMessage != null) {
                    // Traduire les messages d'erreur courants en français
                    return translateErrorMessage(errorMessage)
                }
                
                // Fallback sur les messages par défaut si on ne peut pas extraire le message
                when (throwable.code()) {
                    400 -> "Requête invalide"
                    401 -> "Email ou mot de passe incorrect"
                    403 -> "Accès refusé"
                    404 -> "Ressource non trouvée"
                    500 -> "Erreur serveur. Veuillez réessayer plus tard."
                    else -> "Erreur HTTP ${throwable.code()}"
                }
            }
            is SocketTimeoutException -> "Délai d'attente dépassé. Vérifiez votre connexion."
            is IOException -> "Erreur de connexion. Vérifiez votre réseau."
            else -> throwable.message ?: "Une erreur inattendue s'est produite"
        }
    }
    
    /**
     * Traduit les messages d'erreur courants en français
     */
    private fun translateErrorMessage(message: String): String {
        return when {
            message.contains("Invalid email or password", ignoreCase = true) -> 
                "Email ou mot de passe incorrect"
            message.contains("Invalid email", ignoreCase = true) -> 
                "Email invalide"
            message.contains("Invalid password", ignoreCase = true) -> 
                "Mot de passe incorrect"
            message.contains("User not found", ignoreCase = true) -> 
                "Utilisateur non trouvé"
            message.contains("Email already exists", ignoreCase = true) -> 
                "Cet email est déjà utilisé"
            message.contains("Gamertag already exists", ignoreCase = true) -> 
                "Ce gamertag est déjà utilisé"
            message.contains("Unauthorized", ignoreCase = true) -> 
                "Non autorisé"
            message.contains("Forbidden", ignoreCase = true) -> 
                "Accès refusé"
            message.contains("Not found", ignoreCase = true) -> 
                "Ressource non trouvée"
            else -> message // Retourner le message original s'il n'y a pas de traduction
        }
    }
    
    /**
     * Extrait le message d'erreur depuis le body de la réponse HTTP
     * Format attendu: { "success": false, "error": { "message": "..." } }
     */
    private fun extractErrorMessage(httpException: HttpException): String? {
        return try {
            val errorBody = httpException.response()?.errorBody()?.string()
            if (errorBody != null) {
                val jsonObject = gson.fromJson(errorBody, JsonObject::class.java)
                
                // Vérifier si c'est le format avec "error.message"
                if (jsonObject.has("error")) {
                    val errorObj = jsonObject.getAsJsonObject("error")
                    if (errorObj.has("message")) {
                        return errorObj.get("message").asString
                    }
                }
                
                // Vérifier si c'est le format avec "message" directement
                if (jsonObject.has("message")) {
                    return jsonObject.get("message").asString
                }
            }
            null
        } catch (e: Exception) {
            // Si on ne peut pas parser le JSON, retourner null pour utiliser le fallback
            null
        }
    }
}

