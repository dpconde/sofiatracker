package com.dpconde.sofiatracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dpconde.sofiatracker.domain.model.Event
import com.dpconde.sofiatracker.domain.model.EventType
import com.dpconde.sofiatracker.domain.model.SyncStatus
import java.time.LocalDateTime

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: EventType,
    val timestamp: LocalDateTime,
    val note: String = "",
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val lastSyncAttempt: LocalDateTime? = null,
    val remoteId: String? = null,
    val version: Int = 1
)

fun EventEntity.toDomain(): Event {
    return Event(
        id = id,
        type = type,
        timestamp = timestamp,
        note = note,
        syncStatus = syncStatus,
        lastSyncAttempt = lastSyncAttempt,
        remoteId = remoteId,
        version = version
    )
}

fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        type = type,
        timestamp = timestamp,
        note = note,
        syncStatus = syncStatus,
        lastSyncAttempt = lastSyncAttempt,
        remoteId = remoteId,
        version = version
    )
}