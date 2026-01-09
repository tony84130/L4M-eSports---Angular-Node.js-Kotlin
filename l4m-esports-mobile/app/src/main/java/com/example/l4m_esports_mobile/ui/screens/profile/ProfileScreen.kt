package com.example.l4m_esports_mobile.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.AuthViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.UserViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.UserUiState
import com.example.l4m_esports_mobile.ui.viewmodel.AuthUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onSignOut: () -> Unit,
    onAccountDeleted: () -> Unit,
    userViewModel: UserViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val uiState by userViewModel.uiState.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userViewModel.loadCurrentUser()
    }

    LaunchedEffect(authUiState) {
        if (authUiState is AuthUiState.Success) {
            onSignOut()
        }
    }

    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is UserUiState.Success && currentState.message.contains("supprimé")) {
            onAccountDeleted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon Profil") },
                actions = {
                    TextButton(onClick = onEditProfile) {
                        Text("Modifier")
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
        ) {
            if (uiState is UserUiState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (currentUser != null) {
                val user = currentUser!!
                
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Informations personnelles",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ProfileField(label = "Email", value = user.email)
                        ProfileField(label = "Prénom", value = user.firstName)
                        ProfileField(label = "Nom", value = user.lastName)
                        ProfileField(label = "Pseudonyme", value = user.gamertag)
                        ProfileField(label = "Rôle", value = user.role)
                        ProfileField(label = "Twitch", value = user.twitchUsername ?: "Non renseigné")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bouton de déconnexion
                Button(
                    onClick = {
                        authViewModel.signOut()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = authUiState !is AuthUiState.Loading && uiState !is UserUiState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    if (authUiState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    } else {
                        Text("Se déconnecter")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bouton de suppression de compte
                Button(
                    onClick = {
                        showDeleteConfirmation = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is UserUiState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Supprimer mon compte")
                }

                if (uiState is UserUiState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (uiState as UserUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else if (uiState is UserUiState.Error) {
                Text(
                    text = (uiState as UserUiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // Dialog de confirmation pour la suppression
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Supprimer le compte") },
            text = { Text("Êtes-vous sûr de vouloir supprimer votre compte ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        userViewModel.deleteCurrentUser()
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

@Composable
fun ProfileField(label: String, value: String) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

