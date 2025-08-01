package com.dpconde.sofiatracker.presentation.editevent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dpconde.sofiatracker.domain.model.Event
import com.dpconde.sofiatracker.domain.model.EventType
import com.dpconde.sofiatracker.domain.model.SyncStatus
import com.dpconde.sofiatracker.presentation.components.TimePickerDialog
import com.dpconde.sofiatracker.ui.theme.SofiaTrackerTheme
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class PoopType {
    WET, DIRTY, BOTH
}

enum class SleepType {
    SLEEP, WAKE_UP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    eventId: Long,
    onNavigateBack: () -> Unit,
    viewModel: EditEventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Load event when screen is first displayed
    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }
    
    // Handle navigation when event is saved or deleted
    LaunchedEffect(uiState.eventSaved) {
        if (uiState.eventSaved) {
            viewModel.clearEventSaved()
            onNavigateBack()
        }
    }
    
    LaunchedEffect(uiState.eventDeleted) {
        if (uiState.eventDeleted) {
            viewModel.clearEventDeleted()
            onNavigateBack()
        }
    }
    
    // Handle errors
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when (uiState.eventType) {
                            EventType.SLEEP -> "Edit Sleep Event"
                            EventType.EAT -> "Edit Feeding Event"
                            EventType.POOP -> "Edit Diaper Change"
                            null -> "Edit Event"
                        }
                    ) 
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        enabled = !uiState.isLoading && uiState.originalEvent != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete event",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.originalEvent == null) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Loading event...")
                }
            }
        } else if (uiState.originalEvent != null) {
            // Edit form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Event Type Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (uiState.eventType) {
                            EventType.SLEEP -> MaterialTheme.colorScheme.primaryContainer
                            EventType.EAT -> MaterialTheme.colorScheme.secondaryContainer
                            EventType.POOP -> MaterialTheme.colorScheme.tertiaryContainer
                            null -> MaterialTheme.colorScheme.surfaceContainer
                        }
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
                            text = when (uiState.eventType) {
                                EventType.SLEEP -> "😴"
                                EventType.EAT -> "🍼"
                                EventType.POOP -> "💩"
                                null -> "📝"
                            },
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Column {
                            Text(
                                text = when (uiState.eventType) {
                                    EventType.SLEEP -> "Sleep Event"
                                    EventType.EAT -> "Feeding Event"
                                    EventType.POOP -> "Diaper Change"
                                    null -> "Event"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Edit or delete this event",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Time Selection
                Card(
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null
                            )
                            Text(
                                text = "Event Time",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Text(
                            text = uiState.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Event-specific controls
                when (uiState.eventType) {
                    EventType.EAT -> {
                        // Bottle amount selection for EAT events
                        Text(
                            text = "Bottle Amount Left",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items((0..180 step 10).toList()) { amount ->
                                FilterChip(
                                    onClick = { 
                                        val newAmount = if (uiState.bottleAmountMl == amount) null else amount
                                        viewModel.updateBottleAmount(newAmount)
                                    },
                                    label = { Text("${amount}ml") },
                                    selected = uiState.bottleAmountMl == amount
                                )
                            }
                        }
                    }
                    
                    EventType.SLEEP -> {
                        // Sleep type selection for SLEEP events
                        Text(
                            text = "Sleep Type",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Column(
                            modifier = Modifier.selectableGroup(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SleepType.values().forEach { type ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = uiState.sleepType == type.name,
                                            onClick = { 
                                                val newType = if (uiState.sleepType == type.name) null else type.name
                                                viewModel.updateSleepType(newType)
                                            },
                                            role = Role.RadioButton
                                        )
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = uiState.sleepType == type.name,
                                        onClick = null // handled by selectable modifier
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (type) {
                                            SleepType.SLEEP -> "😴 Sleep"
                                            SleepType.WAKE_UP -> "🌅 Wake Up"
                                        },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                    
                    EventType.POOP -> {
                        // Diaper type selection for POOP events
                        Text(
                            text = "Diaper Type",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Column(
                            modifier = Modifier.selectableGroup(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PoopType.values().forEach { type ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = uiState.diaperType == type.name,
                                            onClick = { 
                                                val newType = if (uiState.diaperType == type.name) null else type.name
                                                viewModel.updateDiaperType(newType)
                                            },
                                            role = Role.RadioButton
                                        )
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = uiState.diaperType == type.name,
                                        onClick = null // handled by selectable modifier
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (type) {
                                            PoopType.WET -> "Wet Only"
                                            PoopType.DIRTY -> "Dirty Only"
                                            PoopType.BOTH -> "Wet & Dirty"
                                        },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                    
                    null -> { /* Handle null case */ }
                }
                
                // Notes Section
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = viewModel::updateNote,
                    label = { Text("Note (optional)") },
                    placeholder = { 
                        Text(
                            when (uiState.eventType) {
                                EventType.SLEEP -> "How was the sleep?"
                                EventType.EAT -> "How much was consumed?"
                                EventType.POOP -> "Any concerns or notes?"
                                null -> "Add a note..."
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Action Buttons
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
                        onClick = viewModel::saveEvent,
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
            }
        } else {
            // Error state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = uiState.error ?: "Event not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialTime = uiState.timestamp.toLocalTime(),
            onTimeSelected = { time ->
                val newTimestamp = uiState.timestamp
                    .withHour(time.hour)
                    .withMinute(time.minute)
                    .withSecond(0)
                    .withNano(0)
                viewModel.updateTimestamp(newTimestamp)
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
                Text("Are you sure you want to delete this event? This action cannot be undone.") 
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
fun EditEventScreenPreview() {
    SofiaTrackerTheme {
        EditEventScreen(
            eventId = 1L,
            onNavigateBack = {}
        )
    }
}