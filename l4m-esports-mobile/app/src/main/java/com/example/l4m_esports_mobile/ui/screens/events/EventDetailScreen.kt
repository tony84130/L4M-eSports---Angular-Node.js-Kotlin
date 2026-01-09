package com.example.l4m_esports_mobile.ui.screens.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.EventViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.EventUiState
import com.example.l4m_esports_mobile.ui.viewmodel.EventRegistrationViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.EventRegistrationUiState
import com.example.l4m_esports_mobile.ui.viewmodel.TeamViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.UserViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.MatchViewModel
import com.example.l4m_esports_mobile.util.formatDate
import com.example.l4m_esports_mobile.util.MapUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    onEditEvent: (String) -> Unit = {},
    onEventDeleted: () -> Unit = {},
    onViewMatches: (String) -> Unit = {},
    eventViewModel: EventViewModel = hiltViewModel(),
    eventRegistrationViewModel: EventRegistrationViewModel = hiltViewModel(),
    teamViewModel: TeamViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    matchViewModel: MatchViewModel = hiltViewModel()
) {
    val selectedEvent by eventViewModel.selectedEvent.collectAsState()
    val eventBracket by eventViewModel.eventBracket.collectAsState()
    val uiState by eventViewModel.uiState.collectAsState()
    val teams by teamViewModel.teams.collectAsState()
    val selectedTeam by teamViewModel.selectedTeam.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()
    val registrationUiState by eventRegistrationViewModel.uiState.collectAsState()
    val eventRegistrations by eventRegistrationViewModel.registrations.collectAsState()
    val matches by matchViewModel.matches.collectAsState()
    
    val isAdmin = currentUser?.role == "admin"
    
    // Vérifier si des matchs ont été joués (pour masquer le bouton de régénération)
    val hasPlayedMatches = remember(matches) {
        matches.any { match ->
            match.status != "upcoming" || 
            match.score.team1 > 0 || 
            match.score.team2 > 0 ||
            match.winner != null
        }
    }
    
    var showSelectMembersDialog by remember { mutableStateOf(false) }
    var selectedTeamId by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        eventViewModel.loadEventById(eventId)
        teamViewModel.loadTeams()
        userViewModel.loadCurrentUser()
        matchViewModel.loadMatchesByEvent(eventId)
        // Charger les inscriptions de l'événement si admin
        if (isAdmin) {
            eventRegistrationViewModel.loadRegistrationsByEvent(eventId)
        }
    }
    
    // Charger le bracket quand l'événement est chargé (toujours depuis l'API pour avoir les équipes peuplées)
    // Recharger aussi si le bracket existe déjà pour avoir les mises à jour (vainqueurs qui progressent)
    LaunchedEffect(selectedEvent?.id) {
        val currentEvent = selectedEvent
        if (currentEvent != null) {
            // Toujours charger depuis l'API pour s'assurer que les équipes sont peuplées et avoir les mises à jour
            eventViewModel.getEventBracket(currentEvent.id)
        }
    }
    
    // Recharger les inscriptions quand l'utilisateur devient admin
    LaunchedEffect(isAdmin, eventId) {
        if (isAdmin) {
            eventRegistrationViewModel.loadRegistrationsByEvent(eventId)
        }
    }

    LaunchedEffect(registrationUiState) {
        if (registrationUiState is EventRegistrationUiState.Success) {
            showSelectMembersDialog = false
            selectedTeamId = null
        }
    }
    
    // Ouvrir le dialog quand l'équipe est chargée
    LaunchedEffect(selectedTeam, selectedTeamId) {
        if (selectedTeamId != null && selectedTeam != null && selectedTeam!!.id == selectedTeamId) {
            showSelectMembersDialog = true
        }
    }

    // Trouver les équipes où l'utilisateur est capitaine et qui correspondent au jeu de l'événement
    val eligibleTeams = remember(teams, currentUser, selectedEvent) {
        val user = currentUser
        val event = selectedEvent
        if (user == null || event == null) {
            emptyList()
        } else {
            teams.filter { team ->
                team.captain?.id != null &&
                team.captain.id == user.id && 
                team.game.id == event.game.id &&
                team.status == "active"
            }
        }
    }
    
    // Calculer si l'utilisateur peut s'inscrire (basé sur le statut de l'événement)
    val canRegister = remember(selectedEvent?.status) {
        val status = selectedEvent?.status?.lowercase() ?: ""
        status == "open" || status == "draft"
    }
    
    // Filtrer les équipes qui ne sont pas déjà inscrites
    val teamsNotRegistered = remember(eligibleTeams, eventRegistrations, eventId) {
        eligibleTeams.filter { team ->
            // Vérifier si l'équipe est déjà inscrite à cet événement
            !eventRegistrations.any { registration ->
                registration.team.id == team.id && 
                registration.event.id == eventId &&
                (registration.status.uppercase() == "PENDING" || registration.status.uppercase() == "ACCEPTED")
            }
        }
    }
    
    // Charger toutes les inscriptions de l'événement pour vérifier si les équipes sont déjà inscrites
    LaunchedEffect(eventId, eligibleTeams.size, isAdmin) {
        if (eligibleTeams.isNotEmpty() && !isAdmin) {
            // Charger toutes les inscriptions de l'événement pour vérifier si nos équipes sont déjà inscrites
            eventRegistrationViewModel.loadRegistrations(event = eventId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedEvent?.name ?: "Détails de l'événement") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    if (isAdmin && selectedEvent != null) {
                        val currentEvent = selectedEvent
                        // Empêcher la modification si l'événement est en cours
                        val canEdit = currentEvent?.status != "in_progress"
                        if (canEdit == true) {
                            IconButton(onClick = { onEditEvent(eventId) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Modifier l'événement"
                                )
                            }
                        }
                        IconButton(
                            onClick = { showDeleteConfirmation = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer l'événement"
                            )
                        }
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
                uiState is EventUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                selectedEvent != null -> {
                    val event = selectedEvent!!
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
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
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
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = event.name,
                                        style = MaterialTheme.typography.headlineMedium
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
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Début: ${formatDate(event.startDate)}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Fin: ${formatDate(event.endDate)}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Inscriptions: ${formatDate(event.registrationStartDate)} - ${formatDate(event.registrationEndDate)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (!event.description.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Description",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = event.description,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    if (!event.rules.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Règles",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = event.rules,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    
                                    // Afficher le lieu et le bouton pour ouvrir Google Maps si l'événement est en présentiel
                                    if (event.location?.type == "physical") {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Lieu",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        val context = LocalContext.current
                                        val coordinates = event.location.coordinates?.coordinates
                                        val latitude = coordinates?.getOrNull(1) // Index 1 = latitude dans [longitude, latitude]
                                        val longitude = coordinates?.getOrNull(0) // Index 0 = longitude
                                        
                                        if (!event.location.address.isNullOrBlank()) {
                                            Text(
                                                text = event.location.address,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                        
                                        if (latitude != null && longitude != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    MapUtils.openGoogleMaps(
                                                        context = context,
                                                        latitude = latitude,
                                                        longitude = longitude,
                                                        address = event.location.address
                                                    )
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = "Ouvrir dans Google Maps",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Voir sur Google Maps")
                                            }
                                        } else if (!event.location.address.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    MapUtils.openGoogleMaps(
                                                        context = context,
                                                        latitude = null,
                                                        longitude = null,
                                                        address = event.location.address
                                                    )
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = "Ouvrir dans Google Maps",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Voir sur Google Maps")
                                            }
                                        }
                                    }
                                    
                                    // Afficher le vainqueur final si l'événement est terminé
                                    if (event.status == "completed") {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        // Trouver le vainqueur final dans le dernier round du bracket
                                        val finalWinner = remember(eventBracket, event.bracket) {
                                            val bracket = eventBracket ?: event.bracket
                                            bracket?.rounds?.lastOrNull()?.matches?.firstOrNull()?.winner
                                        }
                                        
                                        if (finalWinner != null) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = "🏆",
                                                        style = MaterialTheme.typography.displayMedium
                                                    )
                                                    Text(
                                                        text = "Tournoi terminé !",
                                                        style = MaterialTheme.typography.titleLarge,
                                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = "Vainqueur",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                    Text(
                                                        text = finalWinner.name ?: "N/A",
                                                        style = MaterialTheme.typography.headlineMedium,
                                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Affichage des équipes inscrites pour les admins
                        if (isAdmin && eventRegistrations.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "Équipes inscrites (${eventRegistrations.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                        
                                        eventRegistrations.forEach { registration ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = registration.team.name ?: "Équipe",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = "Statut: ${when (registration.status.uppercase()) {
                                                            "PENDING" -> "En attente"
                                                            "ACCEPTED" -> "Acceptée"
                                                            "REJECTED" -> "Rejetée"
                                                            "CANCELLED" -> "Annulée"
                                                            else -> registration.status
                                                        }}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    if (!registration.participatingMembers.isNullOrEmpty()) {
                                                        Text(
                                                            text = "${registration.participatingMembers.size} membre(s) participant(s)",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                            if (registration != eventRegistrations.last()) {
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Affichage du bracket (visible pour tous)
                        if (selectedEvent != null) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "Bracket du tournoi",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                        
                                        // Utiliser eventBracket en priorité (chargé depuis l'API avec équipes peuplées)
                                        val bracketToDisplay = eventBracket ?: event.bracket
                                        
                                        if (bracketToDisplay == null) {
                                            Text(
                                                text = "Aucun bracket généré pour cet événement",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            // Bouton de génération visible uniquement pour les admins et si aucun match n'a été joué
                                            if (isAdmin && !hasPlayedMatches) {
                                                Button(
                                                    onClick = { eventViewModel.generateBracket(eventId) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    enabled = uiState !is EventUiState.Loading
                                                ) {
                                                    Text("Générer le bracket")
                                                }
                                            }
                                        } else {
                                            if (bracketToDisplay.rounds.isNullOrEmpty()) {
                                                Text(
                                                    text = "Bracket vide",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            } else {
                                                // Afficher les rounds du bracket
                                                bracketToDisplay.rounds?.forEach { round ->
                                                    Text(
                                                        text = "Round ${round.roundNumber}",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                        modifier = Modifier.padding(vertical = 8.dp)
                                                    )
                                                    
                                                    round.matches?.forEach { match ->
                                                        Card(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 4.dp),
                                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(12.dp),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Column(
                                                                    modifier = Modifier.weight(1f),
                                                                    horizontalAlignment = Alignment.Start
                                                                ) {
                                                                    Text(
                                                                        text = match.team1?.name ?: "À déterminer",
                                                                        style = MaterialTheme.typography.bodyMedium,
                                                                        fontWeight = if (match.winner?.id == match.team1?.id) {
                                                                            androidx.compose.ui.text.font.FontWeight.Bold
                                                                        } else {
                                                                            androidx.compose.ui.text.font.FontWeight.Normal
                                                                        },
                                                                        color = if (match.winner?.id == match.team1?.id) {
                                                                            MaterialTheme.colorScheme.primary
                                                                        } else {
                                                                            MaterialTheme.colorScheme.onSurface
                                                                        }
                                                                    )
                                                                }
                                                                Text(
                                                                    text = " VS ",
                                                                    style = MaterialTheme.typography.bodyMedium,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    modifier = Modifier.padding(horizontal = 12.dp)
                                                                )
                                                                Column(
                                                                    modifier = Modifier.weight(1f),
                                                                    horizontalAlignment = Alignment.End
                                                                ) {
                                                                    Text(
                                                                        text = match.team2?.name ?: "À déterminer",
                                                                        style = MaterialTheme.typography.bodyMedium,
                                                                        fontWeight = if (match.winner?.id == match.team2?.id) {
                                                                            androidx.compose.ui.text.font.FontWeight.Bold
                                                                        } else {
                                                                            androidx.compose.ui.text.font.FontWeight.Normal
                                                                        },
                                                                        color = if (match.winner?.id == match.team2?.id) {
                                                                            MaterialTheme.colorScheme.primary
                                                                        } else {
                                                                            MaterialTheme.colorScheme.onSurface
                                                                        }
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            // Bouton pour régénérer le bracket (visible uniquement pour les admins et si aucun match n'a été joué)
                                            if (isAdmin && !hasPlayedMatches) {
                                                Button(
                                                    onClick = { eventViewModel.generateBracket(eventId) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    enabled = uiState !is EventUiState.Loading
                                                ) {
                                                    Text("Régénérer le bracket")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Section Matchs (si l'événement est en cours ou terminé)
                        if (selectedEvent != null && (event.status == "in_progress" || event.status == "completed")) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Matchs",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                            )
                                            TextButton(onClick = { onViewMatches(eventId) }) {
                                                Text("Voir tous les matchs")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Bouton d'inscription pour les capitaines
                        // Ne pas afficher si l'événement est en cours, terminé, annulé ou si l'équipe est déjà inscrite
                        if (teamsNotRegistered.isNotEmpty() && currentUser != null && !isAdmin && canRegister) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Inscription à l'événement",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        teamsNotRegistered.forEach { team ->
                                            Button(
                                                onClick = {
                                                    selectedTeamId = team.id
                                                    // Charger l'équipe pour avoir les membres à jour
                                                    teamViewModel.loadTeamById(team.id)
                                                    // Le dialog s'ouvrira automatiquement quand selectedTeam sera chargé
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                enabled = registrationUiState !is EventRegistrationUiState.Loading
                                            ) {
                                                Text("S'inscrire avec ${team.name}")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                uiState is EventUiState.Error -> {
                    Text(
                        text = (uiState as EventUiState.Error).message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Dialog de sélection des membres
            if (showSelectMembersDialog && selectedTeamId != null && selectedEvent != null) {
                // Utiliser selectedTeam si disponible (chargé avec loadTeamById), sinon chercher dans teams
                val teamToUse = selectedTeam?.takeIf { it.id == selectedTeamId } 
                    ?: teams.find { it.id == selectedTeamId }
                
                if (teamToUse != null && currentUser != null) {
                    SelectMembersDialog(
                        eventFormat = selectedEvent!!.format,
                        teamMembers = teamToUse.members,
                        captainId = teamToUse.captain.id,
                        onDismiss = {
                            showSelectMembersDialog = false
                            selectedTeamId = null
                        },
                        onConfirm = { selectedMemberIds ->
                            eventRegistrationViewModel.createRegistration(
                                teamId = selectedTeamId!!,
                                eventId = eventId,
                                participatingMemberIds = selectedMemberIds
                            )
                        }
                    )
                }
            }
            
            // Afficher les erreurs d'inscription
            if (registrationUiState is EventRegistrationUiState.Error) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { eventRegistrationViewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text((registrationUiState as EventRegistrationUiState.Error).message)
                }
            }
            
            // Afficher les erreurs de génération de bracket ou autres erreurs
            if (uiState is EventUiState.Error) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    action = {
                        TextButton(onClick = { eventViewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text((uiState as EventUiState.Error).message)
                }
            }
            
            // Dialog de confirmation pour la suppression
            if (showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = { Text("Supprimer l'événement") },
                    text = { Text("Êtes-vous sûr de vouloir supprimer cet événement ? Cette action est irréversible.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                eventViewModel.deleteEvent(eventId)
                                showDeleteConfirmation = false
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Supprimer")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmation = false }) {
                            Text("Annuler")
                        }
                    }
                )
            }
        }
    }
    
    // Observer le succès de la suppression pour naviguer en arrière
    LaunchedEffect(uiState, selectedEvent) {
        val currentUiState = uiState
        if (currentUiState is EventUiState.Success && currentUiState.message.contains("supprimé") && selectedEvent == null) {
            onEventDeleted()
        }
    }
}

