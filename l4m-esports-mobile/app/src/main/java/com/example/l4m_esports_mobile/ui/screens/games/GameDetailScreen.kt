package com.example.l4m_esports_mobile.ui.screens.games

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
import com.example.l4m_esports_mobile.ui.viewmodel.GameViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.GameUiState
import com.example.l4m_esports_mobile.ui.viewmodel.EventViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.EventUiState
import com.example.l4m_esports_mobile.ui.viewmodel.UserViewModel
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    gameId: String,
    onBack: () -> Unit,
    onEventClick: (String) -> Unit,
    onCreateEvent: () -> Unit = {},
    viewModel: GameViewModel = hiltViewModel(),
    eventViewModel: EventViewModel = hiltViewModel(),
    userViewModel: com.example.l4m_esports_mobile.ui.viewmodel.UserViewModel = hiltViewModel()
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.role == "admin"

    LaunchedEffect(Unit) {
        userViewModel.loadCurrentUser()
    }
    val selectedGame by viewModel.selectedGame.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val events by eventViewModel.events.collectAsState()
    val eventUiState by eventViewModel.uiState.collectAsState()
    
    // État pour filtrer les événements en présentiel
    var showPhysicalOnly by remember { mutableStateOf(false) }

    LaunchedEffect(gameId) {
        viewModel.loadGameById(gameId)
        // Les admins voient tous les événements, les autres voient seulement les événements à venir
        if (isAdmin) {
            eventViewModel.loadEvents(game = gameId)
        } else {
            eventViewModel.loadEvents(game = gameId, upcoming = "true")
        }
    }
    
    // Recharger les événements à chaque fois qu'on entre dans l'écran
    // Cela permet de voir les nouveaux événements créés
    LaunchedEffect(Unit, isAdmin) {
        // Les admins voient tous les événements, les autres voient seulement les événements à venir
        if (isAdmin) {
            eventViewModel.loadEvents(game = gameId)
        } else {
            eventViewModel.loadEvents(game = gameId, upcoming = "true")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails du jeu") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = onCreateEvent) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Créer un événement"
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
                uiState is GameUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                selectedGame != null -> {
                    val game = selectedGame!!
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Informations du jeu
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = game.name,
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(if (game.isActive) "Actif" else "Inactif") }
                                    )
                                    if (!game.description.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Description",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = game.description,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    if (!game.rules.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Règles",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = game.rules,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }

                        // Section Événements
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isAdmin) "Tous les événements" else "Événements à venir",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                
                                // Bouton pour événements en présentiel (tous, peu importe la distance)
                                TextButton(
                                    onClick = {
                                        showPhysicalOnly = !showPhysicalOnly
                                        if (showPhysicalOnly) {
                                            // Charger tous les événements pour filtrer ensuite
                                            if (isAdmin) {
                                                eventViewModel.loadEvents(game = gameId)
                                            } else {
                                                eventViewModel.loadEvents(game = gameId, upcoming = "true")
                                            }
                                        } else {
                                            // Recharger les événements normaux
                                            if (isAdmin) {
                                                eventViewModel.loadEvents(game = gameId)
                                            } else {
                                                eventViewModel.loadEvents(game = gameId, upcoming = "true")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Événements en présentiel",
                                        modifier = Modifier.size(18.dp),
                                        tint = if (showPhysicalOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (showPhysicalOnly) "Tous" else "En présentiel",
                                        color = if (showPhysicalOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        if (eventUiState is EventUiState.Loading) {
                            item {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            }
                        } else {
                            // Filtrer les événements si on veut seulement ceux en présentiel
                            val filteredEvents = if (showPhysicalOnly) {
                                events.filter { it.location?.type == "physical" }
                            } else {
                                events
                            }
                            
                            if (filteredEvents.isEmpty()) {
                                item {
                                    Text(
                                        text = if (showPhysicalOnly) "Aucun événement en présentiel" else "Aucun événement à venir",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            } else {
                                items(filteredEvents) { event ->
                                    EventItem(
                                        event = event,
                                        onClick = { onEventClick(event.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                uiState is GameUiState.Error -> {
                    Text(
                        text = (uiState as GameUiState.Error).message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

