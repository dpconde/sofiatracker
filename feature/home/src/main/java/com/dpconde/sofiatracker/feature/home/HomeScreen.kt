package com.dpconde.sofiatracker.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dpconde.sofiatracker.core.model.Event
import com.dpconde.sofiatracker.core.model.EventType
import com.dpconde.sofiatracker.core.model.SyncStatus
import com.dpconde.sofiatracker.core.designsystem.theme.SofiaTrackerTheme
import com.dpconde.sofiatracker.core.ui.components.SyncStatusIndicator
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private fun getRelativeTime(eventTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val duration = Duration.between(eventTime, now)
    
    return when {
        duration.toDays() >= 1 -> eventTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        duration.toHours() >= 1 -> {
            val hours = duration.toHours()
            val minutes = duration.toMinutes() % 60
            if (minutes == 0L) "${hours}h ago" else "${hours}h ${minutes}' ago"
        }
        duration.toMinutes() >= 1 -> "${duration.toMinutes()}' ago"
        else -> "Now"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddEvent: (EventType) -> Unit,
    onNavigateToEditEvent: (Event) -> Unit,
    viewModel: MainScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Trigger sync when screen is resumed
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.triggerSync()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                    Text(
                        "Loading your data...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {

                item {
                    WelcomeHeader(babyName = uiState.babyName)
                }
                
                item {
                    SyncStatusCard(
                        syncState = uiState.syncState,
                        isNetworkAvailable = uiState.isNetworkAvailable,
                        onSyncClick = viewModel::triggerSync
                    )
                }

                item {
                    Text(
                        "Recent Activity",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                item {
                    EnhancedEventTypeSection(
                        title = "Sleep",
                        icon = "😴",
                        events = uiState.recentSleepEvents,
                        eventType = EventType.SLEEP,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        onContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onAddEvent = onNavigateToAddEvent,
                        onEventClick = onNavigateToEditEvent
                    )
                }
                
                item {
                    EnhancedEventTypeSection(
                        title = "Feeding",
                        icon = "🍼",
                        events = uiState.recentEatEvents,
                        eventType = EventType.EAT,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        onContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onAddEvent = onNavigateToAddEvent,
                        onEventClick = onNavigateToEditEvent
                    )
                }
                
                item {
                    EnhancedEventTypeSection(
                        title = "Diaper",
                        icon = "💩",
                        events = uiState.recentPoopEvents,
                        eventType = EventType.POOP,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        onContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onAddEvent = onNavigateToAddEvent,
                        onEventClick = onNavigateToEditEvent
                    )
                }
                
            }
        }
    }
}

@Composable
fun WelcomeHeader(babyName: String = "Sofía") {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👶",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Welcome back!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Track $babyName's daily activities",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SyncStatusCard(
    syncState: com.dpconde.sofiatracker.core.database.entity.SyncStateEntity?,
    isNetworkAvailable: Boolean,
    onSyncClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        SyncStatusIndicator(
            syncState = syncState,
            isNetworkAvailable = isNetworkAvailable,
            onSyncClick = onSyncClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EnhancedEventTypeSection(
    title: String,
    icon: String,
    events: List<Event>,
    eventType: EventType,
    containerColor: Color,
    onContentColor: Color,
    onAddEvent: (EventType) -> Unit,
    onEventClick: (Event) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = icon,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${events.size} recent events",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Button(
                    onClick = { onAddEvent(eventType) },

                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add $title",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Events List
            if (events.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No ${eventType.name.lowercase()} events yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    events.take(3).forEach { event ->
                        EnhancedEventItem(
                            event = event,
                            containerColor = containerColor,
                            onClick = { onEventClick(event) }
                        )
                    }
                    
                    if (events.size > 3) {
                        Text(
                            text = "... and ${events.size - 3} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedEventItem(
    event: Event,
    containerColor: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = getRelativeTime(event.timestamp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Show bottle amount for EAT events
                    if (event.type == EventType.EAT && event.bottleAmountMl != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${event.bottleAmountMl}ml left",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                    
                    // Show sleep type for SLEEP events
                    if (event.type == EventType.SLEEP && event.sleepType != null) {
                        val sleepType = when (event.sleepType) {
                            "SLEEP" -> "😴 Sleep"
                            "WAKE_UP" -> "🌅 Wake up"
                            else -> "😴 Sleep" // Default fallback
                        }
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = sleepType,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                    
                    // Show diaper type for POOP events
                    if (event.type == EventType.POOP && event.diaperType != null) {
                        val diaperType = when (event.diaperType) {
                            "WET" -> "💧 Wet"
                            "DIRTY" -> "💩 Dirty"
                            "BOTH" -> "💧💩 Both"
                            else -> "💩 Dirty" // Default fallback
                        }
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = diaperType,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                
                // Display note as-is since we no longer auto-populate with type info
                val displayNote = event.note
                
                if (displayNote.isNotBlank()) {
                    Text(
                        text = displayNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                
                Text(
                    text = "${event.timestamp.format(DateTimeFormatter.ofPattern("MMM dd"))} at ${event.timestamp.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            // Sync status indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        when (event.syncStatus) {
                            SyncStatus.SYNCED -> Color(0xFF4CAF50)
                            SyncStatus.PENDING_SYNC -> MaterialTheme.colorScheme.secondary
                            SyncStatus.SYNC_ERROR -> MaterialTheme.colorScheme.error
                            SyncStatus.SYNCING -> MaterialTheme.colorScheme.tertiary
                        },
                        CircleShape
                    )
            )
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
        HomeScreen(
            onNavigateToAddEvent = {},
            onNavigateToEditEvent = {}
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
                syncState = com.dpconde.sofiatracker.core.database.entity.SyncStateEntity(
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