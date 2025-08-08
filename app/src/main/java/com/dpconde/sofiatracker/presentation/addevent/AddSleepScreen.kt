package com.dpconde.sofiatracker.presentation.addevent

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
import com.dpconde.sofiatracker.domain.model.EventType
import com.dpconde.sofiatracker.core.designsystem.theme.SofiaTrackerTheme
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class SleepEventType {
    SLEEP, WAKE_UP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSleepScreen(
    onNavigateBack: () -> Unit,
    editEventId: Long? = null,
    viewModel: AddEventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var sleepEventType by remember { mutableStateOf(SleepEventType.SLEEP) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val isEditMode = editEventId != null
    
    // Initialize the screen - either for adding or editing
    LaunchedEffect(editEventId) {
        if (editEventId != null) {
            viewModel.loadEventForEditing(editEventId)
        } else {
            viewModel.setEventType(EventType.SLEEP)
        }
    }
    
    // Update selectedTime and sleepEventType when editing event is loaded
    LaunchedEffect(uiState.customTimestamp, uiState.sleepType) {
        uiState.customTimestamp?.let { timestamp ->
            selectedTime = timestamp.toLocalTime()
        }
        uiState.sleepType?.let { type ->
            sleepEventType = when (type) {
                "SLEEP" -> SleepEventType.SLEEP
                "WAKE_UP" -> SleepEventType.WAKE_UP
                else -> SleepEventType.SLEEP
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
            // Sleep Event Type Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
                        text = "😴",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Column {
                        Text(
                            text = "Sleep Event",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Record sleep and wake-up times",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Sleep/Wake Up Button Group
            Text(
                text = "Event Type",
                style = MaterialTheme.typography.titleMedium
            )

            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SleepEventType.entries.forEach { type ->
                    val isSelected = sleepEventType == type
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = { sleepEventType = type },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (type) {
                                SleepEventType.SLEEP -> "😴 Sleep"
                                SleepEventType.WAKE_UP -> "🌅 Wake Up"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // Time Picker Section
            Text(
                text = "Time",
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
                        when (sleepEventType) {
                            SleepEventType.SLEEP -> "How was the sleep preparation? Any issues?"
                            SleepEventType.WAKE_UP -> "How did they wake up? Well rested?"
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
                            viewModel.updateSleepType(sleepEventType.name)
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
                        
                        // Set sleep type directly, don't modify note
                        viewModel.setCustomTimestamp(eventDateTime)
                        viewModel.updateSleepType(sleepEventType.name)
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
                        Text(
                            "Add ${when (sleepEventType) {
                                SleepEventType.SLEEP -> "Sleep"
                                SleepEventType.WAKE_UP -> "Wake Up"
                            }} Event"
                        )
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
                Text("Are you sure you want to delete this sleep event? This action cannot be undone.") 
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Time")
        },
        text = {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.padding(16.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(
                        LocalTime.of(
                            timePickerState.hour,
                            timePickerState.minute
                        )
                    )
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AddSleepScreenPreview() {
    SofiaTrackerTheme {
        AddSleepScreen(
            onNavigateBack = {}
        )
    }
}