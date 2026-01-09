package com.example.l4m_esports_mobile.ui.screens.games

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.l4m_esports_mobile.data.model.response.EventResponse
import com.example.l4m_esports_mobile.util.formatDate

@Composable
fun EventItem(
    event: EventResponse,
    onClick: () -> Unit
) {
    val statusLabel = when (event.status.lowercase()) {
        "draft" -> "À venir"
        "open" -> "Inscriptions ouvertes"
        "registration_closed" -> "Inscriptions fermées"
        "in_progress" -> "En cours"
        "completed" -> "Terminé"
        "cancelled" -> "Annulé"
        else -> event.status
    }
    
    val statusColors = when (event.status.lowercase()) {
        "draft" -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        "open" -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        "registration_closed" -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        "in_progress" -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        "completed" -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        "cancelled" -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            labelColor = MaterialTheme.colorScheme.onErrorContainer
        )
        else -> AssistChipDefaults.assistChipColors()
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            // Légère variation de couleur de fond selon le statut
            containerColor = when (event.status.lowercase()) {
                "open" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                "in_progress" -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                "completed" -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                "cancelled" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(statusLabel) },
                    colors = statusColors
                )
                AssistChip(
                    onClick = {},
                    label = { Text(event.format) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Début: ${formatDate(event.startDate)}",
                style = MaterialTheme.typography.bodySmall
            )
            if (!event.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }
        }
    }
}

