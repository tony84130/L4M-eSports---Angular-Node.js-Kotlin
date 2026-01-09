package com.example.l4m_esports_mobile.ui.screens.support

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.l4m_esports_mobile.data.model.request.AiRequestContext
import com.example.l4m_esports_mobile.ui.viewmodel.AiUiState
import com.example.l4m_esports_mobile.ui.viewmodel.AiViewModel

@Composable
fun AiHelpWidget(
    currentPage: String? = null,
    userRole: String? = null,
    viewModel: AiViewModel = hiltViewModel()
) {
    var isOpen by remember { mutableStateOf(false) }
    var questionText by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val answer by viewModel.answer.collectAsState()

    // Widget flottant - Positionné en bas à gauche, aligné horizontalement avec les FloatingActionButtons
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(start = 16.dp, bottom = 16.dp)
        ) {
            // Bouton flottant
            FloatingActionButton(
                onClick = { isOpen = !isOpen },
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = if (isOpen) Icons.Default.Close else Icons.Default.QuestionAnswer,
                    contentDescription = if (isOpen) "Fermer l'assistant" else "Ouvrir l'assistant"
                )
            }

            // Widget d'aide
            AnimatedVisibility(
                visible = isOpen,
                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Assistant IA",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = questionText,
                            onValueChange = { questionText = it },
                            label = { Text("Posez votre question") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState !is AiUiState.Loading,
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val context = AiRequestContext(
                                    page = currentPage,
                                    role = userRole
                                )
                                viewModel.askQuestion(questionText, context)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = questionText.trim().isNotBlank() && uiState !is AiUiState.Loading
                        ) {
                            if (uiState is AiUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Poser la question")
                            }
                        }

                        // Affichage de la réponse
                        answer?.let { ans ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = ans,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // Affichage des erreurs
                        if (uiState is AiUiState.Error) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = (uiState as AiUiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Bouton pour fermer/réinitialiser
                        if (answer != null || uiState is AiUiState.Error) {
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(
                                onClick = {
                                    questionText = ""
                                    viewModel.clearState()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Nouvelle question")
                            }
                        }
                    }
                }
            }
        }
    }
}

