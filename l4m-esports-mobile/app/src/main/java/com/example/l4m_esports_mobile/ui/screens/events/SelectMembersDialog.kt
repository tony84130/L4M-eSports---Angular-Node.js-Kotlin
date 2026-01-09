package com.example.l4m_esports_mobile.ui.screens.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.l4m_esports_mobile.data.model.response.MemberInfo

/**
 * Convertit le format d'événement en nombre de joueurs requis
 */
fun getRequiredPlayersFromFormat(format: String): Int {
    return when (format) {
        "1v1" -> 1
        "2v2" -> 2
        "3v3" -> 3
        "4v4" -> 4
        "5v5" -> 5
        "BATTLE_ROYALE" -> 100
        else -> 5
    }
}

@Composable
fun SelectMembersDialog(
    eventFormat: String,
    teamMembers: List<MemberInfo>,
    captainId: String,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val requiredPlayers = getRequiredPlayersFromFormat(eventFormat)
    // Le capitaine est inclus automatiquement, donc on doit sélectionner (requiredPlayers - 1) membres
    val membersToSelect = requiredPlayers - 1
    
    // Filtrer les membres (exclure le capitaine)
    val availableMembers = teamMembers.filter { it.id != captainId }
    
    var selectedMemberIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Sélectionner les membres")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Information sur le format
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Format: $eventFormat",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Le capitaine participe automatiquement.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Sélectionnez $membersToSelect membre(s) supplémentaire(s).",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Compteur
                Text(
                    text = "Sélectionnés: ${selectedMemberIds.size}/$membersToSelect",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedMemberIds.size == membersToSelect) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                
                // Liste des membres
                if (availableMembers.isEmpty()) {
                    Text(
                        text = "Aucun membre disponible (le capitaine est inclus automatiquement)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableMembers) { member ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedMemberIds.contains(member.id),
                                    onCheckedChange = { checked ->
                                        selectedMemberIds = if (checked) {
                                            if (selectedMemberIds.size < membersToSelect) {
                                                selectedMemberIds + member.id
                                            } else {
                                                selectedMemberIds
                                            }
                                        } else {
                                            selectedMemberIds - member.id
                                        }
                                    },
                                    enabled = selectedMemberIds.contains(member.id) || selectedMemberIds.size < membersToSelect
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = member.gamertag ?: "Membre ${member.id.take(8)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedMemberIds.toList())
                },
                enabled = selectedMemberIds.size == membersToSelect
            ) {
                Text("S'inscrire")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

