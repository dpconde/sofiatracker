package com.dpconde.sofiatracker.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.dpconde.sofiatracker.core.database.entity.EventEntity
import com.dpconde.sofiatracker.core.model.EventType
import com.dpconde.sofiatracker.core.model.SyncStatus
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
    
    // Sync-related queries
    @Query("SELECT * FROM events WHERE syncStatus = :status")
    suspend fun getEventsBySyncStatus(status: SyncStatus): List<EventEntity>
    
    @Query("SELECT COUNT(*) FROM events WHERE syncStatus = 'PENDING_SYNC'")
    suspend fun getPendingSyncCount(): Int
    
    @Query("UPDATE events SET syncStatus = :status WHERE id = :eventId")
    suspend fun updateSyncStatus(eventId: Long, status: SyncStatus)
    
    @Query("UPDATE events SET syncStatus = :status, lastSyncAttempt = :syncTime WHERE id = :eventId")
    suspend fun updateSyncStatusWithTime(eventId: Long, status: SyncStatus, syncTime: java.time.LocalDateTime)
    
    @Query("UPDATE events SET syncStatus = :status, remoteId = :remoteId WHERE id = :eventId")
    suspend fun updateSyncStatusWithRemoteId(eventId: Long, status: SyncStatus, remoteId: String)
    
    @Upsert
    suspend fun upsertEvent(event: EventEntity)
    
    @Update
    suspend fun updateEvent(event: EventEntity)
    
    @Delete
    suspend fun deleteEvent(event: EventEntity)
    
    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: Long): EventEntity?
}