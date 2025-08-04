package com.dpconde.sofiatracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dpconde.sofiatracker.data.local.entity.SyncStateEntity
import com.dpconde.sofiatracker.domain.model.SyncStatus
import com.dpconde.sofiatracker.ui.theme.SofiaTrackerTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SyncStatusIndicator(
    syncState: SyncStateEntity?,
    isNetworkAvailable: Boolean,
    onSyncClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (icon, color, text, description) = when {
        !isNetworkAvailable -> SyncIndicatorData(
            icon = Icons.Default.Info,
            color = MaterialTheme.colorScheme.outline,
            text = "Offline",
            description = "No network connection"
        )
        syncState == null -> SyncIndicatorData(
            icon = Icons.Default.Refresh,
            color = MaterialTheme.colorScheme.primary,
            text = "Initializing",
            description = "Setting up sync"
        )
        syncState.status == SyncStatus.SYNCED && syncState.pendingEventsCount == 0 -> SyncIndicatorData(
            icon = Icons.Default.CheckCircle,
            color = Color(0xFF4CAF50),
            text = "Synced",
            description = "All data synchronized"
        )
        syncState.status == SyncStatus.SYNCING -> SyncIndicatorData(
            icon = Icons.Default.Refresh,
            color = MaterialTheme.colorScheme.primary,
            text = "Syncing",
            description = "Synchronizing data..."
        )
        syncState.status == SyncStatus.PENDING_SYNC -> SyncIndicatorData(
            icon = Icons.Default.Refresh,
            color = MaterialTheme.colorScheme.secondary,
            text = "Pending",
            description = "${syncState.pendingEventsCount} events pending"
        )
        syncState.status == SyncStatus.SYNC_ERROR -> SyncIndicatorData(
            icon = Icons.Default.Warning,
            color = MaterialTheme.colorScheme.error,
            text = "Error",
            description = syncState.errorMessage ?: "Sync failed"
        )
        else -> SyncIndicatorData(
            icon = Icons.Default.Refresh,
            color = MaterialTheme.colorScheme.outline,
            text = "Unknown",
            description = "Unknown sync state"
        )
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        onClick = if (syncState?.status != SyncStatus.SYNCING) onSyncClick else { {} }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {

                if (syncState?.pendingEventsCount ?: 0 > 0) {
                    Text(
                        text = "${syncState?.pendingEventsCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = color
                    )
                }

                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )

            }
            
            Column {
                Row {

                    Text(
                        text = "$text - ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                syncState?.lastSuccessfulSync?.let { lastSync ->
                    Text(
                        text = "Last sync: ${lastSync.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CompactSyncStatusIndicator(
    syncState: SyncStateEntity?,
    isNetworkAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    val (icon, color, _, _) = when {
        !isNetworkAvailable -> SyncIndicatorData(
            icon = Icons.Default.Info,
            color = MaterialTheme.colorScheme.outline,
            text = "Offline",
            description = ""
        )
        syncState == null -> SyncIndicatorData(
            icon = Icons.Default.Refresh,
            color = MaterialTheme.colorScheme.primary,
            text = "Initializing",
            description = ""
        )
        syncState.status == SyncStatus.SYNCED && syncState.pendingEventsCount == 0 -> SyncIndicatorData(
            icon = Icons.Default.CheckCircle,
            color = Color(0xFF4CAF50),
            text = "Synced",
            description = ""
        )
        syncState.status == SyncStatus.SYNCING -> SyncIndicatorData(
            icon = Icons.Default.Refresh,
            color = MaterialTheme.colorScheme.primary,
            text = "Syncing",
            description = ""
        )
        syncState.status == SyncStatus.PENDING_SYNC -> SyncIndicatorData(
            icon = Icons.Default.Refresh,
            color = MaterialTheme.colorScheme.secondary,
            text = "Pending",
            description = ""
        )
        else -> SyncIndicatorData(
            icon = Icons.Default.Warning,
            color = MaterialTheme.colorScheme.error,
            text = "Error",
            description = ""
        )
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        
        if (syncState?.pendingEventsCount ?: 0 > 0) {
            Text(
                text = "${syncState?.pendingEventsCount}",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

private data class SyncIndicatorData(
    val icon: ImageVector,
    val color: Color,
    val text: String,
    val description: String
)

@Preview(showBackground = true)
@Composable
fun SyncStatusIndicatorPreview() {
    SofiaTrackerTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SyncStatusIndicator(
                syncState = SyncStateEntity(
                    status = SyncStatus.SYNCED,
                    lastSyncAttempt = LocalDateTime.now(),
                    lastSuccessfulSync = LocalDateTime.now().minusMinutes(5),
                    pendingEventsCount = 0
                ),
                isNetworkAvailable = true
            )
            
            SyncStatusIndicator(
                syncState = SyncStateEntity(
                    status = SyncStatus.PENDING_SYNC,
                    lastSyncAttempt = LocalDateTime.now(),
                    lastSuccessfulSync = LocalDateTime.now().minusMinutes(10),
                    pendingEventsCount = 3
                ),
                isNetworkAvailable = true
            )
            
            SyncStatusIndicator(
                syncState = SyncStateEntity(
                    status = SyncStatus.SYNC_ERROR,
                    lastSyncAttempt = LocalDateTime.now(),
                    lastSuccessfulSync = LocalDateTime.now().minusHours(1),
                    errorMessage = "Network timeout",
                    pendingEventsCount = 5
                ),
                isNetworkAvailable = false
            )
        }
    }
}