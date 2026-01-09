package com.example.l4m_esports_mobile.ui.screens.teams

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.l4m_esports_mobile.data.model.response.TeamResponse
import com.example.l4m_esports_mobile.ui.viewmodel.TeamViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.TeamUiState
import com.example.l4m_esports_mobile.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsListScreen(
    onTeamClick: (String) -> Unit,
    onCreateTeam: () -> Unit,
    teamViewModel: TeamViewModel = hiltViewModel(),
    userViewModel: com.example.l4m_esports_mobile.ui.viewmodel.UserViewModel = hiltViewModel()
) {
    val teams by teamViewModel.teams.collectAsState()
    val uiState by teamViewModel.uiState.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        teamViewModel.loadTeams()
        userViewModel.loadCurrentUser()
    }

    val canCreateTeam = currentUser?.role == "member" || currentUser?.role == "captain"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Équipes") }
            )
        },
        floatingActionButton = {
            if (canCreateTeam) {
                FloatingActionButton(onClick = onCreateTeam) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Créer une équipe"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState is TeamUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                teams.isEmpty() -> {
                    Text(
                        text = "Aucune équipe disponible",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(teams) { team ->
                            TeamItem(
                                team = team,
                                currentUserId = currentUser?.id,
                                onClick = { onTeamClick(team.id) }
                            )
                        }
                    }
                }
            }

            if (uiState is TeamUiState.Error) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text((uiState as TeamUiState.Error).message)
                }
            }
        }
    }
}

@Composable
fun TeamItem(
    team: TeamResponse,
    currentUserId: String?,
    onClick: () -> Unit
) {
    // Vérifier si l'utilisateur est membre de l'équipe
    val isMember = currentUserId != null && team.members.any { it.id == currentUserId }
    val isCaptain = currentUserId != null && team.captain?.id != null && team.captain.id == currentUserId
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isMember) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // En-tête avec nom et indicateur de membre
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = team.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                // Indicateur visuel si l'utilisateur est membre
                if (isMember) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCaptain) Icons.Default.Person else Icons.Default.CheckCircle,
                            contentDescription = if (isCaptain) "Capitaine" else "Membre",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        AssistChip(
                            onClick = {},
                            label = { 
                                Text(
                                    text = if (isCaptain) "Capitaine" else "Membre",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
            if (!team.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = team.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(team.status) }
                )
                // Afficher le jeu si disponible
                if (!team.game.name.isNullOrBlank()) {
                    AssistChip(
                        onClick = {},
                        label = { 
                            Text(
                                text = team.game.name,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
                Text(
                    text = "${team.members.size}/${team.maxMembers} membres",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

