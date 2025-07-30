package com.dpconde.sofiatracker.data.local.dao

import androidx.room.*
import com.dpconde.sofiatracker.data.local.entity.SyncStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncStateDao {
    
    @Query("SELECT * FROM sync_state WHERE id = 'app_sync_state'")
    fun getSyncState(): Flow<SyncStateEntity?>
    
    @Query("SELECT * FROM sync_state WHERE id = 'app_sync_state'")
    suspend fun getSyncStateOnce(): SyncStateEntity?
    
    @Upsert
    suspend fun updateSyncState(syncState: SyncStateEntity)
    
    @Query("UPDATE sync_state SET pendingEventsCount = :count WHERE id = 'app_sync_state'")
    suspend fun updatePendingEventsCount(count: Int)
}