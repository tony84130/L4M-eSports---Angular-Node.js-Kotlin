package com.example.l4m_esports_mobile.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.AuthViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.AuthUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var gamertag by remember { mutableStateOf("") }
    var twitchUsername by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onSignUpSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Inscription",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        val showPasswordMismatch = hasAttemptedSubmit && confirmPassword.isNotBlank() && password != confirmPassword
        val showPasswordLengthError = hasAttemptedSubmit && password.isNotBlank() && password.length < 6

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = showPasswordLengthError,
            supportingText = {
                if (showPasswordLengthError) {
                    Text(
                        text = "Le mot de passe doit contenir au moins 6 caractères",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
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

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmer le mot de passe") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = showPasswordMismatch,
            supportingText = {
                if (showPasswordMismatch) {
                    Text(
                        text = "Les mots de passe ne correspondent pas",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (confirmPasswordVisible) "Masquer le mot de passe" else "Afficher le mot de passe"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        val showFirstNameError = hasAttemptedSubmit && firstName.isBlank()
        val showLastNameError = hasAttemptedSubmit && lastName.isBlank()
        val showGamertagLengthError = hasAttemptedSubmit && gamertag.isNotBlank() && (gamertag.length < 2 || gamertag.length > 30)
        val showGamertagEmptyError = hasAttemptedSubmit && gamertag.isBlank()

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("Prénom *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = showFirstNameError,
            supportingText = {
                if (showFirstNameError) {
                    Text(
                        text = "Le prénom est obligatoire",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Nom *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = showLastNameError,
            supportingText = {
                if (showLastNameError) {
                    Text(
                        text = "Le nom est obligatoire",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = gamertag,
            onValueChange = { gamertag = it },
            label = { Text("Pseudonyme (Gamertag) *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = showGamertagLengthError || showGamertagEmptyError,
            supportingText = {
                when {
                    showGamertagEmptyError -> {
                        Text(
                            text = "Le pseudonyme est obligatoire",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    showGamertagLengthError -> {
                        Text(
                            text = "Le pseudonyme doit contenir entre 2 et 30 caractères",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = twitchUsername,
            onValueChange = { twitchUsername = it },
            label = { Text("Nom d'utilisateur Twitch (optionnel)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        val isFormValid = email.isNotBlank() && 
                password.isNotBlank() && 
                password == confirmPassword &&
                password.length >= 6 &&
                firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                gamertag.isNotBlank() &&
                gamertag.length >= 2 &&
                gamertag.length <= 30

        Button(
            onClick = {
                hasAttemptedSubmit = true
                if (isFormValid) {
                    viewModel.signUp(
                        email = email,
                        password = password,
                        firstName = firstName,
                        lastName = lastName,
                        gamertag = gamertag,
                        twitchUsername = twitchUsername.takeIf { it.isNotBlank() }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is AuthUiState.Loading
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("S'inscrire")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToSignIn) {
            Text("Déjà un compte ? Se connecter")
        }

        if (uiState is AuthUiState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (uiState as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

