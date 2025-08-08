package com.dpconde.sofiatracker.feature.home.addevent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dpconde.sofiatracker.core.model.EventType
import com.dpconde.sofiatracker.core.designsystem.theme.SofiaTrackerTheme
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
    editEventId: Long? = null,
    viewModel: AddEventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var poopType by remember { mutableStateOf(PoopType.DIRTY) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val isEditMode = editEventId != null
    
    // Initialize the screen - either for adding or editing
    LaunchedEffect(editEventId) {
        if (editEventId != null) {
            viewModel.loadEventForEditing(editEventId)
        } else {
            viewModel.setEventType(EventType.POOP)
        }
    }
    
    // Update selectedTime and poopType when editing event is loaded
    LaunchedEffect(uiState.customTimestamp, uiState.diaperType) {
        uiState.customTimestamp?.let { timestamp ->
            selectedTime = timestamp.toLocalTime()
        }
        uiState.diaperType?.let { type ->
            poopType = when (type) {
                "WET" -> PoopType.WET
                "DIRTY" -> PoopType.DIRTY
                "BOTH" -> PoopType.BOTH
                else -> PoopType.DIRTY
            }
        }
    }
    
    LaunchedEffect(uiState.eventAdded) {
        if (uiState.eventAdded) {
            viewModel.clearEventAdded()
            onNavigateBack()
        }
    }
    
    LaunchedEffect(uiState.eventDeleted) {
        if (uiState.eventDeleted) {
            viewModel.clearEventDeleted()
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
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back")
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
                            text = "Record diaper changes",
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
                                PoopType.WET -> "💧 Wet"
                                PoopType.DIRTY -> "💩 Dirty"
                                PoopType.BOTH -> "💧💩 Both"
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
            
            // Action Buttons
            if (isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteConfirmation = true },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Delete")
                        }
                    }
                    
                    Button(
                        onClick = {
                            val eventDateTime = LocalDateTime.now()
                                .withYear(uiState.customTimestamp?.year?: LocalDateTime.now().year)
                                .withMonth(uiState.customTimestamp?.monthValue?: LocalDateTime.now().monthValue)
                                .withDayOfMonth(uiState.customTimestamp?.dayOfMonth?: LocalDateTime.now().dayOfMonth)
                                .withHour(selectedTime.hour)
                                .withMinute(selectedTime.minute)
                                .withSecond(0)
                                .withNano(0)
                            
                            viewModel.setCustomTimestamp(eventDateTime)
                            viewModel.updateDiaperType(poopType.name)
                            viewModel.updateEvent()
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.weight(2f)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Save Changes")
                        }
                    }
                }
            } else {
                Button(
                    onClick = {
                        // Create event with selected time
                        val eventDateTime = LocalDateTime.now()
                            .withHour(selectedTime.hour)
                            .withMinute(selectedTime.minute)
                            .withSecond(0)
                            .withNano(0)
                        
                        // Set diaper type directly, don't modify note
                        viewModel.setCustomTimestamp(eventDateTime)
                        viewModel.updateDiaperType(poopType.name)
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
    
    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Event") },
            text = { 
                Text("Are you sure you want to delete this diaper change event? This action cannot be undone.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.deleteEvent()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
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