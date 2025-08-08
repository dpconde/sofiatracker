package com.dpconde.sofiatracker.core.database

import androidx.room.TypeConverter
import com.dpconde.sofiatracker.core.model.EventType
import com.dpconde.sofiatracker.core.model.SyncStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(formatter)
    }
    
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it, formatter) }
    }
    
    @TypeConverter
    fun fromEventType(eventType: EventType): String {
        return eventType.name
    }
    
    @TypeConverter
    fun toEventType(eventTypeString: String): EventType {
        return EventType.valueOf(eventTypeString)
    }
    
    @TypeConverter
    fun fromSyncStatus(syncStatus: SyncStatus): String {
        return syncStatus.name
    }
    
    @TypeConverter
    fun toSyncStatus(syncStatusString: String): SyncStatus {
        return SyncStatus.valueOf(syncStatusString)
    }
}