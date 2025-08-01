package com.dpconde.sofiatracker.data.remote.dto

import com.dpconde.sofiatracker.domain.model.Event
import com.dpconde.sofiatracker.domain.model.EventType
import com.dpconde.sofiatracker.domain.model.SyncStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class RemoteEventDto(
    val id: String = "",
    val localId: Long = 0,
    val type: String = "",
    val timestamp: String = "",
    val note: String = "",
    val version: Int = 1,
    val lastModified: Long = System.currentTimeMillis(),
    // Event type specific fields
    val bottleAmountMl: Int? = null, // For EAT events
    val sleepType: String? = null, // For SLEEP events (SLEEP or WAKE_UP)
    val diaperType: String? = null // For POOP events (WET, DIRTY, BOTH)
)

fun Event.toRemoteDto(): RemoteEventDto {
    // Extract type-specific information
    val extractedSleepType = if (type == EventType.SLEEP) {
        when {
            note.startsWith("Sleep") -> "SLEEP"
            note.startsWith("Wake up") -> "WAKE_UP"
            else -> "SLEEP" // Default fallback
        }
    } else null
    
    val extractedDiaperType = if (type == EventType.POOP) {
        when {
            note.contains("wet", ignoreCase = true) && note.contains("dirty", ignoreCase = true) -> "BOTH"
            note.contains("wet", ignoreCase = true) -> "WET"
            note.contains("dirty", ignoreCase = true) -> "DIRTY"
            else -> "DIRTY" // Default fallback for poop events
        }
    } else null
    
    return RemoteEventDto(
        id = remoteId ?: "",
        localId = id,
        type = type.name,
        timestamp = timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        note = note,
        version = version,
        lastModified = lastSyncAttempt?.atZone(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis(),
        bottleAmountMl = bottleAmountMl,
        sleepType = extractedSleepType,
        diaperType = extractedDiaperType
    )
}

fun RemoteEventDto.toEvent(): Event {
    return Event(
        id = 0, // Let Room auto-generate the local ID
        type = EventType.valueOf(type),
        timestamp = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        note = note,
        bottleAmountMl = bottleAmountMl,
        syncStatus = SyncStatus.SYNCED,
        lastSyncAttempt = java.time.Instant.ofEpochMilli(lastModified).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
        remoteId = id,
        version = version
    )
}