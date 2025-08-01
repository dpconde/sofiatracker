package com.dpconde.sofiatracker.data.sync

import com.dpconde.sofiatracker.data.local.dao.EventDao
import com.dpconde.sofiatracker.data.local.dao.SyncStateDao
import com.dpconde.sofiatracker.data.local.entity.SyncStateEntity
import com.dpconde.sofiatracker.data.local.entity.toDomain
import com.dpconde.sofiatracker.data.local.entity.toEntity
import com.dpconde.sofiatracker.data.remote.RemoteEventDataSource
import com.dpconde.sofiatracker.data.remote.dto.toEvent
import com.dpconde.sofiatracker.data.remote.dto.toRemoteDto
import com.dpconde.sofiatracker.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val eventDao: EventDao,
    private val syncStateDao: SyncStateDao,
    private val remoteDataSource: RemoteEventDataSource,
    private val conflictResolutionStrategy: ConflictResolutionStrategy
) {
    
    suspend fun performFullSync(): Flow<SyncResult> = flow {
        emit(SyncResult.InProgress)
        
        try {
            updateSyncState(SyncStatus.SYNCING, "Starting sync...")
            
            // Step 1: Upload pending local events
            val pendingEvents = eventDao.getEventsBySyncStatus(SyncStatus.PENDING_SYNC)
            emit(SyncResult.Progress("Uploading ${pendingEvents.size} pending events"))
            
            for (event in pendingEvents) {
                val result = remoteDataSource.saveEvent(event.toDomain())
                if (result.isSuccess) {
                    val remoteId = result.getOrThrow()
                    eventDao.updateSyncStatusWithRemoteId(
                        event.id, 
                        SyncStatus.SYNCED, 
                        remoteId
                    )
                } else {
                    eventDao.updateSyncStatus(event.id, SyncStatus.SYNC_ERROR)
                }
            }
            
            // Step 2: Get remote events modified since last sync
            val lastSync = syncStateDao.getSyncStateOnce()?.lastSuccessfulSync
            val lastSyncTimestamp = lastSync?.let { 
                it.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            } ?: 0L
            
            emit(SyncResult.Progress("Downloading remote changes since $lastSyncTimestamp"))
            
            val remoteResult = remoteDataSource.getEventsModifiedAfter(lastSyncTimestamp)
            if (remoteResult.isSuccess) {
                val remoteEvents = remoteResult.getOrThrow()
                emit(SyncResult.Progress("Found ${remoteEvents.size} remote events to process"))
                
                for (remoteEvent in remoteEvents) {
                    // Check if remote event is marked as deleted
                    if (remoteEvent.deleted) {
                        // Handle deleted event
                        val existingLocal = eventDao.getEventsBySyncStatus(SyncStatus.SYNCED)
                            .find { it.remoteId == remoteEvent.id }
                        
                        if (existingLocal != null) {
                            emit(SyncResult.Progress("Deleting remote-deleted event ${remoteEvent.id}"))
                            eventDao.deleteEvent(existingLocal)
                        }
                        continue
                    }
                    
                    // Check if we have a local version
                    val existingLocal = eventDao.getEventsBySyncStatus(SyncStatus.SYNCED)
                        .find { it.remoteId == remoteEvent.id }
                    
                    if (existingLocal == null) {
                        // New remote event - insert locally
                        emit(SyncResult.Progress("Inserting new remote event ${remoteEvent.id}"))
                        val localEvent = remoteEvent.toEvent()
                        eventDao.insertEvent(localEvent.toEntity())
                    } else {
                        // Check for conflicts and resolve them
                        if (conflictResolutionStrategy.hasConflict(existingLocal, remoteEvent)) {
                            emit(SyncResult.Progress("Resolving conflict for event ${existingLocal.id}"))
                            
                            val resolution = conflictResolutionStrategy.resolveConflict(
                                localEvent = existingLocal,
                                remoteEvent = remoteEvent,
                                policy = ConflictResolutionPolicy.REMOTE_WINS
                            )
                            
                            // Update with resolved version
                            eventDao.updateEvent(resolution.resolvedEvent)
                            
                            // Log the conflict resolution
                            emit(SyncResult.Progress("Conflict resolved: ${resolution.conflictReason}"))
                        } else {
                            // No conflict detected, update with remote version
                            emit(SyncResult.Progress("Updating existing event ${remoteEvent.id}"))
                            val localEvent = remoteEvent.toEvent()
                            eventDao.updateEvent(localEvent.copy(id = existingLocal.id).toEntity())
                        }
                    }
                }
            } else {
                throw remoteResult.exceptionOrNull() ?: Exception("Failed to download remote events")
            }
            
            // Update sync state
            val pendingCount = eventDao.getPendingSyncCount()
            updateSyncState(
                status = if (pendingCount == 0) SyncStatus.SYNCED else SyncStatus.PENDING_SYNC,
                message = "Sync completed successfully",
                lastSuccessfulSync = LocalDateTime.now(),
                pendingCount = pendingCount
            )
            
            emit(SyncResult.Success("Sync completed successfully"))
            
        } catch (e: Exception) {
            updateSyncState(
                SyncStatus.SYNC_ERROR, 
                "Sync failed: ${e.message}"
            )
            emit(SyncResult.Error(e))
        }
    }
    
    suspend fun syncSingleEvent(eventId: Long): Result<Unit> {
        return try {
            val localEvents = eventDao.getEventsBySyncStatus(SyncStatus.PENDING_SYNC)
            val event = localEvents.find { it.id == eventId }
                ?: return Result.failure(Exception("Event not found"))
            
            eventDao.updateSyncStatusWithTime(
                eventId, 
                SyncStatus.SYNCING, 
                LocalDateTime.now()
            )
            
            val result = remoteDataSource.saveEvent(event.toDomain())
            if (result.isSuccess) {
                val remoteId = result.getOrThrow()
                eventDao.updateSyncStatusWithRemoteId(eventId, SyncStatus.SYNCED, remoteId)
                Result.success(Unit)
            } else {
                eventDao.updateSyncStatus(eventId, SyncStatus.SYNC_ERROR)
                Result.failure(result.exceptionOrNull() ?: Exception("Sync failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteRemoteEvent(remoteId: String): Result<Unit> {
        return try {
            // Soft delete: mark as deleted in remote instead of hard delete
            val getResult = remoteDataSource.getEvent(remoteId)
            if (getResult.isSuccess) {
                val remoteEvent = getResult.getOrThrow()
                val deletedEvent = remoteEvent.copy(
                    deleted = true,
                    lastModified = System.currentTimeMillis()
                )
                // Use the new saveRemoteEvent to update with deleted flag
                val saveResult = remoteDataSource.saveRemoteEvent(deletedEvent)
                if (saveResult.isSuccess) {
                    Result.success(Unit)
                } else {
                    Result.failure(saveResult.exceptionOrNull() ?: Exception("Failed to mark event as deleted"))
                }
            } else {
                Result.failure(getResult.exceptionOrNull() ?: Exception("Event not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun updateSyncState(
        status: SyncStatus,
        message: String? = null,
        lastSuccessfulSync: LocalDateTime? = null,
        pendingCount: Int? = null
    ) {
        val currentState = syncStateDao.getSyncStateOnce()
        val newState = SyncStateEntity(
            status = status,
            lastSyncAttempt = LocalDateTime.now(),
            lastSuccessfulSync = lastSuccessfulSync ?: currentState?.lastSuccessfulSync,
            errorMessage = message,
            pendingEventsCount = pendingCount ?: eventDao.getPendingSyncCount()
        )
        syncStateDao.updateSyncState(newState)
    }
    
    fun getSyncState(): Flow<SyncStateEntity?> = syncStateDao.getSyncState()
}

sealed class SyncResult {
    object InProgress : SyncResult()
    data class Progress(val message: String) : SyncResult()
    data class Success(val message: String) : SyncResult()
    data class Error(val exception: Exception) : SyncResult()
}