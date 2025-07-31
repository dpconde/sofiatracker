package com.dpconde.sofiatracker.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    onNavigateToAddEvent: (EventType) -> Unit,
    viewModel: MainScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Sofia Tracker",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    CompactSyncStatusIndicator(
                        syncState = uiState.syncState,
                        isNetworkAvailable = uiState.isNetworkAvailable,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { paddingValues ->
        if (uiState.isLoading) {
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    WelcomeHeader()
                }

                item {
                    StatisticsRow(
                        sleepCount = uiState.recentSleepEvents.size,
                        eatCount = uiState.recentEatEvents.size,
                        poopCount = uiState.recentPoopEvents.size
                    )
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
                        onAddEvent = onNavigateToAddEvent
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
                        onAddEvent = onNavigateToAddEvent
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
                        onAddEvent = onNavigateToAddEvent
                    )
                }
                
            }
        }
    }
}

@Composable
fun WelcomeHeader() {
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
                            text = "Track Sofia's daily activities",
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
fun StatisticsRow(
    sleepCount: Int,
    eatCount: Int,
    poopCount: Int
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            StatisticCard(
                title = "Sleep",
                count = sleepCount,
                icon = "😴",
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }
        item {
            StatisticCard(
                title = "Feeding",
                count = eatCount,
                icon = "🍼",
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }
        item {
            StatisticCard(
                title = "Diaper",
                count = poopCount,
                icon = "💩",
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

@Composable
fun StatisticCard(
    title: String,
    count: Int,
    icon: String,
    color: Color
) {
    Card(
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun SyncStatusCard(
    syncState: SyncStateEntity?,
    isNetworkAvailable: Boolean,
    onSyncClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
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
    onAddEvent: (EventType) -> Unit
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
                            .background(containerColor, CircleShape),
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = containerColor,
                        contentColor = onContentColor
                    ),
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
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
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
                            containerColor = containerColor
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
    containerColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
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
                        text = event.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Show bottle amount for EAT events
                    if (event.type == EventType.EAT && event.bottleAmountMl != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${event.bottleAmountMl}ml left",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    // Show sleep type for SLEEP events
                    if (event.type == EventType.SLEEP) {
                        val sleepType = when {
                            event.note.startsWith("Sleep") -> "😴 Sleep"
                            event.note.startsWith("Wake up") -> "🌅 Wake up"
                            else -> "😴 Sleep" // Default fallback
                        }
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = sleepType,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                // Display note, filtering out sleep type prefixes for SLEEP events
                val displayNote = if (event.type == EventType.SLEEP) {
                    when {
                        event.note.startsWith("Sleep: ") -> event.note.removePrefix("Sleep: ")
                        event.note.startsWith("Wake up: ") -> event.note.removePrefix("Wake up: ")
                        event.note in listOf("Sleep event", "Wake up event") -> "" // Hide default notes
                        else -> event.note
                    }
                } else {
                    event.note
                }
                
                if (displayNote.isNotBlank()) {
                    Text(
                        text = displayNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                
                Text(
                    text = event.timestamp.format(DateTimeFormatter.ofPattern("MMM dd")),
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
                            SyncStatus.SYNCED -> MaterialTheme.colorScheme.primary
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