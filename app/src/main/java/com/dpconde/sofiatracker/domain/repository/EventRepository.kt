package com.dpconde.sofiatracker.domain.repository

import com.dpconde.sofiatracker.data.local.entity.SyncStateEntity
import com.dpconde.sofiatracker.data.sync.SyncResult
import com.dpconde.sofiatracker.domain.model.Event
import com.dpconde.sofiatracker.domain.model.EventType
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    suspend fun insertEvent(event: Event)
    fun getAllEvents(): Flow<List<Event>>
    fun getEventsByType(type: EventType): Flow<List<Event>>
    fun getLastTwoEventsByType(type: EventType): Flow<List<Event>>
    
    // Sync functionality
    suspend fun syncAllEvents(): Flow<SyncResult>
    fun getSyncState(): Flow<SyncStateEntity?>
    suspend fun getPendingSyncCount(): Int
    fun isNetworkAvailable(): Boolean
}