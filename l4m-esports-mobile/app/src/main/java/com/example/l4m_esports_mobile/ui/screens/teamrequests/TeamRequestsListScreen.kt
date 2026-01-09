package com.example.l4m_esports_mobile.ui.screens.teamrequests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.l4m_esports_mobile.data.model.response.TeamRequestResponse
import com.example.l4m_esports_mobile.ui.viewmodel.TeamRequestViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.TeamRequestUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamRequestsListScreen(
    viewModel: TeamRequestViewModel = hiltViewModel()
) {
    val teamRequests by viewModel.teamRequests.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadTeamRequests()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Demandes d'équipe") }
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
                    Text(
                        text = "Aucune demande disponible",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(teamRequests) { request ->
                            TeamRequestItem(
                                request = request,
                                onAccept = { viewModel.acceptTeamRequest(request.id) },
                                onReject = { viewModel.rejectTeamRequest(request.id) },
                                onDelete = { viewModel.deleteTeamRequest(request.id) }
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
        }
    }
}

@Composable
fun TeamRequestItem(
    request: TeamRequestResponse,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Équipe: ${request.team}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Utilisateur: ${request.user.gamertag ?: request.user.id.take(8)}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (!request.message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = request.message,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            AssistChip(
                onClick = {},
                label = { Text(request.status) }
            )
            if (request.status == "pending") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onAccept) {
                        Text("Accepter")
                    }
                    OutlinedButton(onClick = onReject) {
                        Text("Rejeter")
                    }
                    TextButton(onClick = onDelete) {
                        Text("Supprimer")
                    }
                }
            }
        }
    }
}

