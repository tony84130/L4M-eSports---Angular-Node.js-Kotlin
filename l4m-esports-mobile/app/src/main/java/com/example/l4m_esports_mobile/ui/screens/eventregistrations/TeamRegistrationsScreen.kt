package com.example.l4m_esports_mobile.ui.screens.eventregistrations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.l4m_esports_mobile.data.model.response.EventRegistrationResponse
import com.example.l4m_esports_mobile.ui.viewmodel.EventRegistrationViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.EventRegistrationUiState
import com.example.l4m_esports_mobile.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamRegistrationsScreen(
    teamId: String,
    onBack: () -> Unit,
    onRegistrationClick: (String) -> Unit = {},
    eventRegistrationViewModel: EventRegistrationViewModel = hiltViewModel()
) {
    val registrations by eventRegistrationViewModel.registrations.collectAsState()
    val uiState by eventRegistrationViewModel.uiState.collectAsState()

    LaunchedEffect(teamId) {
        eventRegistrationViewModel.loadRegistrationsByTeam(teamId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inscriptions de l'équipe") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is EventRegistrationUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is EventRegistrationUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = (uiState as EventRegistrationUiState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { eventRegistrationViewModel.loadRegistrationsByTeam(teamId) }) {
                            Text("Réessayer")
                        }
                    }
                }
            }
            else -> {
                if (registrations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucune inscription trouvée",
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
                        items(registrations) { registration ->
                            RegistrationCard(
                                registration = registration,
                                onClick = { onRegistrationClick(registration.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RegistrationCard(
    registration: EventRegistrationResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = registration.event.name ?: "Événement",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            registration.event.startDate?.let { startDate ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Date de début:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDate(startDate),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Statut:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusChip(status = registration.status)
            }
            
            registration.participatingMembers?.let { members ->
                if (members.isNotEmpty()) {
                    Text(
                        text = "Membres participants: ${members.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (label, color) = when (status.uppercase()) {
        "PENDING" -> "En attente" to MaterialTheme.colorScheme.tertiary
        "ACCEPTED" -> "Acceptée" to MaterialTheme.colorScheme.primary
        "REJECTED" -> "Rejetée" to MaterialTheme.colorScheme.error
        "CANCELLED" -> "Annulée" to MaterialTheme.colorScheme.onSurfaceVariant
        else -> status to MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    AssistChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.2f),
            labelColor = color
        )
    )
}

