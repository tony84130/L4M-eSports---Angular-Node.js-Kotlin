package com.example.l4m_esports_mobile.ui.screens.matches

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.l4m_esports_mobile.data.model.response.MatchResponse
import com.example.l4m_esports_mobile.util.formatDate

@Composable
fun MatchItem(
    match: MatchResponse,
    onClick: () -> Unit
) {
    val statusLabel = when (match.status.lowercase()) {
        "upcoming" -> "À venir"
        "in_progress" -> "En cours"
        "finished" -> "Terminé"
        "pending_validation" -> "En attente de validation"
        "cancelled" -> "Annulé"
        else -> match.status
    }
    
    val statusColors = when (match.status.lowercase()) {
        "upcoming" -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        "in_progress" -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        "finished" -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        "pending_validation" -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            labelColor = MaterialTheme.colorScheme.onErrorContainer
        )
        "cancelled" -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
            labelColor = MaterialTheme.colorScheme.onErrorContainer
        )
        else -> AssistChipDefaults.assistChipColors()
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (match.status.lowercase()) {
                "in_progress" -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                "finished" -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                "pending_validation" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                "cancelled" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Match ${match.bracketPosition?.round?.let { "Round $it" } ?: ""}",
                    style = MaterialTheme.typography.titleMedium
                )
                AssistChip(
                    onClick = {},
                    label = { Text(statusLabel) },
                    colors = statusColors
                )
            }
            
            // Affichage des équipes et du score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = match.teams.getOrNull(0)?.name ?: "Équipe 1",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (match.winner?.id == match.teams.getOrNull(0)?.id) {
                            androidx.compose.ui.text.font.FontWeight.Bold
                        } else {
                            androidx.compose.ui.text.font.FontWeight.Normal
                        },
                        color = if (match.winner?.id == match.teams.getOrNull(0)?.id) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                
                Text(
                    text = "${match.score.team1} - ${match.score.team2}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = match.teams.getOrNull(1)?.name ?: "Équipe 2",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (match.winner?.id == match.teams.getOrNull(1)?.id) {
                            androidx.compose.ui.text.font.FontWeight.Bold
                        } else {
                            androidx.compose.ui.text.font.FontWeight.Normal
                        },
                        color = if (match.winner?.id == match.teams.getOrNull(1)?.id) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
            
            // Afficher le gagnant si le match est terminé
            if (match.status == "finished" && match.winner != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "🏆 Gagnant: ${match.winner.name}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            
            Text(
                text = "Programmé: ${formatDate(match.scheduledTime)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

