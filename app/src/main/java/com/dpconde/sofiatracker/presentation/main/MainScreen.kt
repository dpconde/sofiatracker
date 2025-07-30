package com.dpconde.sofiatracker.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dpconde.sofiatracker.data.local.entity.SyncStateEntity
import com.dpconde.sofiatracker.domain.model.Event
import com.dpconde.sofiatracker.domain.model.EventType
import com.dpconde.sofiatracker.domain.model.SyncStatus
import com.dpconde.sofiatracker.presentation.components.CompactSyncStatusIndicator
import com.dpconde.sofiatracker.presentation.components.SyncStatusIndicator
import com.dpconde.sofiatracker.ui.theme.SofiaTrackerTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToAddEvent: () -> Unit,
    viewModel: MainScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sofia Tracker") },
                actions = {
                    CompactSyncStatusIndicator(
                        syncState = uiState.syncState,
                        isNetworkAvailable = uiState.isNetworkAvailable,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddEvent
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SyncStatusIndicator(
                        syncState = uiState.syncState,
                        isNetworkAvailable = uiState.isNetworkAvailable,
                        onSyncClick = viewModel::triggerSync,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    EventTypeSection(
                        title = "Recent Sleep Events",
                        events = uiState.recentSleepEvents,
                        eventType = EventType.SLEEP
                    )
                }
                
                item {
                    EventTypeSection(
                        title = "Recent Eat Events",
                        events = uiState.recentEatEvents,
                        eventType = EventType.EAT
                    )
                }
            }
        }
    }
}

@Composable
fun EventTypeSection(
    title: String,
    events: List<Event>,
    eventType: EventType
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (events.isEmpty()) {
                Text(
                    text = "No ${eventType.name.lowercase()} events recorded yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                events.forEach { event ->
                    EventItem(event = event)
                    if (event != events.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EventItem(event: Event) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = event.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            if (event.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    SofiaTrackerTheme {
        MainScreen(
            onNavigateToAddEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EventTypeSectionPreview() {
    SofiaTrackerTheme {
        EventTypeSection(
            title = "Recent Sleep Events",
            events = listOf(
                Event(
                    id = 1,
                    type = EventType.SLEEP,
                    timestamp = LocalDateTime.now().minusHours(2),
                    note = "Good night sleep",
                    syncStatus = SyncStatus.SYNCED
                ),
                Event(
                    id = 2,
                    type = EventType.SLEEP,
                    timestamp = LocalDateTime.now().minusHours(8),
                    note = "",
                    syncStatus = SyncStatus.PENDING_SYNC
                )
            ),
            eventType = EventType.SLEEP
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EventTypeSectionEmptyPreview() {
    SofiaTrackerTheme {
        EventTypeSection(
            title = "Recent Eat Events",
            events = emptyList(),
            eventType = EventType.EAT
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EventItemPreview() {
    SofiaTrackerTheme {
        EventItem(
            event = Event(
                id = 1,
                type = EventType.SLEEP,
                timestamp = LocalDateTime.now(),
                note = "Had a great nap!",
                syncStatus = SyncStatus.SYNCED
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EventItemNoNotePreview() {
    SofiaTrackerTheme {
        EventItem(
            event = Event(
                id = 2,
                type = EventType.EAT,
                timestamp = LocalDateTime.now().minusMinutes(30),
                note = "",
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenWithSyncPreview() {
    SofiaTrackerTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SyncStatusIndicator(
                syncState = SyncStateEntity(
                    status = SyncStatus.SYNCED,
                    lastSyncAttempt = LocalDateTime.now(),
                    lastSuccessfulSync = LocalDateTime.now().minusMinutes(5),
                    pendingEventsCount = 0
                ),
                isNetworkAvailable = true,
                onSyncClick = {}
            )
            
            EventTypeSection(
                title = "Recent Sleep Events",
                events = listOf(
                    Event(
                        id = 1,
                        type = EventType.SLEEP,
                        timestamp = LocalDateTime.now().minusHours(2),
                        note = "Good night sleep",
                        syncStatus = SyncStatus.SYNCED
                    )
                ),
                eventType = EventType.SLEEP
            )
        }
    }
}