package com.example.l4m_esports_mobile.ui.screens.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.l4m_esports_mobile.data.model.response.GameResponse
import com.example.l4m_esports_mobile.ui.viewmodel.GameViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.GameUiState
import com.example.l4m_esports_mobile.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesListScreen(
    onGameClick: (String) -> Unit,
    onCreateGame: () -> Unit,
    gameViewModel: GameViewModel = hiltViewModel(),
    userViewModel: com.example.l4m_esports_mobile.ui.viewmodel.UserViewModel = hiltViewModel()
) {
    val games by gameViewModel.games.collectAsState()
    val uiState by gameViewModel.uiState.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        gameViewModel.loadGames()
        userViewModel.loadCurrentUser()
    }
    
    val isAdmin = currentUser?.role == "admin"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jeux") }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = onCreateGame) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Créer un jeu"
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
                games.isEmpty() -> {
                    Text(
                        text = "Aucun jeu disponible",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(games) { game ->
                            GameItem(
                                game = game,
                                onClick = { onGameClick(game.id) }
                            )
                        }
                    }
                }
            }

            if (uiState is GameUiState.Error) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text((uiState as GameUiState.Error).message)
                }
            }
        }
    }
}

@Composable
fun GameItem(
    game: GameResponse,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = game.name,
                style = MaterialTheme.typography.titleLarge
            )
            if (!game.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = game.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                AssistChip(
                    onClick = {},
                    label = { Text(if (game.isActive) "Actif" else "Inactif") }
                )
            }
        }
    }
}

