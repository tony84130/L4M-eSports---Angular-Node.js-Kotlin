package com.example.l4m_esports_mobile.ui.screens.events

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.l4m_esports_mobile.util.LocationService
import com.example.l4m_esports_mobile.util.LocationResult
import kotlinx.coroutines.launch

@Composable
fun rememberLocationPermissionHandler(
    onLocationObtained: (LocationResult) -> Unit
): LocationPermissionHandler {
    val context = LocalContext.current
    val locationService = remember { LocationService(context) }
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (granted) {
            scope.launch {
                val result = locationService.getCurrentLocation()
                onLocationObtained(result)
            }
        } else {
            onLocationObtained(
                LocationResult(
                    latitude = null,
                    longitude = null,
                    error = "Permissions de localisation refusées"
                )
            )
        }
    }

    return remember {
        object : LocationPermissionHandler {
            override fun requestLocation() {
                if (locationService.hasLocationPermission()) {
                    scope.launch {
                        val result = locationService.getCurrentLocation()
                        onLocationObtained(result)
                    }
                } else {
                    launcher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }

            override fun hasPermission(): Boolean = locationService.hasLocationPermission()
        }
    }
}

interface LocationPermissionHandler {
    fun requestLocation()
    fun hasPermission(): Boolean
}

