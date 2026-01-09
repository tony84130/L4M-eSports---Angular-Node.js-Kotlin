package com.example.l4m_esports_mobile.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.l4m_esports_mobile.ui.viewmodel.TwitchUiState
import com.example.l4m_esports_mobile.ui.viewmodel.UserViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.UserUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onSaveSuccess: () -> Unit,
    onBack: () -> Unit = {},
    viewModel: UserViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val twitchUser by viewModel.twitchUser.collectAsState()
    val twitchState by viewModel.twitchState.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var twitchUsername by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            firstName = user.firstName ?: ""
            lastName = user.lastName ?: ""
            email = user.email ?: ""
            twitchUsername = user.twitchUsername ?: ""
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is UserUiState.Success) {
            onSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier le profil") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Prénom") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Nom") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Nouveau mot de passe (laisser vide pour ne pas changer)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Masquer le mot de passe" else "Afficher le mot de passe"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section Twitch
            Text(
                text = "Lier un compte Twitch",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = twitchUsername,
                    onValueChange = { 
                        twitchUsername = it
                        viewModel.clearTwitchState()
                    },
                    label = { Text("Nom d'utilisateur Twitch") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        val username = twitchUsername.trim()
                        if (username.isNotBlank()) {
                            viewModel.fetchTwitchUser(username)
                        } else {
                            viewModel.clearTwitchState()
                        }
                    },
                    enabled = twitchState !is TwitchUiState.Loading && twitchUsername.trim().isNotBlank()
                ) {
                    if (twitchState is TwitchUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Tester")
                    }
                }
            }

            // Message Twitch
            val currentTwitchState = twitchState
            if (currentTwitchState is TwitchUiState.Success || currentTwitchState is TwitchUiState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (currentTwitchState) {
                        is TwitchUiState.Success -> currentTwitchState.message
                        is TwitchUiState.Error -> currentTwitchState.message
                        else -> ""
                    },
                    color = if (currentTwitchState is TwitchUiState.Success) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Aperçu Twitch
            twitchUser?.let { twitch ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        twitch.profileImageUrl?.let { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Avatar Twitch",
                                modifier = Modifier
                                    .size(64.dp)
                                    .align(Alignment.CenterVertically),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = twitch.displayName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            twitch.description?.let { desc ->
                                if (desc.isNotBlank()) {
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                twitch.viewCount?.let {
                                    Text(
                                        text = "Vues: $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                twitch.followers?.let {
                                    Text(
                                        text = "Followers: $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Live: ${if (twitch.isLive == true) "Oui" else "Non"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (twitch.isLive == true) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.updateCurrentUser(
                        firstName = firstName.takeIf { it.isNotBlank() },
                        lastName = lastName.takeIf { it.isNotBlank() },
                        email = email.takeIf { it.isNotBlank() },
                        password = password.takeIf { it.isNotBlank() },
                        twitchUsername = twitchUsername.takeIf { it.isNotBlank() },
                        latitude = null,
                        longitude = null,
                        avatar = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is UserUiState.Loading
            ) {
                if (uiState is UserUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Enregistrer")
                }
            }

            if (uiState is UserUiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (uiState as UserUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

