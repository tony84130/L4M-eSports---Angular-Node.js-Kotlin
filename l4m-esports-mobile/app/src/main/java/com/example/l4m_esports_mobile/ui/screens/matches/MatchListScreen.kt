package com.example.l4m_esports_mobile.ui.screens.matches

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.MatchViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.MatchUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchListScreen(
    eventId: String? = null,
    teamId: String? = null,
    onBack: () -> Unit,
    onMatchClick: (String) -> Unit,
    matchViewModel: MatchViewModel = hiltViewModel()
) {
    val matches by matchViewModel.matches.collectAsState()
    val uiState by matchViewModel.uiState.collectAsState()

    LaunchedEffect(eventId, teamId) {
        when {
            !eventId.isNullOrBlank() && eventId != "null" -> matchViewModel.loadMatchesByEvent(eventId)
            !teamId.isNullOrBlank() && teamId != "null" -> matchViewModel.loadMatchesByTeam(teamId)
            else -> matchViewModel.loadMatches()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when {
                            eventId != null -> "Matchs de l'événement"
                            teamId != null -> "Matchs de l'équipe"
                            else -> "Tous les matchs"
                        }
                    )
                },
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
        when (uiState) {
            is MatchUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is MatchUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (uiState as MatchUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                if (matches.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucun match trouvé",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(matches) { match ->
                            MatchItem(
                                match = match,
                                onClick = { onMatchClick(match.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

