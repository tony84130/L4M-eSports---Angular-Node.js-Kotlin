package com.example.l4m_esports_mobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GamingBlue,
    onPrimary = GamingDark,
    primaryContainer = GamingPurple,
    onPrimaryContainer = Color.White,
    
    secondary = GamingPurple,
    onSecondary = Color.White,
    secondaryContainer = GamingPink,
    onSecondaryContainer = Color.White,
    
    tertiary = GamingGreen,
    onTertiary = GamingDark,
    tertiaryContainer = GamingOrange,
    onTertiaryContainer = Color.White,
    
    background = GamingDark,
    onBackground = Color.White,
    surface = GamingDarkSurface,
    onSurface = Color.White,
    surfaceVariant = GamingDarkSecondary,
    onSurfaceVariant = GamingBlue,
    
    error = GamingPink,
    onError = Color.White,
    errorContainer = GamingOrange,
    onErrorContainer = Color.White,
    
    outline = GamingBlue.copy(alpha = 0.5f),
    outlineVariant = GamingPurple.copy(alpha = 0.3f)
)

private val LightColorScheme = lightColorScheme(
    primary = GamingBlueLight,
    onPrimary = Color.White,
    primaryContainer = GamingPurpleLight,
    onPrimaryContainer = Color.White,
    
    secondary = GamingPurpleLight,
    onSecondary = Color.White,
    secondaryContainer = GamingPinkLight,
    onSecondaryContainer = Color.White,
    
    tertiary = GamingGreenLight,
    onTertiary = Color.White,
    tertiaryContainer = GamingOrangeLight,
    onTertiaryContainer = Color.White,
    
    background = GamingLight,
    onBackground = GamingDark,
    surface = GamingLightSurface,
    onSurface = GamingDark,
    surfaceVariant = GamingLightSecondary,
    onSurfaceVariant = GamingBlueLight,
    
    error = GamingPinkLight,
    onError = Color.White,
    errorContainer = GamingOrangeLight,
    onErrorContainer = Color.White,
    
    outline = GamingBlueLight.copy(alpha = 0.5f),
    outlineVariant = GamingPurpleLight.copy(alpha = 0.3f)
)

@Composable
fun L4mesportsmobileTheme(
    darkTheme: Boolean = true, // Mode sombre pour l'ambiance gaming (mais plus clair)
    // Dynamic color désactivé pour garder notre thème gaming personnalisé
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Désactiver dynamicColor pour garder notre thème gaming
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Utiliser une couleur plus claire pour la barre de statut pour meilleure visibilité des icônes système
            // En mode sombre, utiliser une couleur plus claire que le fond principal
            val statusBarColor = if (darkTheme) {
                // Utiliser une couleur intermédiaire entre GamingDarkSurface et GamingDarkSecondary
                Color(0xFF2A3547) // Plus clair que le fond pour meilleure visibilité
            } else {
                colorScheme.surface
            }
            window.statusBarColor = statusBarColor.toArgb()
            // Configurer les icônes de la barre de statut : claires (blanches) en mode sombre
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme // false en mode sombre = icônes claires/blanches
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}