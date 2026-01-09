package com.example.l4m_esports_mobile.ui.screens.events

import android.os.Build
import androidx.annotation.RequiresApi
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
import com.example.l4m_esports_mobile.ui.viewmodel.EventViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.EventUiState
import com.example.l4m_esports_mobile.ui.viewmodel.GameViewModel
import com.example.l4m_esports_mobile.util.localDateTimeToISO
import java.time.LocalDate
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    gameId: String,
    onEventCreated: () -> Unit,
    onBack: () -> Unit = {},
    eventViewModel: EventViewModel = hiltViewModel(),
    gameViewModel: GameViewModel = hiltViewModel()
) {
    val uiState by eventViewModel.uiState.collectAsState()
    val selectedGame by gameViewModel.selectedGame.collectAsState()
    
    // Variables pour les dates (déclarées une seule fois en haut)
    val today = LocalDate.now()
    val now = LocalDateTime.now()
    
    var name by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var endDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var registrationStartDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var registrationEndDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var format by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var rules by remember { mutableStateOf("") }
    var locationType by remember { mutableStateOf("online") }
    var address by remember { mutableStateOf("") }
    var maxTeams by remember { mutableStateOf("16") }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    // Formats disponibles pour le jeu
    val availableFormats = remember(selectedGame) {
        selectedGame?.formats ?: emptyList()
    }

    // Si un seul format, le sélectionner automatiquement
    LaunchedEffect(availableFormats) {
        if (availableFormats.size == 1) {
            format = availableFormats[0]
        }
    }

    LaunchedEffect(gameId) {
        gameViewModel.loadGameById(gameId)
    }

    LaunchedEffect(uiState) {
        if (uiState is EventUiState.Success) {
            onEventCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créer un événement") },
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
            // Nom de l'événement (obligatoire)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom de l'événement *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = hasAttemptedSubmit && name.isBlank(),
                supportingText = {
                    if (hasAttemptedSubmit && name.isBlank()) {
                        Text("Le nom est obligatoire")
                    }
                }
            )

            // Jeu (pré-rempli)
            OutlinedTextField(
                value = selectedGame?.name ?: "",
                onValueChange = {},
                label = { Text("Jeu") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )

            // Format (seulement si plusieurs formats disponibles)
            if (availableFormats.isEmpty()) {
                // Aucun format défini pour le jeu - afficher un message
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Aucun format défini pour ce jeu. Veuillez d'abord définir les formats dans les paramètres du jeu.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            } else if (availableFormats.size == 1) {
                // Un seul format - afficher en lecture seule
                OutlinedTextField(
                    value = format ?: availableFormats[0],
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Format") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text("Format unique pour ce jeu")
                    }
                )
            } else {
                // Plusieurs formats - afficher un dropdown
                var formatExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = formatExpanded,
                    onExpandedChange = { formatExpanded = !formatExpanded }
                ) {
                    OutlinedTextField(
                        value = format ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Format *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = hasAttemptedSubmit && format == null,
                        supportingText = {
                            if (hasAttemptedSubmit && format == null) {
                                Text("Le format est obligatoire")
                            }
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = formatExpanded,
                        onDismissRequest = { formatExpanded = false }
                    ) {
                        availableFormats.forEach { f ->
                            DropdownMenuItem(
                                text = { Text(f) },
                                onClick = {
                                    format = f
                                    formatExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Date de début (peut être aujourd'hui mais l'heure doit être dans le futur)
            val startDateError = remember(startDate, registrationEndDate, hasAttemptedSubmit, today, now) {
                when {
                    !hasAttemptedSubmit -> null
                    startDate == null -> "La date de début est obligatoire"
                    startDate!!.toLocalDate().isBefore(today) -> "La date de début ne peut pas être dans le passé"
                    startDate!!.toLocalDate() == today && startDate!!.toLocalTime() <= now.toLocalTime() -> "L'heure de début doit être dans le futur"
                    registrationEndDate != null && startDate!! <= registrationEndDate!! -> "La date de début doit être après la fin des inscriptions"
                    else -> null
                }
            }
            DateTimePickerField(
                label = "Date de début *",
                value = startDate,
                onValueChange = { startDate = it },
                modifier = Modifier.fillMaxWidth(),
                isError = startDateError != null,
                errorMessage = startDateError,
                minDate = today // Date minimale = aujourd'hui
            )

            // Date de fin (doit être après la date de début)
            val endDateError = remember(endDate, startDate, hasAttemptedSubmit) {
                when {
                    !hasAttemptedSubmit -> null
                    endDate == null -> "La date de fin est obligatoire"
                    startDate != null && endDate!! <= startDate!! -> "La date de fin doit être après la date de début"
                    else -> null
                }
            }
            val minEndDate = remember(startDate) {
                startDate?.toLocalDate() ?: today
            }
            DateTimePickerField(
                label = "Date de fin *",
                value = endDate,
                onValueChange = { endDate = it },
                modifier = Modifier.fillMaxWidth(),
                isError = endDateError != null,
                errorMessage = endDateError,
                minDate = minEndDate
            )

            // Date de début des inscriptions
            val registrationStartDateError = remember(registrationStartDate, hasAttemptedSubmit) {
                when {
                    !hasAttemptedSubmit -> null
                    registrationStartDate == null -> "La date de début des inscriptions est obligatoire"
                    else -> null
                }
            }
            DateTimePickerField(
                label = "Début inscriptions *",
                value = registrationStartDate,
                onValueChange = { registrationStartDate = it },
                modifier = Modifier.fillMaxWidth(),
                isError = registrationStartDateError != null,
                errorMessage = registrationStartDateError,
                minDate = today // Date minimale = aujourd'hui
            )

            // Date de fin des inscriptions (doit être après la date de début des inscriptions)
            val registrationEndDateError = remember(registrationEndDate, registrationStartDate, startDate, hasAttemptedSubmit) {
                when {
                    !hasAttemptedSubmit -> null
                    registrationEndDate == null -> "La date de fin des inscriptions est obligatoire"
                    registrationStartDate != null && registrationEndDate!! <= registrationStartDate!! -> "La date de fin des inscriptions doit être après la date de début"
                    startDate != null && registrationEndDate!! >= startDate!! -> "La date de fin des inscriptions doit être avant la date de début de l'événement"
                    else -> null
                }
            }
            val minRegistrationEndDate = remember(registrationStartDate) {
                registrationStartDate?.toLocalDate() ?: today
            }
            DateTimePickerField(
                label = "Fin inscriptions *",
                value = registrationEndDate,
                onValueChange = { registrationEndDate = it },
                modifier = Modifier.fillMaxWidth(),
                isError = registrationEndDateError != null,
                errorMessage = registrationEndDateError,
                minDate = minRegistrationEndDate
            )

            // Type de lieu
            val locationTypes = mapOf(
                "online" to "En ligne",
                "physical" to "Présentiel"
            )
            var locationTypeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = locationTypeExpanded,
                onExpandedChange = { locationTypeExpanded = !locationTypeExpanded }
            ) {
                OutlinedTextField(
                    value = locationTypes[locationType] ?: locationType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type de lieu") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationTypeExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = locationTypeExpanded,
                    onDismissRequest = { locationTypeExpanded = false }
                ) {
                    locationTypes.forEach { (type, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                locationType = type
                                locationTypeExpanded = false
                            }
                        )
                    }
                }
            }

            // Adresse (si physical)
            if (locationType == "physical") {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Adresse (optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Nombre maximum d'équipes
            OutlinedTextField(
                value = maxTeams,
                onValueChange = {
                    if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                        maxTeams = it
                    }
                },
                label = { Text("Nombre maximum d'équipes") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = {
                    Text("Par défaut: 16")
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

            // Règles
            OutlinedTextField(
                value = rules,
                onValueChange = { rules = it },
                label = { Text("Règles (optionnel)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bouton de création
            val currentFormat = format
            
            // Validation complète du formulaire
            val isFormValid = name.isNotBlank() &&
                    startDate != null &&
                    endDate != null &&
                    registrationStartDate != null &&
                    registrationEndDate != null &&
                    currentFormat != null && currentFormat.isNotBlank() &&
                    // Validation des dates
                    !startDate!!.toLocalDate().isBefore(today) &&
                    (startDate!!.toLocalDate() != today || startDate!!.toLocalTime() > now.toLocalTime()) &&
                    endDate!! > startDate!! &&
                    registrationEndDate!! > registrationStartDate!! &&
                    startDate!! > registrationEndDate!!
            
            Button(
                onClick = {
                    hasAttemptedSubmit = true
                    
                    if (isFormValid && format != null) {
                        eventViewModel.createEvent(
                            name = name,
                            game = gameId,
                            startDate = localDateTimeToISO(startDate!!),
                            endDate = localDateTimeToISO(endDate!!),
                            registrationStartDate = localDateTimeToISO(registrationStartDate!!),
                            registrationEndDate = localDateTimeToISO(registrationEndDate!!),
                            format = format!!,
                            rules = rules.takeIf { it.isNotBlank() },
                            description = description.takeIf { it.isNotBlank() },
                            locationType = locationType,
                            address = address.takeIf { it.isNotBlank() },
                            latitude = null, // TODO: Ajouter support pour géolocalisation
                            longitude = null,
                            maxTeams = maxTeams.toIntOrNull()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is EventUiState.Loading
            ) {
                if (uiState is EventUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Créer l'événement")
                }
            }

            if (uiState is EventUiState.Error) {
                Text(
                    text = (uiState as EventUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

