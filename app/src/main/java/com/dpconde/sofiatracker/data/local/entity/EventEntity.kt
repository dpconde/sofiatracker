package com.dpconde.sofiatracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dpconde.sofiatracker.domain.model.Event
import com.dpconde.sofiatracker.domain.model.EventType
import java.time.LocalDateTime

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: EventType,
    val timestamp: LocalDateTime,
    val note: String = ""
)

fun EventEntity.toDomain(): Event {
    return Event(
        id = id,
        type = type,
        timestamp = timestamp,
        note = note
    )
}

fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        type = type,
        timestamp = timestamp,
        note = note
    )
}