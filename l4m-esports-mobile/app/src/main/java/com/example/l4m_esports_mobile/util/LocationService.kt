package com.example.l4m_esports_mobile.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

data class LocationResult(
    val latitude: Double?,
    val longitude: Double?,
    val error: String? = null
)

class LocationService(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocation(): LocationResult {
        if (!hasLocationPermission()) {
            return LocationResult(
                latitude = null,
                longitude = null,
                error = "Permissions de localisation non accordées"
            )
        }

        return try {
            val cancellationTokenSource = CancellationTokenSource()
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()

            if (location != null) {
                LocationResult(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            } else {
                LocationResult(
                    latitude = null,
                    longitude = null,
                    error = "Impossible d'obtenir la localisation"
                )
            }
        } catch (e: SecurityException) {
            LocationResult(
                latitude = null,
                longitude = null,
                error = "Permission de localisation refusée"
            )
        } catch (e: Exception) {
            LocationResult(
                latitude = null,
                longitude = null,
                error = "Erreur lors de la récupération de la localisation: ${e.message}"
            )
        }
    }
}

