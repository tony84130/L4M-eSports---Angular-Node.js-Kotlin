package com.example.l4m_esports_mobile.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.*

/**
 * Formate une date ISO 8601 en format français jj-mm-aaaa HH:mm
 * @param dateString La date au format ISO 8601 (ex: "2024-01-15T10:30:00.000Z")
 * @return La date formatée en "jj-mm-aaaa HH:mm" ou la chaîne originale si le parsing échoue
 */
fun formatDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return ""
    
    return try {
        // Parser la date ISO 8601
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        
        if (date != null) {
            // Formater en jj-mm-aaaa HH:mm avec le fuseau horaire de Montréal
            val outputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.FRENCH)
            outputFormat.timeZone = TimeZone.getTimeZone("America/Montreal")
            outputFormat.format(date)
        } else {
            dateString
        }
    } catch (e: Exception) {
        // Si le parsing échoue, essayer un format alternatif
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)
            
            if (date != null) {
                val outputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.FRENCH)
                outputFormat.timeZone = TimeZone.getTimeZone("America/Montreal")
                outputFormat.format(date)
            } else {
                dateString
            }
        } catch (e2: Exception) {
            // Si tout échoue, retourner la chaîne originale
            dateString
        }
    }
}

/**
 * Convertit une date du format français "jj-mm-aaaa HH:mm" en format ISO 8601
 * @param dateString La date au format "dd-MM-yyyy HH:mm" (ex: "25-12-2024 14:00")
 * @return La date au format ISO 8601 (ex: "2024-12-25T14:00:00.000Z") ou null si le parsing échoue
 */
fun parseDateToISO(dateString: String?): String? {
    if (dateString.isNullOrBlank()) return null
    
    return try {
        // Parser la date au format français
        val inputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.FRENCH)
        val date = inputFormat.parse(dateString)
        
        if (date != null) {
            // Formater en ISO 8601
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            outputFormat.timeZone = TimeZone.getTimeZone("UTC")
            outputFormat.format(date)
        } else {
            null
        }
    } catch (e: Exception) {
        // Si le parsing échoue, retourner null
        null
    }
}

/**
 * Convertit un LocalDateTime en format ISO 8601
 * @param dateTime Le LocalDateTime à convertir
 * @return La date au format ISO 8601 (ex: "2024-12-25T14:00:00.000Z")
 */
@RequiresApi(Build.VERSION_CODES.O)
fun localDateTimeToISO(dateTime: java.time.LocalDateTime): String {
    // Convertir depuis le fuseau horaire de Montréal vers UTC
    val montrealZone = java.time.ZoneId.of("America/Montreal")
    val zonedDateTime = dateTime.atZone(montrealZone)
    val instant = zonedDateTime.toInstant()
    val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .withZone(java.time.ZoneOffset.UTC)
    return formatter.format(instant)
}

