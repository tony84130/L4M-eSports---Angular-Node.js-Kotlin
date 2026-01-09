package com.example.l4m_esports_mobile.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.l4m_esports_mobile.ui.theme.GamingBlue
import com.example.l4m_esports_mobile.ui.theme.GamingDark
import com.example.l4m_esports_mobile.ui.theme.GamingGreen
import com.example.l4m_esports_mobile.ui.theme.GamingPurple
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    onNavigateToSignIn: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        delay(500)
        showContent = true
        delay(2500) // Afficher pendant 3 secondes au total
        onNavigateToSignIn()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GamingDark,
                        GamingDark.copy(alpha = 0.9f),
                        GamingDark
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            if (showContent) {
                // Logo/Titre animé
                Text(
                    text = "L4M",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = GamingBlue,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "ESPORTS",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = GamingGreen,
                    letterSpacing = 8.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Bienvenue dans l'arène",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Rejoignez les meilleurs joueurs",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Indicateur de chargement
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = GamingPurple,
                    strokeWidth = 4.dp
                )
            }
        }
    }
}

