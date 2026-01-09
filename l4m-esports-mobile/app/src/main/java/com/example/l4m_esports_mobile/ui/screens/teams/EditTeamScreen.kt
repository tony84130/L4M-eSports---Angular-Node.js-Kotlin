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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTeamScreen(
    teamId: String,
    onTeamUpdated: () -> Unit,
    onBack: () -> Unit = {},
    teamViewModel: TeamViewModel = hiltViewModel()
) {
    val selectedTeam by teamViewModel.selectedTeam.collectAsState()
    val uiState by teamViewModel.uiState.collectAsState()

    var teamName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var logo by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("active") }
    var maxMembers by remember { mutableStateOf("") }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    LaunchedEffect(teamId) {
        teamViewModel.loadTeamById(teamId)
    }

    LaunchedEffect(selectedTeam) {
        selectedTeam?.let { team ->
            teamName = team.name
            description = team.description ?: ""
            logo = team.logo ?: ""
            status = team.status
            maxMembers = team.maxMembers.toString()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is TeamUiState.Success) {
            onTeamUpdated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier l'équipe") },
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

            // Statut
            var statusExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Statut") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    listOf("active", "inactive", "archived").forEach { statusOption ->
                        DropdownMenuItem(
                            text = { Text(statusOption) },
                            onClick = {
                                status = statusOption
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            // Nombre maximum de membres
            OutlinedTextField(
                value = maxMembers,
                onValueChange = { 
                    if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                        maxMembers = it
                    }
                },
                label = { Text("Nombre maximum de membres") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bouton de sauvegarde
            val isFormValid = teamName.isNotBlank()
            Button(
                onClick = {
                    hasAttemptedSubmit = true
                    if (isFormValid) {
                        teamViewModel.updateTeam(
                            id = teamId,
                            name = teamName,
                            description = description.takeIf { it.isNotBlank() },
                            logo = logo.takeIf { it.isNotBlank() },
                            status = status,
                            maxMembers = maxMembers.toIntOrNull()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is TeamUiState.Loading
            ) {
                if (uiState is TeamUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Enregistrer les modifications")
                }
            }

            if (uiState is TeamUiState.Error) {
                Text(
                    text = (uiState as TeamUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

