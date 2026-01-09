package com.example.l4m_esports_mobile.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object MapUtils {
    /**
     * Ouvre Google Maps avec les coordonnées spécifiées
     * @param context Le contexte Android
     * @param latitude La latitude
     * @param longitude La longitude
     * @param address L'adresse optionnelle (pour la recherche par adresse si les coordonnées ne sont pas disponibles)
     */
    fun openGoogleMaps(context: Context, latitude: Double?, longitude: Double?, address: String? = null) {
        val intent: Intent = when {
            latitude != null && longitude != null -> {
                // Ouvrir avec les coordonnées GPS
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
                )
            }
            !address.isNullOrBlank() -> {
                // Ouvrir avec l'adresse si les coordonnées ne sont pas disponibles
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                )
            }
            else -> {
                // Fallback : ouvrir Google Maps sans position spécifique
                Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0"))
            }
        }
        
        intent.setPackage("com.google.android.apps.maps")
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Si Google Maps n'est pas installé, essayer avec un navigateur web
            val webIntent = when {
                latitude != null && longitude != null -> {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps?q=$latitude,$longitude")
                    )
                }
                !address.isNullOrBlank() -> {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(address)}")
                    )
                }
                else -> null
            }
            
            if (webIntent != null) {
                try {
                    context.startActivity(webIntent)
                } catch (e2: Exception) {
                    // Ne rien faire si même le navigateur ne peut pas s'ouvrir
                }
            }
        }
    }
}

