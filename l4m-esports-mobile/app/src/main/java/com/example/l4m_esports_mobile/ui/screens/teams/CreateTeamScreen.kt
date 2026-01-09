package com.example.l4m_esports_mobile.ui.screens.teams

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.TeamViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.TeamUiState
import com.example.l4m_esports_mobile.ui.viewmodel.GameViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.GameUiState
import com.example.l4m_esports_mobile.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeamScreen(
    onTeamCreated: () -> Unit,
    onBack: () -> Unit = {},
    teamViewModel: TeamViewModel = hiltViewModel(),
    gameViewModel: GameViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val teamUiState by teamViewModel.uiState.collectAsState()
    val games by gameViewModel.games.collectAsState()
    val gameUiState by gameViewModel.uiState.collectAsState()
    val teams by teamViewModel.teams.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()

    var teamName by remember { mutableStateOf("") }
    var selectedGameId by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var logo by remember { mutableStateOf("") }
    var maxMembers by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        gameViewModel.loadGames()
        teamViewModel.loadTeams()
        userViewModel.loadCurrentUser()
    }

    // Filtrer les jeux : exclure ceux où l'utilisateur est déjà capitaine OU membre d'une équipe active
    val availableGames = remember(games, teams, currentUser) {
        val userTeamsAsCaptain = teams.filter { 
            it.captain.id == currentUser?.id && it.status == "active" 
        }
        val gamesWhereUserIsCaptain = userTeamsAsCaptain.map { it.game.id }.toSet()
        
        // Trouver les jeux où l'utilisateur est déjà membre (mais pas capitaine)
        val userTeamsAsMember = teams.filter { 
            it.members.any { member -> member.id == currentUser?.id } && 
            it.status == "active" &&
            it.captain.id != currentUser?.id // Exclure les équipes où il est capitaine (déjà géré)
        }
        val gamesWhereUserIsMember = userTeamsAsMember.map { it.game.id }.toSet()
        
        games.filter { game ->
            game.isActive && 
            game.id !in gamesWhereUserIsCaptain && 
            game.id !in gamesWhereUserIsMember
        }
    }

    LaunchedEffect(teamUiState) {
        if (teamUiState is TeamUiState.Success) {
            // Recharger les données utilisateur pour voir le changement de rôle
            userViewModel.loadCurrentUser()
            // Recharger les équipes pour mettre à jour la liste des jeux disponibles
            teamViewModel.loadTeams()
            onTeamCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créer une équipe") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nom de l'équipe
            val showNameError = hasAttemptedSubmit && teamName.isBlank()
            OutlinedTextField(
                value = teamName,
                onValueChange = { teamName = it },
                label = { Text("Nom de l'équipe *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showNameError,
                supportingText = {
                    if (showNameError) {
                        Text(
                            text = "Le nom de l'équipe est obligatoire",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            // Sélection du jeu
            val showGameError = hasAttemptedSubmit && selectedGameId == null
            ExposedDropdownMenuBox(
                expanded = expanded && availableGames.isNotEmpty(),
                onExpandedChange = { if (availableGames.isNotEmpty()) expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedGameId?.let { gameId ->
                        availableGames.find { it.id == gameId }?.name ?: ""
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Jeu *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && availableGames.isNotEmpty()) },
                    isError = showGameError || availableGames.isEmpty(),
                    enabled = availableGames.isNotEmpty(),
                    supportingText = {
                        when {
                            showGameError -> {
                                Text(
                                    text = "Vous devez sélectionner un jeu",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            availableGames.isEmpty() -> {
                                Text(
                                    text = "Vous êtes déjà capitaine d'une équipe pour tous les jeux disponibles",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                )
                if (availableGames.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableGames.forEach { game ->
                            DropdownMenuItem(
                                text = { Text(game.name) },
                                onClick = {
                                    selectedGameId = game.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optionnel)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Logo URL
            OutlinedTextField(
                value = logo,
                onValueChange = { logo = it },
                label = { Text("URL du logo (optionnel)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Nombre maximum de membres
            OutlinedTextField(
                value = maxMembers,
                onValueChange = { 
                    if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                        maxMembers = it
                    }
                },
                label = { Text("Nombre maximum de membres (optionnel)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = {
                    Text("Laisser vide pour utiliser la valeur par défaut du jeu")
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bouton de création
            val isFormValid = teamName.isNotBlank() && selectedGameId != null
            Button(
                onClick = {
                    hasAttemptedSubmit = true
                    if (isFormValid && selectedGameId != null) {
                        teamViewModel.createTeam(
                            name = teamName,
                            game = selectedGameId!!,
                            description = description.takeIf { it.isNotBlank() },
                            logo = logo.takeIf { it.isNotBlank() },
                            maxMembers = maxMembers.toIntOrNull()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = teamUiState !is TeamUiState.Loading
            ) {
                if (teamUiState is TeamUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Créer l'équipe")
                }
            }

            if (teamUiState is TeamUiState.Error) {
                Text(
                    text = (teamUiState as TeamUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (gameUiState is GameUiState.Error) {
                Text(
                    text = (gameUiState as GameUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

