package com.example.l4m_esports_mobile.ui.screens.matches

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.l4m_esports_mobile.data.model.response.MatchResponse
import com.example.l4m_esports_mobile.ui.viewmodel.MatchViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.MatchUiState
import com.example.l4m_esports_mobile.ui.viewmodel.UserViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.EventViewModel
import com.example.l4m_esports_mobile.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    matchId: String,
    onBack: () -> Unit,
    matchViewModel: MatchViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    eventViewModel: EventViewModel = hiltViewModel()
) {
    val selectedMatch by matchViewModel.selectedMatch.collectAsState()
    val uiState by matchViewModel.uiState.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()
    
    val isAdmin = currentUser?.role == "admin"
    
    var showStatusDialog by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }
    var showValidationDialog by remember { mutableStateOf(false) }
    
    var newStatus by remember { mutableStateOf("") }
    var team1Score by remember { mutableStateOf("0") }
    var team2Score by remember { mutableStateOf("0") }

    LaunchedEffect(matchId) {
        matchViewModel.loadMatchById(matchId)
        userViewModel.loadCurrentUser()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails du match") },
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
                selectedMatch?.let { match ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Informations de l'événement
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Événement",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                    Text(
                                        text = match.event.name ?: "N/A",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                        
                        // Statut du match
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Statut",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                    val statusLabel = when (match.status.lowercase()) {
                                        "upcoming" -> "À venir"
                                        "in_progress" -> "En cours"
                                        "finished" -> "Terminé"
                                        "pending_validation" -> "En attente de validation"
                                        "cancelled" -> "Annulé"
                                        else -> match.status
                                    }
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(statusLabel) },
                                        colors = when (match.status.lowercase()) {
                                            "in_progress" -> AssistChipDefaults.assistChipColors(
                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                            "pending_validation" -> AssistChipDefaults.assistChipColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                                labelColor = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            else -> AssistChipDefaults.assistChipColors()
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Équipes et score
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Équipes",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.Start
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
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                        
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.End
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
                                    
                                    // Afficher le gagnant de manière plus visible si le match est terminé
                                    if (match.status == "finished" && match.winner != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🏆 ",
                                                    style = MaterialTheme.typography.headlineMedium
                                                )
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = "Gagnant",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                    Text(
                                                        text = match.winner.name ?: "N/A",
                                                        style = MaterialTheme.typography.headlineSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    } else if (match.winner != null && match.status != "finished") {
                                        // Afficher le gagnant de manière discrète si le match n'est pas encore terminé
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Gagnant prévu: ${match.winner.name}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Dates
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Dates",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                    Text(
                                        text = "Programmé: ${formatDate(match.scheduledTime)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    match.actualStartTime?.let {
                                        Text(
                                            text = "Début réel: ${formatDate(it)}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    match.actualEndTime?.let {
                                        Text(
                                            text = "Fin réelle: ${formatDate(it)}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Validations
                        if (match.validatedBy != null && match.validatedBy.isNotEmpty()) {
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Validations",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                        match.validatedBy.forEach { validation ->
                                            Text(
                                                text = "${validation.user.gamertag ?: "Utilisateur"} - ${formatDate(validation.validatedAt)}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Actions (Admin uniquement)
                        if (isAdmin) {
                            // Bouton pour mettre à jour le statut
                            item {
                                Button(
                                    onClick = {
                                        newStatus = match.status
                                        showStatusDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Modifier le statut")
                                }
                            }
                            
                            // Bouton pour mettre à jour le score (uniquement si le match est en cours)
                            if (match.status == "in_progress") {
                                item {
                                    Button(
                                        onClick = {
                                            team1Score = match.score.team1.toString()
                                            team2Score = match.score.team2.toString()
                                            showScoreDialog = true
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Modifier le score")
                                    }
                                }
                            }
                            
                            // Bouton pour valider le résultat (si en attente de validation)
                            if (match.status == "pending_validation") {
                                item {
                                    Button(
                                        onClick = { showValidationDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    ) {
                                        Text("Valider le résultat")
                                    }
                                }
                            }
                        }
                    }
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Match introuvable")
                    }
                }
            }
        }
        
        // Dialog pour modifier le statut
        if (showStatusDialog) {
            AlertDialog(
                onDismissRequest = { showStatusDialog = false },
                title = { Text("Modifier le statut") },
                text = {
                    val statuses = listOf("upcoming", "in_progress", "finished", "pending_validation", "cancelled")
                    Column {
                        statuses.forEach { status ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = newStatus == status,
                                    onClick = { newStatus = status }
                                )
                                Text(
                                    text = when (status) {
                                        "upcoming" -> "À venir"
                                        "in_progress" -> "En cours"
                                        "finished" -> "Terminé"
                                        "pending_validation" -> "En attente de validation"
                                        "cancelled" -> "Annulé"
                                        else -> status
                                    },
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            matchViewModel.updateMatchStatus(matchId, newStatus)
                            showStatusDialog = false
                        }
                    ) {
                        Text("Confirmer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStatusDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }
        
        // Dialog pour modifier le score
        if (showScoreDialog) {
            AlertDialog(
                onDismissRequest = { showScoreDialog = false },
                title = { Text("Modifier le score") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = team1Score,
                            onValueChange = { if (it.all { char -> char.isDigit() }) team1Score = it },
                            label = { Text("Score équipe 1") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = team2Score,
                            onValueChange = { if (it.all { char -> char.isDigit() }) team2Score = it },
                            label = { Text("Score équipe 2") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val score1 = team1Score.toIntOrNull() ?: 0
                            val score2 = team2Score.toIntOrNull() ?: 0
                            matchViewModel.updateMatchScore(matchId, score1, score2)
                            showScoreDialog = false
                        }
                    ) {
                        Text("Confirmer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showScoreDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }
        
        // Dialog de confirmation pour la validation
        if (showValidationDialog) {
            AlertDialog(
                onDismissRequest = { showValidationDialog = false },
                title = { Text("Valider le résultat") },
                text = { Text("Êtes-vous sûr de vouloir valider ce résultat ?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            matchViewModel.validateMatchResult(matchId)
                            showValidationDialog = false
                        }
                    ) {
                        Text("Confirmer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showValidationDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }
        
        // Snackbar pour les messages de succès/erreur
        // Recharger l'événement après validation d'un match pour voir les mises à jour (statut completed, bracket, etc.)
        LaunchedEffect(uiState, selectedMatch) {
            if (uiState is MatchUiState.Success && selectedMatch != null) {
                // Recharger l'événement pour voir les mises à jour (statut, bracket, etc.)
                val eventId = selectedMatch?.event?.id
                if (eventId != null) {
                    eventViewModel.loadEventById(eventId)
                    eventViewModel.getEventBracket(eventId)
                }
            }
        }
        
        if (uiState is MatchUiState.Success) {
            Snackbar(
                modifier = Modifier.padding(16.dp)
            ) {
                Text((uiState as MatchUiState.Success).message)
            }
        }
        
        if (uiState is MatchUiState.Error) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Text((uiState as MatchUiState.Error).message)
            }
        }
    }
}

