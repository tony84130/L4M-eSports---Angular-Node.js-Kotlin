package com.example.l4m_esports_mobile.ui.screens.games

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
import com.example.l4m_esports_mobile.ui.viewmodel.GameViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.GameUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGameScreen(
    onGameCreated: () -> Unit,
    onBack: () -> Unit = {},
    gameViewModel: GameViewModel = hiltViewModel()
) {
    val uiState by gameViewModel.uiState.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var logo by remember { mutableStateOf("") }
    var rules by remember { mutableStateOf("") }
    var selectedFormats by remember { mutableStateOf<Set<String>>(emptySet()) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }
    
    val availableFormats = listOf("1v1", "2v2", "3v3", "4v4", "5v5", "BATTLE_ROYALE")

    LaunchedEffect(uiState) {
        if (uiState is GameUiState.Success) {
            onGameCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créer un jeu") },
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
            // Nom du jeu (obligatoire)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom du jeu *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = hasAttemptedSubmit && name.isBlank(),
                supportingText = {
                    if (hasAttemptedSubmit && name.isBlank()) {
                        Text("Le nom est obligatoire")
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

            // Règles
            OutlinedTextField(
                value = rules,
                onValueChange = { rules = it },
                label = { Text("Règles (optionnel)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Formats disponibles
            Text(
                text = "Formats disponibles *",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Sélectionnez au moins un format pour ce jeu",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            availableFormats.forEach { format ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedFormats.contains(format),
                        onCheckedChange = { checked ->
                            selectedFormats = if (checked) {
                                selectedFormats + format
                            } else {
                                selectedFormats - format
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = format,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            if (hasAttemptedSubmit && selectedFormats.isEmpty()) {
                Text(
                    text = "Au moins un format est obligatoire",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bouton de création
            val isFormValid = name.isNotBlank() && selectedFormats.isNotEmpty()
            Button(
                onClick = {
                    hasAttemptedSubmit = true
                    if (isFormValid) {
                        gameViewModel.createGame(
                            name = name,
                            description = description.takeIf { it.isNotBlank() },
                            logo = logo.takeIf { it.isNotBlank() },
                            rules = rules.takeIf { it.isNotBlank() },
                            formats = selectedFormats.toList()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is GameUiState.Loading
            ) {
                if (uiState is GameUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Créer le jeu")
                }
            }

            if (uiState is GameUiState.Error) {
                Text(
                    text = (uiState as GameUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

