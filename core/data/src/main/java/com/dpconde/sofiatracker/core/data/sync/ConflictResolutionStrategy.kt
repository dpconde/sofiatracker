package com.dpconde.sofiatracker.core.data.sync

import com.dpconde.sofiatracker.core.model.EventType
import com.dpconde.sofiatracker.core.model.SyncStatus
import com.dpconde.sofiatracker.core.database.entity.EventEntity
import com.dpconde.sofiatracker.core.network.model.RemoteEventDto
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

enum class ConflictResolutionPolicy {
    LOCAL_WINS,        // Local version always takes precedence
    REMOTE_WINS,       // Remote version always takes precedence
    LATEST_TIMESTAMP,  // Version with latest timestamp wins
    MERGE_STRATEGY,    // Intelligent merge based on content
    USER_CHOICE        // Prompt user to choose (for future implementation)
}

data class ConflictResolution(
    val policy: ConflictResolutionPolicy,
    val resolvedEvent: EventEntity,
    val conflictReason: String
)

@Singleton
class ConflictResolutionStrategy @Inject constructor() {
    
    fun resolveConflict(
        localEvent: EventEntity,
        remoteEvent: RemoteEventDto,
        policy: ConflictResolutionPolicy = ConflictResolutionPolicy.LATEST_TIMESTAMP
    ): ConflictResolution {
        
        return when (policy) {
            ConflictResolutionPolicy.LOCAL_WINS -> {
                ConflictResolution(
                    policy = policy,
                    resolvedEvent = localEvent,
                    conflictReason = "Local version preserved by policy"
                )
            }
            
            ConflictResolutionPolicy.REMOTE_WINS -> {
                ConflictResolution(
                    policy = policy,
                    resolvedEvent = remoteEvent.toEventEntity(localEvent.id),
                    conflictReason = "Remote version accepted by policy"
                )
            }
            
            ConflictResolutionPolicy.LATEST_TIMESTAMP -> {
                resolveByTimestamp(localEvent, remoteEvent)
            }
            
            ConflictResolutionPolicy.MERGE_STRATEGY -> {
                resolveBySmartMerge(localEvent, remoteEvent)
            }
            
            ConflictResolutionPolicy.USER_CHOICE -> {
                // For now, fall back to timestamp-based resolution
                // TODO: Implement user choice UI
                resolveByTimestamp(localEvent, remoteEvent).copy(
                    conflictReason = "User choice not implemented, using timestamp resolution"
                )
            }
        }
    }
    
    private fun resolveByTimestamp(
        localEvent: com.dpconde.sofiatracker.core.database.entity.EventEntity,
        remoteEvent: com.dpconde.sofiatracker.core.network.model.RemoteEventDto
    ): ConflictResolution {
        
        val localTimestamp = localEvent.lastSyncAttempt ?: localEvent.timestamp
        val remoteTimestamp = LocalDateTime.ofEpochSecond(
            remoteEvent.lastModified / 1000, 
            ((remoteEvent.lastModified % 1000) * 1_000_000).toInt(),
            java.time.ZoneOffset.UTC
        )
        
        return if (localTimestamp.isAfter(remoteTimestamp)) {
            ConflictResolution(
                policy = ConflictResolutionPolicy.LATEST_TIMESTAMP,
                resolvedEvent = localEvent,
                conflictReason = "Local version is more recent (${localTimestamp} > ${remoteTimestamp})"
            )
        } else {
            ConflictResolution(
                policy = ConflictResolutionPolicy.LATEST_TIMESTAMP,
                resolvedEvent = remoteEvent.toEventEntity(localEvent.id),
                conflictReason = "Remote version is more recent (${remoteTimestamp} >= ${localTimestamp})"
            )
        }
    }
    
    private fun resolveBySmartMerge(
        localEvent: com.dpconde.sofiatracker.core.database.entity.EventEntity,
        remoteEvent: com.dpconde.sofiatracker.core.network.model.RemoteEventDto
    ): ConflictResolution {
        
        // Smart merge strategy: combine the best of both versions
        val mergedEvent = when {
            // If only local has a note, keep local
            localEvent.note.isNotBlank() && remoteEvent.note.isBlank() -> {
                localEvent.copy(
                    version = maxOf(localEvent.version, remoteEvent.version) + 1
                )
            }
            
            // If only remote has a note, use remote but keep local ID
            localEvent.note.isBlank() && remoteEvent.note.isNotBlank() -> {
                remoteEvent.toEventEntity(localEvent.id).copy(
                    version = maxOf(localEvent.version, remoteEvent.version) + 1
                )
            }
            
            // If both have notes, prefer the longer/more detailed one
            localEvent.note.isNotBlank() && remoteEvent.note.isNotBlank() -> {
                if (localEvent.note.length >= remoteEvent.note.length) {
                    localEvent.copy(
                        version = maxOf(localEvent.version, remoteEvent.version) + 1
                    )
                } else {
                    remoteEvent.toEventEntity(localEvent.id).copy(
                        version = maxOf(localEvent.version, remoteEvent.version) + 1
                    )
                }
            }
            
            // Default case: use timestamp resolution
            else -> {
                return resolveByTimestamp(localEvent, remoteEvent).copy(
                    policy = ConflictResolutionPolicy.MERGE_STRATEGY,
                    conflictReason = "Smart merge fell back to timestamp resolution"
                )
            }
        }
        
        return ConflictResolution(
            policy = ConflictResolutionPolicy.MERGE_STRATEGY,
            resolvedEvent = mergedEvent,
            conflictReason = "Smart merge: combined best attributes from both versions"
        )
    }
    
    fun hasConflict(localEvent: com.dpconde.sofiatracker.core.database.entity.EventEntity, remoteEvent: com.dpconde.sofiatracker.core.network.model.RemoteEventDto): Boolean {
        // Check if there's a meaningful difference that constitutes a conflict
        return when {
            // Different versions indicate potential conflict
            localEvent.version != remoteEvent.version -> true
            
            // Different notes indicate content conflict
            localEvent.note != remoteEvent.note -> true
            
            // Different timestamps beyond tolerance (1 second) indicate conflict
            kotlin.math.abs(
                localEvent.timestamp.toEpochSecond(java.time.ZoneOffset.UTC) * 1000 - 
                kotlin.run {
                    val remoteDateTime = LocalDateTime.parse(
                        remoteEvent.timestamp, 
                        java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    )
                    remoteDateTime.toEpochSecond(java.time.ZoneOffset.UTC) * 1000
                }
            ) > 1000 -> true
            
            else -> false
        }
    }
}

// Extension function to convert RemoteEventDto to EventEntity
private fun com.dpconde.sofiatracker.core.network.model.RemoteEventDto.toEventEntity(localId: Long): com.dpconde.sofiatracker.core.database.entity.EventEntity {
    return EventEntity(
        id = localId,
        type = EventType.valueOf(this.type),
        timestamp = LocalDateTime.parse(
            this.timestamp, 
            java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
        ),
        sleepType = this.sleepType,
        diaperType = this.diaperType,
        bottleAmountMl = this.bottleAmountMl,
        note = this.note,
        syncStatus = SyncStatus.SYNCED,
        lastSyncAttempt = LocalDateTime.now(),
        remoteId = this.id,
        version = this.version
    )
}