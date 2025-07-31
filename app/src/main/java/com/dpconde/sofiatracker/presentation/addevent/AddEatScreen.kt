package com.dpconde.sofiatracker.presentation.addevent

import androidx.compose.foundation.layout.*
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
    viewModel: AddEventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Set the event type when the screen loads
    LaunchedEffect(Unit) {
        viewModel.setEventType(EventType.EAT)
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
                title = { Text("Add Feeding") },
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
            // Feeding Event Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Record a feeding session",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
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
            
            // Add Event Button
            Button(
                onClick = {
                    // Create event with selected time
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
fun AddEatScreenPreview() {
    SofiaTrackerTheme {
        AddEatScreen(
            onNavigateBack = {}
        )
    }
}