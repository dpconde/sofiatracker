package com.dpconde.sofiatracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dpconde.sofiatracker.data.local.entity.EventEntity
import com.dpconde.sofiatracker.domain.model.EventType
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    
    @Insert
    suspend fun insertEvent(event: EventEntity)
    
    @Query("SELECT * FROM events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE type = :type ORDER BY timestamp DESC")
    fun getEventsByType(type: EventType): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE type = :type ORDER BY timestamp DESC LIMIT 2")
    fun getLastTwoEventsByType(type: EventType): Flow<List<EventEntity>>
}