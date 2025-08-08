package com.dpconde.sofiatracker.feature.home.addevent

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
import com.dpconde.sofiatracker.core.model.EventType
import com.dpconde.sofiatracker.core.designsystem.theme.SofiaTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    eventType: EventType,
    onNavigateBack: () -> Unit,
    viewModel: AddEventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Set the event type when the screen loads
    LaunchedEffect(eventType) {
        viewModel.setEventType(eventType)
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
                title = { Text("Add ${getEventDisplayName(eventType)}") },
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = getEventColor(eventType)
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
                        text = getEventIcon(eventType),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Column {
                        Text(
                            text = getEventDisplayName(eventType),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = getEventDescription(eventType),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::updateNote,
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = viewModel::addEvent,
                enabled = uiState.selectedEventType != null && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Add ${getEventDisplayName(eventType)}")
                }
            }
        }
    }
}

@Composable
fun EventTypeSelector(
    selectedEventType: EventType?,
    onEventTypeSelected: (EventType) -> Unit
) {
    Column(
        modifier = Modifier.selectableGroup()
    ) {
        EventType.entries.forEach { eventType ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedEventType == eventType,
                        onClick = { onEventTypeSelected(eventType) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedEventType == eventType,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = eventType.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddEventScreenPreview() {
    SofiaTrackerTheme {
        AddEventScreen(
            eventType = EventType.SLEEP,
            onNavigateBack = {}
        )
    }
}

private fun getEventDisplayName(eventType: EventType): String {
    return when (eventType) {
        EventType.SLEEP -> "Sleep"
        EventType.EAT -> "Feeding"
        EventType.POOP -> "Diaper Change"
    }
}

private fun getEventIcon(eventType: EventType): String {
    return when (eventType) {
        EventType.SLEEP -> "😴"
        EventType.EAT -> "🍼"
        EventType.POOP -> "💩"
    }
}

private fun getEventDescription(eventType: EventType): String {
    return when (eventType) {
        EventType.SLEEP -> "Record a sleep session"
        EventType.EAT -> "Record a feeding session"
        EventType.POOP -> "Record a diaper change"
    }
}

@Composable
private fun getEventColor(eventType: EventType): androidx.compose.ui.graphics.Color {
    return when (eventType) {
        EventType.SLEEP -> MaterialTheme.colorScheme.primaryContainer
        EventType.EAT -> MaterialTheme.colorScheme.tertiaryContainer
        EventType.POOP -> MaterialTheme.colorScheme.secondaryContainer
    }
}

@Preview(showBackground = true)
@Composable
fun EventTypeSelectorPreview() {
    SofiaTrackerTheme {
        EventTypeSelector(
            selectedEventType = EventType.SLEEP,
            onEventTypeSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EventTypeSelectorEmptyPreview() {
    SofiaTrackerTheme {
        EventTypeSelector(
            selectedEventType = null,
            onEventTypeSelected = {}
        )
    }
}