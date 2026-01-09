package com.example.l4m_esports_mobile.ui.screens.events

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Composable pour sélectionner une date et une heure avec des pickers natifs
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    label: String,
    value: LocalDateTime?,
    onValueChange: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
    minDate: LocalDate? = null // Date minimale sélectionnable
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }

    // Initialiser avec la valeur actuelle si elle existe
    LaunchedEffect(value) {
        if (value != null) {
            selectedDate = value.toLocalDate()
            selectedTime = value.toLocalTime()
        }
    }

    // Formatter pour afficher la date et l'heure
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val displayValue = remember(selectedDate, selectedTime) {
        when {
            selectedDate != null && selectedTime != null -> {
                "${selectedDate!!.format(dateFormatter)} ${selectedTime!!.format(timeFormatter)}"
            }
            selectedDate != null -> selectedDate!!.format(dateFormatter)
            else -> ""
        }
    }

    // Permettre de modifier la date même si elle est déjà sélectionnée
    // On ouvre toujours le DatePicker en premier
    val openPicker: () -> Unit = {
        if (enabled) {
            showDatePicker = true
        }
    }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = openPicker),
            readOnly = true,
            enabled = enabled,
            isError = isError,
            trailingIcon = {
                IconButton(
                    onClick = openPicker,
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Sélectionner la date et l'heure"
                    )
                }
            },
            supportingText = {
                when {
                    isError && errorMessage != null -> {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                    supportingText != null -> {
                        Text(supportingText)
                    }
                    else -> {
                        Text("Appuyez sur le champ ou l'icône pour sélectionner la date et l'heure")
                    }
                }
            }
        )

        // Date Picker
        if (showDatePicker) {
            val selectableDates = if (minDate != null) {
                object : androidx.compose.material3.SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val selectedDate = java.time.Instant.ofEpochMilli(utcTimeMillis)
                            .atZone(java.time.ZoneId.of("America/Montreal"))
                            .toLocalDate()
                        return !selectedDate.isBefore(minDate)
                    }
                }
            } else {
                androidx.compose.material3.DatePickerDefaults.AllDates
            }
            
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate?.let {
                    it.atStartOfDay(java.time.ZoneId.of("America/Montreal")).toInstant().toEpochMilli()
                },
                selectableDates = selectableDates
            )
            
            Dialog(
                onDismissRequest = { showDatePicker = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // DatePicker avec plus d'espace
                        DatePicker(
                            state = datePickerState,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Boutons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { showDatePicker = false }
                            ) {
                                Text("Annuler")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val newDate = java.time.Instant.ofEpochMilli(millis)
                                            .atZone(java.time.ZoneId.systemDefault())
                                            .toLocalDate()
                                        
                                        // Vérifier que la date est valide (>= minDate)
                                        if (minDate == null || !newDate.isBefore(minDate)) {
                                            selectedDate = newDate
                                            showDatePicker = false
                                            // Toujours ouvrir le time picker après avoir sélectionné la date
                                            showTimePicker = true
                                        }
                                    }
                                }
                            ) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }

        // Time Picker
        if (showTimePicker && selectedDate != null) {
            val now = LocalDateTime.now()
            val isToday = selectedDate == now.toLocalDate()
            val currentTime = now.toLocalTime()
            
            val timePickerState = rememberTimePickerState(
                initialHour = selectedTime?.hour ?: if (isToday) currentTime.hour else LocalTime.now().hour,
                initialMinute = selectedTime?.minute ?: if (isToday) currentTime.minute + 1 else LocalTime.now().minute,
                is24Hour = true
            )

            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val newTime = LocalTime.of(
                                timePickerState.hour,
                                timePickerState.minute
                            )
                            
                            // Si la date sélectionnée est aujourd'hui, vérifier que l'heure est dans le futur
                            if (isToday && newTime <= currentTime) {
                                // Ne pas permettre de sélectionner une heure passée pour aujourd'hui
                                return@TextButton
                            }
                            
                            selectedTime = newTime
                            showTimePicker = false
                            // Mettre à jour la valeur complète
                            if (selectedDate != null) {
                                onValueChange(LocalDateTime.of(selectedDate!!, selectedTime!!))
                            }
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Annuler")
                    }
                },
                text = {
                    Column {
                        TimePicker(state = timePickerState)
                        if (isToday) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "L'heure doit être après ${currentTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    }
}


