package com.dpconde.sofiatracker.core.data.sync

import com.dpconde.sofiatracker.core.database.entity.toDomain
import com.dpconde.sofiatracker.core.database.entity.toEntity
import com.dpconde.sofiatracker.core.network.model.toEvent
import com.dpconde.sofiatracker.core.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val eventDao: com.dpconde.sofiatracker.core.database.dao.EventDao,
    private val syncStateDao: com.dpconde.sofiatracker.core.database.dao.SyncStateDao,
    private val remoteDataSource: com.dpconde.sofiatracker.core.network.firebase.RemoteEventDataSource,
    private val conflictResolutionStrategy: ConflictResolutionStrategy
) {
    
    fun performFullSync(): Flow<SyncResult> = flow {
        emit(SyncResult.InProgress)
        
        try {
            updateSyncState(SyncStatus.SYNCING, "Starting sync...")
            
            // Step 1: Upload pending local events
            uploadPendingEvents { progress ->
                emit(progress)
            }
            
            // Step 2: Download and process remote events
            downloadAndProcessRemoteEvents { progress ->
                emit(progress)
            }
            
            // Update sync state
            eventDao.getPendingSyncCount().let { count ->
                if(count == 0){ //No events have been created in the meantime
                    updateSyncState(
                        status = SyncStatus.SYNCED,
                        lastSuccessfulSync = LocalDateTime.now(),
                        pendingCount = count
                    )
                }
            }

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
    
    private suspend fun uploadPendingEvents(onProgress: suspend (SyncResult.Progress) -> Unit) {
        val pendingEvents = eventDao.getEventsBySyncStatus(SyncStatus.PENDING_SYNC)
        onProgress(SyncResult.Progress("Uploading ${pendingEvents.size} pending events"))
        
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
                throw Exception("Failed to upload pending event ${event.id}. Error: ${result.exceptionOrNull()?.message}")
            }
        }
    }
    
    private suspend fun downloadAndProcessRemoteEvents(onProgress: suspend (SyncResult.Progress) -> Unit) {
        val lastSync = syncStateDao.getSyncStateOnce()?.lastSuccessfulSync
        val lastSyncTimestamp = lastSync?.atZone(java.time.ZoneId.systemDefault())?.toInstant()
            ?.toEpochMilli()
            ?: 0L
        
        onProgress(SyncResult.Progress("Downloading remote changes since $lastSyncTimestamp"))
        
        val remoteResult = remoteDataSource.getEventsModifiedAfter(lastSyncTimestamp)
        if (remoteResult.isSuccess) {
            val remoteEvents = remoteResult.getOrThrow()
            onProgress(SyncResult.Progress("Found ${remoteEvents.size} remote events to process"))
            
            for (remoteEvent in remoteEvents) {
                // Check if remote event is marked as deleted
                if (remoteEvent.deleted) {
                    // Handle deleted event
                    val existingLocal = eventDao.getEventsBySyncStatus(SyncStatus.SYNCED)
                        .find { it.remoteId == remoteEvent.id }
                    
                    if (existingLocal != null) {
                        onProgress(SyncResult.Progress("Deleting remote-deleted event ${remoteEvent.id}"))
                        eventDao.deleteEvent(existingLocal)
                    }
                    continue
                }
                
                // Check if we have a local version
                val existingLocal = eventDao.getEventsBySyncStatus(SyncStatus.SYNCED)
                    .find { it.remoteId == remoteEvent.id }
                
                if (existingLocal == null) {
                    // New remote event - insert locally
                    onProgress(SyncResult.Progress("Inserting new remote event ${remoteEvent.id}"))
                    val localEvent = remoteEvent.toEvent()
                    eventDao.insertEvent(localEvent.toEntity())
                } else {
                    // Check for conflicts and resolve them
                    if (conflictResolutionStrategy.hasConflict(existingLocal, remoteEvent)) {
                        onProgress(SyncResult.Progress("Resolving conflict for event ${existingLocal.id}"))
                        
                        val resolution = conflictResolutionStrategy.resolveConflict(
                            localEvent = existingLocal,
                            remoteEvent = remoteEvent,
                            policy = ConflictResolutionPolicy.REMOTE_WINS
                        )
                        
                        // Update with resolved version
                        eventDao.updateEvent(resolution.resolvedEvent)
                        
                        // Log the conflict resolution
                        onProgress(SyncResult.Progress("Conflict resolved: ${resolution.conflictReason}"))
                    } else {
                        // No conflict detected, update with remote version
                        onProgress(SyncResult.Progress("Updating existing event ${remoteEvent.id}"))
                        val localEvent = remoteEvent.toEvent()
                        eventDao.updateEvent(localEvent.copy(id = existingLocal.id).toEntity())
                    }
                }
            }
        } else {
            throw remoteResult.exceptionOrNull() ?: Exception("Failed to download remote events")
        }
    }
    
    private suspend fun updateSyncState(
        status: SyncStatus,
        message: String? = null,
        lastSuccessfulSync: LocalDateTime? = null,
        pendingCount: Int? = null
    ) {
        val currentState = syncStateDao.getSyncStateOnce()
        val newState = com.dpconde.sofiatracker.core.database.entity.SyncStateEntity(
            status = status,
            lastSyncAttempt = LocalDateTime.now(),
            lastSuccessfulSync = lastSuccessfulSync ?: currentState?.lastSuccessfulSync,
            errorMessage = message,
            pendingEventsCount = pendingCount ?: eventDao.getPendingSyncCount()
        )
        syncStateDao.updateSyncState(newState)
    }
    
    fun getSyncState(): Flow<com.dpconde.sofiatracker.core.database.entity.SyncStateEntity?> = syncStateDao.getSyncState()
}

sealed class SyncResult {
    data object InProgress : SyncResult()
    data class Progress(val message: String) : SyncResult()
    data class Success(val message: String) : SyncResult()
    data class Error(val exception: Exception) : SyncResult()
}