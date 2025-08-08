package com.dpconde.sofiatracker.core.data.repository

import com.dpconde.sofiatracker.core.data.sync.SyncResult
import com.dpconde.sofiatracker.core.database.entity.SyncStateEntity
import com.dpconde.sofiatracker.core.model.Event
import com.dpconde.sofiatracker.core.model.EventType
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    suspend fun insertEvent(event: Event)
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(event: Event)
    suspend fun getEventById(eventId: Long): Event?
    fun getAllEvents(): Flow<List<Event>>
    fun getEventsByType(type: EventType): Flow<List<Event>>
    fun getLastTwoEventsByType(type: EventType): Flow<List<Event>>
    
    // Sync functionality
    suspend fun syncAllEvents(): Flow<SyncResult>
    fun getSyncState(): Flow<SyncStateEntity?>
    suspend fun getPendingSyncCount(): Int
    fun isNetworkAvailable(): Boolean
}