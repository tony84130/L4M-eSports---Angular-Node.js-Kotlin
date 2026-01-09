package com.example.l4m_esports_mobile.ui.screens.teamrequests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.l4m_esports_mobile.data.model.response.TeamRequestResponse
import com.example.l4m_esports_mobile.ui.viewmodel.TeamRequestViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.TeamRequestUiState
import com.example.l4m_esports_mobile.ui.viewmodel.TeamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamRequestsForTeamScreen(
    teamId: String,
    onBack: () -> Unit,
    teamRequestViewModel: TeamRequestViewModel = hiltViewModel(),
    teamViewModel: TeamViewModel = hiltViewModel()
) {
    val teamRequests by teamRequestViewModel.teamRequests.collectAsState()
    val uiState by teamRequestViewModel.uiState.collectAsState()
    val selectedTeam by teamViewModel.selectedTeam.collectAsState()

    LaunchedEffect(teamId) {
        teamViewModel.loadTeamById(teamId)
        teamRequestViewModel.loadTeamRequestsByTeam(teamId)
    }

    // Recharger les demandes après acceptation/rejet
    LaunchedEffect(uiState) {
        if (uiState is TeamRequestUiState.Success) {
            teamRequestViewModel.loadTeamRequestsByTeam(teamId)
            // Recharger l'équipe pour mettre à jour le nombre de membres
            teamViewModel.loadTeamById(teamId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Demandes pour ${selectedTeam?.name ?: "l'équipe"}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState is TeamRequestUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                teamRequests.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Aucune demande",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Il n'y a pas de demande en attente pour cette équipe",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(teamRequests) { request ->
                            TeamRequestForTeamItem(
                                request = request,
                                onAccept = { 
                                    teamRequestViewModel.acceptTeamRequest(request.id, teamId)
                                },
                                onReject = { 
                                    teamRequestViewModel.rejectTeamRequest(request.id, teamId)
                                }
                            )
                        }
                    }
                }
            }

            if (uiState is TeamRequestUiState.Error) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text((uiState as TeamRequestUiState.Error).message)
                }
            }

            if (uiState is TeamRequestUiState.Success) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text((uiState as TeamRequestUiState.Success).message)
                }
            }
        }
    }
}

@Composable
fun TeamRequestForTeamItem(
    request: TeamRequestResponse,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // En-tête avec statut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Demande de ${request.user.gamertag ?: request.user.id.take(8)}",
                    style = MaterialTheme.typography.titleMedium
                )
                AssistChip(
                    onClick = {},
                    label = { 
                        Text(
                            text = request.status,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (request.status) {
                            "pending" -> MaterialTheme.colorScheme.primaryContainer
                            "accepted" -> MaterialTheme.colorScheme.tertiaryContainer
                            "rejected" -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                )
            }

            // Message si présent
            if (!request.message.isNullOrBlank()) {
                Text(
                    text = request.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Date de création
            if (!request.createdAt.isNullOrBlank()) {
                Text(
                    text = "Demandé le: ${com.example.l4m_esports_mobile.util.formatDate(request.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Boutons d'action (seulement pour les demandes en attente)
            if (request.status == "pending") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Accepter")
                    }
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Rejeter")
                    }
                }
            } else if (request.status == "accepted") {
                Text(
                    text = "✓ Demande acceptée",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (request.status == "rejected") {
                Text(
                    text = "✗ Demande rejetée",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

