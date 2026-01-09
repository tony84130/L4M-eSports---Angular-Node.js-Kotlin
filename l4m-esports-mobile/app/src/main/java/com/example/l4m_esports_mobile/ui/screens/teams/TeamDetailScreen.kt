package com.example.l4m_esports_mobile.ui.screens.teams

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.TeamViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.TeamUiState
import com.example.l4m_esports_mobile.ui.viewmodel.UserViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.TeamRequestViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.NotificationViewModel
import com.example.l4m_esports_mobile.navigation.Screen
import com.example.l4m_esports_mobile.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    teamId: String,
    onBack: () -> Unit,
    onEditTeam: (String) -> Unit = {},
    onTeamDeleted: () -> Unit = {},
    onViewRequests: (String) -> Unit = {},
    onViewRegistrations: (String) -> Unit = {},
    teamViewModel: TeamViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    teamRequestViewModel: TeamRequestViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val selectedTeam by teamViewModel.selectedTeam.collectAsState()
    val uiState by teamViewModel.uiState.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()
    val teamRequests by teamRequestViewModel.teamRequests.collectAsState()
    val teamRequestUiState by teamRequestViewModel.uiState.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var joinMessage by remember { mutableStateOf("") }
    var showInviteDialog by remember { mutableStateOf(false) }
    var inviteGamertag by remember { mutableStateOf("") }
    var showRemoveMemberDialog by remember { mutableStateOf<String?>(null) }
    var showLeaveTeamDialog by remember { mutableStateOf(false) }
    var showTransferCaptainDialog by remember { mutableStateOf(false) }
    var selectedNewCaptainId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(teamId) {
        teamViewModel.loadTeamById(teamId)
        teamViewModel.loadTeams() // Charger toutes les équipes pour vérifier les membres
        userViewModel.loadCurrentUser()
    }

    // Charger les demandes quand l'utilisateur est disponible
    LaunchedEffect(currentUser?.id) {
        currentUser?.id?.let { userId ->
            teamRequestViewModel.loadTeamRequests(team = teamId, user = userId)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is TeamUiState.Success) {
            val message = (uiState as TeamUiState.Success).message
            if (message.contains("supprimée")) {
                // Recharger les données utilisateur pour voir le changement de rôle
                userViewModel.loadCurrentUser()
                onTeamDeleted()
            } else if (message.contains("Invitation") || message.contains("retiré")) {
                // Recharger le nombre de notifications non lues après invitation ou retrait
                notificationViewModel.loadUnreadCount()
            } else if (message.contains("transféré") || message.contains("quitté")) {
                // Recharger les données utilisateur et les équipes après transfert ou départ
                userViewModel.loadCurrentUser()
                teamViewModel.loadTeams()
                notificationViewModel.loadUnreadCount()
            }
        }
    }

    // Vérifier si l'utilisateur est le capitaine
    val isCaptain = selectedTeam?.captain?.id != null && selectedTeam?.captain?.id == currentUser?.id
    
    // Vérifier si l'utilisateur est membre de l'équipe
    val isMember = selectedTeam?.members?.any { it.id == currentUser?.id } == true
    
    // Vérifier si l'utilisateur a déjà une demande en attente pour cette équipe
    val hasPendingRequest = teamRequests.any { 
        it.team == teamId && it.user.id == currentUser?.id && it.status == "pending" 
    }
    
    // Vérifier si l'utilisateur est déjà membre d'une autre équipe du même jeu
    val teams by teamViewModel.teams.collectAsState()
    val currentTeam = selectedTeam // Stocker dans une variable locale pour permettre le smart cast
    val currentUserLocal = currentUser // Stocker dans une variable locale pour permettre le smart cast
    val isAlreadyMemberOfSameGame = remember(teams, currentTeam, currentUserLocal) {
        if (currentTeam == null || currentUserLocal == null) return@remember false
        teams.any { team ->
            team.game.id == currentTeam.game.id && 
            team.id != currentTeam.id && 
            team.status == "active" &&
            team.members.any { member -> member.id == currentUserLocal.id }
        }
    }
    
    // Vérifier si l'utilisateur est admin
    val isAdmin = currentUser?.role == "admin"
    
    // Vérifier si l'utilisateur peut rejoindre (membre ou capitaine, pas admin, pas déjà membre, pas de demande en attente, pas déjà membre d'une autre équipe du même jeu)
    val canJoin = !isAdmin && !isCaptain && !isMember && !hasPendingRequest && !isAlreadyMemberOfSameGame &&
                  selectedTeam?.status == "active" &&
                  (selectedTeam?.members?.size ?: 0) < (selectedTeam?.maxMembers ?: 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedTeam?.name ?: "Détails de l'équipe") },
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
                uiState is TeamUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                selectedTeam == null -> {
                    Text(
                        text = "Équipe introuvable",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    val team = selectedTeam!!
                    val teamDescription = team.description
                    val teamCreatedAt = team.createdAt
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // Nom de l'équipe
                            Text(
                                text = team.name,
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                        
                        item {

                            // Jeu
                            if (!team.game.name.isNullOrBlank()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Jeu:",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    AssistChip(
                                        onClick = {},
                                        label = { 
                                            Text(
                                                text = team.game.name,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    )
                                }
                            }

                            // Statut
                            AssistChip(
                                onClick = {},
                                label = { Text(team.status) }
                            )
                        }
                        
                        item {
                            // Description
                            if (!teamDescription.isNullOrBlank()) {
                                Text(
                                    text = "Description",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = teamDescription,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        
                        item {
                            // Membres
                            Text(
                                text = "Membres",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${team.members.size}/${team.maxMembers} membres",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (team.members.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    team.members.forEach { member ->
                                        val isCaptainMember = team.captain?.id != null && member.id == team.captain.id
                                        val role = if (isCaptainMember) "Capitaine" else "Membre"
                                        val gamertag = member.gamertag ?: "Membre ${member.id.take(8)}"
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = "• $gamertag",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                                AssistChip(
                                                    onClick = {},
                                                    label = { 
                                                        Text(
                                                            text = role,
                                                            style = MaterialTheme.typography.labelSmall
                                                        )
                                                    },
                                                    colors = AssistChipDefaults.assistChipColors(
                                                        containerColor = if (isCaptainMember) {
                                                            MaterialTheme.colorScheme.primaryContainer
                                                        } else {
                                                            MaterialTheme.colorScheme.surfaceVariant
                                                        }
                                                    )
                                                )
                                            }
                                            // Bouton retirer pour les membres (pas le capitaine) - seulement pour les capitaines, pas les admins
                                            if (!isCaptainMember && !isAdmin && team.captain?.id != null && currentUser?.id == team.captain.id) {
                                                TextButton(
                                                    onClick = { showRemoveMemberDialog = member.id }
                                                ) {
                                                    Text(
                                                        text = "Retirer",
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        item {
                            // Dates
                            if (teamCreatedAt != null) {
                                Text(
                                    text = "Créée le: ${formatDate(teamCreatedAt)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        
                        item {
                            // Boutons d'action
                        if (isAdmin) {
                            // Bouton pour supprimer l'équipe (admin peut supprimer n'importe quelle équipe)
                            Button(
                                onClick = { showDeleteConfirmation = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Supprimer l'équipe")
                            }
                        } else if (isCaptain) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Bouton pour voir les demandes
                                Button(
                                    onClick = { onViewRequests(teamId) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Voir les demandes")
                                }
                                
                                // Bouton pour voir les inscriptions
                                Button(
                                    onClick = { onViewRegistrations(teamId) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Voir les inscriptions")
                                }
                                
                                // Bouton pour transférer le capitaine (si il y a d'autres membres)
                                val otherMembers = team.members.filter { it.id != team.captain?.id }
                                if (otherMembers.isNotEmpty()) {
                                    Button(
                                        onClick = { showTransferCaptainDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    ) {
                                        Text("Transférer le rôle de capitaine")
                                    }
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onEditTeam(teamId) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Modifier")
                                    }

                                    Button(
                                        onClick = { showDeleteConfirmation = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Supprimer")
                                    }
                                }
                            }
                        } else if (canJoin) {
                            // Bouton pour rejoindre l'équipe (membres seulement)
                            Button(
                                onClick = { showJoinDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Rejoindre l'équipe")
                            }
                        } else if (hasPendingRequest) {
                            // Afficher le statut de la demande
                            AssistChip(
                                onClick = {},
                                label = { Text("Demande en attente") }
                            )
                        } else if (isAlreadyMemberOfSameGame) {
                            // Afficher un message si l'utilisateur est déjà membre d'une autre équipe du même jeu
                            AssistChip(
                                onClick = {},
                                label = { Text("Vous êtes déjà membre d'une équipe de ce jeu") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            )
                        } else if (isMember) {
                            // Bouton pour quitter l'équipe (membres seulement, pas le capitaine)
                            Button(
                                onClick = { showLeaveTeamDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text("Quitter l'équipe")
                            }
                        }
                        }
                    }
                }
            }

            // Afficher les erreurs
            if (uiState is TeamUiState.Error) {
                Text(
                    text = (uiState as TeamUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }

            // Afficher les erreurs de demande
            if (teamRequestUiState is com.example.l4m_esports_mobile.ui.viewmodel.TeamRequestUiState.Error) {
                Text(
                    text = (teamRequestUiState as com.example.l4m_esports_mobile.ui.viewmodel.TeamRequestUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }

    // Dialog de confirmation de suppression
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Supprimer l'équipe") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cette équipe ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        teamViewModel.deleteTeam(teamId)
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialog pour rejoindre l'équipe
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { 
                showJoinDialog = false
                joinMessage = ""
            },
            title = { Text("Rejoindre l'équipe") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Voulez-vous envoyer une demande pour rejoindre cette équipe ?")
                    OutlinedTextField(
                        value = joinMessage,
                        onValueChange = { joinMessage = it },
                        label = { Text("Message (optionnel)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        teamRequestViewModel.createTeamRequest(teamId, joinMessage.takeIf { it.isNotBlank() })
                        showJoinDialog = false
                        joinMessage = ""
                    }
                ) {
                    Text("Envoyer la demande")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showJoinDialog = false
                    joinMessage = ""
                }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Recharger les demandes après création
    LaunchedEffect(teamRequestUiState) {
        if (teamRequestUiState is com.example.l4m_esports_mobile.ui.viewmodel.TeamRequestUiState.Success) {
            currentUser?.id?.let { userId ->
                teamRequestViewModel.loadTeamRequests(team = teamId, user = userId)
            }
        }
    }

    // Dialog pour inviter un membre
    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { 
                showInviteDialog = false
                inviteGamertag = ""
            },
            title = { Text("Inviter un membre") },
            text = {
                OutlinedTextField(
                    value = inviteGamertag,
                    onValueChange = { inviteGamertag = it },
                    label = { Text("Gamertag") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (inviteGamertag.isNotBlank()) {
                            teamViewModel.inviteMember(teamId, inviteGamertag.trim())
                            showInviteDialog = false
                            inviteGamertag = ""
                        }
                    }
                ) {
                    Text("Inviter")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showInviteDialog = false
                    inviteGamertag = ""
                }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialog pour confirmer le retrait d'un membre
    showRemoveMemberDialog?.let { memberId ->
        val member = selectedTeam?.members?.find { it.id == memberId }
        AlertDialog(
            onDismissRequest = { showRemoveMemberDialog = null },
            title = { Text("Retirer un membre") },
            text = { 
                Text("Êtes-vous sûr de vouloir retirer ${member?.gamertag ?: "ce membre"} de l'équipe ?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        teamViewModel.removeMember(teamId, memberId)
                        showRemoveMemberDialog = null
                    }
                ) {
                    Text("Retirer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveMemberDialog = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialog pour quitter l'équipe
    if (showLeaveTeamDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveTeamDialog = false },
            title = { Text("Quitter l'équipe") },
            text = { 
                if (isCaptain) {
                    Text("Vous êtes le capitaine. Vous devez d'abord transférer le rôle de capitaine à un autre membre avant de pouvoir quitter l'équipe.")
                } else {
                    Text("Êtes-vous sûr de vouloir quitter cette équipe ?")
                }
            },
            confirmButton = {
                if (!isCaptain) {
                    TextButton(
                        onClick = {
                            teamViewModel.leaveTeam(teamId)
                            showLeaveTeamDialog = false
                            onBack() // Retourner à la liste des équipes
                        }
                    ) {
                        Text("Quitter", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    TextButton(
                        onClick = {
                            showLeaveTeamDialog = false
                            showTransferCaptainDialog = true
                        }
                    ) {
                        Text("Transférer le capitaine")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveTeamDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialog pour transférer le capitaine
    if (showTransferCaptainDialog) {
        val otherMembers = selectedTeam?.members?.filter { it.id != selectedTeam?.captain?.id } ?: emptyList()
        AlertDialog(
            onDismissRequest = { 
                showTransferCaptainDialog = false
                selectedNewCaptainId = null
            },
            title = { Text("Transférer le capitaine") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Sélectionnez un membre pour devenir le nouveau capitaine :")
                    Spacer(modifier = Modifier.height(8.dp))
                    if (otherMembers.isEmpty()) {
                        Text(
                            text = "Aucun autre membre disponible",
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        otherMembers.forEach { member ->
                            val gamertag = member.gamertag ?: "Membre ${member.id.take(8)}"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(gamertag)
                                RadioButton(
                                    selected = selectedNewCaptainId == member.id,
                                    onClick = { selectedNewCaptainId = member.id }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedNewCaptainId?.let { newCaptainId ->
                            teamViewModel.transferCaptain(teamId, newCaptainId)
                            showTransferCaptainDialog = false
                            selectedNewCaptainId = null
                        }
                    },
                    enabled = selectedNewCaptainId != null && otherMembers.isNotEmpty()
                ) {
                    Text("Transférer")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showTransferCaptainDialog = false
                    selectedNewCaptainId = null
                }) {
                    Text("Annuler")
                }
            }
        )
    }
}

