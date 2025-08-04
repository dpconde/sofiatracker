package com.dpconde.sofiatracker.presentation.addevent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dpconde.sofiatracker.domain.model.EventType
import com.dpconde.sofiatracker.ui.theme.SofiaTrackerTheme
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEatScreen(
    onNavigateBack: () -> Unit,
    editEventId: Long? = null,
    viewModel: AddEventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val isEditMode = editEventId != null
    
    // Initialize the screen - either for adding or editing
    LaunchedEffect(editEventId) {
        if (editEventId != null) {
            viewModel.loadEventForEditing(editEventId)
        } else {
            viewModel.setEventType(EventType.EAT)
        }
    }
    
    // Update selectedTime when editing event is loaded
    LaunchedEffect(uiState.customTimestamp) {
        uiState.customTimestamp?.let { timestamp ->
            selectedTime = timestamp.toLocalTime()
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
            // Feeding Event Card
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
                        text = "🍼",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Column {
                        Text(
                            text = "Feeding",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Record a feeding session",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Time Picker Section
            Text(
                text = "Feeding Time",
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

            // Bottle Amount Section
            Text(
                text = "Bottle Amount Left",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Select amount remaining in bottle (ml)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        label = { 
                            Text("${amount}ml") 
                        },
                        selected = uiState.bottleAmountMl == amount,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                }
            }
            
            // Notes Section
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::updateNote,
                label = { Text("Note (optional)") },
                placeholder = { Text("How much? Breast/bottle? Any issues?") },
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
                        val eventDateTime = LocalDateTime.now()
                            .withHour(selectedTime.hour)
                            .withMinute(selectedTime.minute)
                            .withSecond(0)
                            .withNano(0)
                        
                        viewModel.setCustomTimestamp(eventDateTime)
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
                        Text("Add Feeding")
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
                Text("Are you sure you want to delete this feeding event? This action cannot be undone.") 
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
fun AddEatScreenPreview() {
    SofiaTrackerTheme {
        AddEatScreen(
            onNavigateBack = {}
        )
    }
}