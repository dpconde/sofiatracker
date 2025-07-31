package com.dpconde.sofiatracker.presentation.addevent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dpconde.sofiatracker.domain.model.EventType
import com.dpconde.sofiatracker.ui.theme.SofiaTrackerTheme
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class PoopType {
    WET, DIRTY, BOTH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPoopScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var poopType by remember { mutableStateOf(PoopType.DIRTY) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Set the event type when the screen loads
    LaunchedEffect(Unit) {
        viewModel.setEventType(EventType.POOP)
    }
    
    LaunchedEffect(uiState.eventAdded) {
        if (uiState.eventAdded) {
            viewModel.clearEventAdded()
            onNavigateBack()
        }
    }
    
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Diaper Change") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Diaper Change Event Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "💩",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Column {
                        Text(
                            text = "Diaper Change",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Record diaper changes and types",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Diaper Type Selection
            Text(
                text = "Diaper Type",
                style = MaterialTheme.typography.titleMedium
            )
            
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PoopType.entries.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = poopType == type,
                                onClick = { poopType = type },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = poopType == type,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (type) {
                                PoopType.WET -> "💧 Wet diaper only"
                                PoopType.DIRTY -> "💩 Dirty diaper"
                                PoopType.BOTH -> "🔄 Both wet and dirty"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // Time Picker Section
            Text(
                text = "Change Time",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showTimePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Selected time:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Notes Section
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::updateNote,
                label = { Text("Note (optional)") },
                placeholder = { 
                    Text(
                        when (poopType) {
                            PoopType.WET -> "Any leaks? Diaper brand?"
                            PoopType.DIRTY -> "Consistency? Color? Any concerns?"
                            PoopType.BOTH -> "How was the cleanup? Any issues?"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Add Event Button
            Button(
                onClick = {
                    // Create event with selected time
                    val eventDateTime = LocalDateTime.now()
                        .withHour(selectedTime.hour)
                        .withMinute(selectedTime.minute)
                        .withSecond(0)
                        .withNano(0)
                    
                    // Add diaper type to note if not already included
                    val finalNote = if (uiState.note.isBlank()) {
                        when (poopType) {
                            PoopType.WET -> "Wet diaper"
                            PoopType.DIRTY -> "Dirty diaper"
                            PoopType.BOTH -> "Wet and dirty diaper"
                        }
                    } else {
                        "${when (poopType) {
                            PoopType.WET -> "Wet"
                            PoopType.DIRTY -> "Dirty"
                            PoopType.BOTH -> "Wet & Dirty"
                        }}: ${uiState.note}"
                    }
                    
                    viewModel.setCustomTimestamp(eventDateTime)
                    viewModel.updateNote(finalNote)
                    viewModel.addEvent()
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Add Diaper Change")
                }
            }
        }
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialTime = selectedTime,
            onTimeSelected = { time ->
                selectedTime = time
                showTimePicker = false
            },
            onDismiss = {
                showTimePicker = false
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddPoopScreenPreview() {
    SofiaTrackerTheme {
        AddPoopScreen(
            onNavigateBack = {}
        )
    }
}