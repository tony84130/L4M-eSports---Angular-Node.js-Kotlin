package com.example.l4m_esports_mobile.ui.screens.eventregistrations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.EventRegistrationViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.EventRegistrationUiState
import com.example.l4m_esports_mobile.ui.viewmodel.UserViewModel
import com.example.l4m_esports_mobile.util.formatDate
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventRegistrationDetailScreen(
    registrationId: String,
    onBack: () -> Unit,
    onRegistrationDeleted: () -> Unit = {},
    eventRegistrationViewModel: EventRegistrationViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val selectedRegistration by eventRegistrationViewModel.selectedRegistration.collectAsState()
    val uiState by eventRegistrationViewModel.uiState.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()
    
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    val isCaptain = selectedRegistration?.registeredBy?.id == currentUser?.id
    val canCancel = isCaptain && selectedRegistration?.status == "PENDING"
    
    LaunchedEffect(registrationId) {
        eventRegistrationViewModel.loadRegistrationById(registrationId)
        userViewModel.loadCurrentUser()
    }
    
    LaunchedEffect(uiState) {
        if (uiState is EventRegistrationUiState.Success) {
            val message = (uiState as EventRegistrationUiState.Success).message
            if (message.contains("annulée") || message.contains("supprimée") || message.contains("CANCELLED")) {
                // Attendre un peu pour que l'utilisateur voie le message
                kotlinx.coroutines.delay(500)
                onRegistrationDeleted()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails de l'inscription") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (canCancel) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Annuler l'inscription")
                        }
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
                        Button(onClick = { eventRegistrationViewModel.loadRegistrationById(registrationId) }) {
                            Text("Réessayer")
                        }
                    }
                }
            }
            else -> {
                selectedRegistration?.let { registration ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // Informations sur l'événement
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Événement",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Text(
                                        text = registration.event.name ?: "Événement",
                                        style = MaterialTheme.typography.titleLarge
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
                                    
                                    registration.event.endDate?.let { endDate ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Date de fin:",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = formatDate(endDate),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                    
                                    registration.event.format?.let { format ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Format:",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = format,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        item {
                            // Informations sur l'équipe
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Équipe",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Text(
                                        text = registration.team.name ?: "Équipe",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                        }
                        
                        item {
                            // Statut
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Statut",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    StatusChip(status = registration.status)
                                }
                            }
                        }
                        
                        item {
                            // Membres participants
                            if (!registration.participatingMembers.isNullOrEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "Membres participants (${registration.participatingMembers.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        registration.participatingMembers.forEach { member ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = member.gamertag ?: "${member.firstName} ${member.lastName}",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        }
                                    }
                                }
                            }
                        }
                        
                        item {
                            // Informations sur l'inscription
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Informations",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    registration.registeredBy?.let { registeredBy ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Inscrit par:",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = registeredBy.gamertag ?: "${registeredBy.firstName} ${registeredBy.lastName}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                    
                                    registration.createdAt?.let { createdAt ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Créée le:",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = formatDate(createdAt),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
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
                        Text(
                            text = "Inscription introuvable",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Dialog de confirmation pour annuler l'inscription
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Annuler l'inscription") },
                text = { Text("Êtes-vous sûr de vouloir annuler cette inscription ? Cette action est irréversible.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            eventRegistrationViewModel.updateRegistration(registrationId, "CANCELLED")
                            showDeleteConfirmation = false
                        }
                    ) {
                        Text("Annuler l'inscription")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
                        Text("Retour")
                    }
                }
            )
        }
        
        // Afficher les messages de succès/erreur
        when (uiState) {
            is EventRegistrationUiState.Success -> {
                LaunchedEffect(uiState) {
                    // Le message sera géré par le LaunchedEffect qui écoute selectedRegistration
                }
            }
            is EventRegistrationUiState.Error -> {
                // L'erreur est déjà affichée dans le contenu principal
            }
            else -> {}
        }
    }
}

