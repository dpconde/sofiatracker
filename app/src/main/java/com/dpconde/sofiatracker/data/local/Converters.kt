package com.dpconde.sofiatracker.data.local

import androidx.room.TypeConverter
import com.dpconde.sofiatracker.domain.model.EventType
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
}