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
    val lastModified: Long = System.currentTimeMillis()
)

fun Event.toRemoteDto(): RemoteEventDto {
    return RemoteEventDto(
        id = remoteId ?: "",
        localId = id,
        type = type.name,
        timestamp = timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        note = note,
        version = version,
        lastModified = System.currentTimeMillis()
    )
}

fun RemoteEventDto.toEvent(): Event {
    return Event(
        id = localId,
        type = EventType.valueOf(type),
        timestamp = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        note = note,
        syncStatus = SyncStatus.SYNCED,
        lastSyncAttempt = LocalDateTime.now(),
        remoteId = id,
        version = version
    )
}