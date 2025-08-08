package com.dpconde.sofiatracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dpconde.sofiatracker.core.model.Event
import com.dpconde.sofiatracker.core.model.EventType
import com.dpconde.sofiatracker.core.model.SyncStatus
import java.time.LocalDateTime

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: EventType,
    val timestamp: LocalDateTime,
    val note: String = "",
    val bottleAmountMl: Int? = null,
    val sleepType: String? = null,
    val diaperType: String? = null,
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
        bottleAmountMl = bottleAmountMl,
        sleepType = sleepType,
        diaperType = diaperType,
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
        sleepType = sleepType,
        diaperType = diaperType,
        note = note,
        bottleAmountMl = bottleAmountMl,
        syncStatus = syncStatus,
        lastSyncAttempt = lastSyncAttempt,
        remoteId = remoteId,
        version = version
    )
}