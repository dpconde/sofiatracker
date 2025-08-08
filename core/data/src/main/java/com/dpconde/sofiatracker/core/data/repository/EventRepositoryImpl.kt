package com.dpconde.sofiatracker.core.data.repository

import com.dpconde.sofiatracker.core.data.sync.SyncManager
import com.dpconde.sofiatracker.core.data.sync.SyncResult
import com.dpconde.sofiatracker.core.database.dao.EventDao
import com.dpconde.sofiatracker.core.database.dao.SyncStateDao
import com.dpconde.sofiatracker.core.database.entity.SyncStateEntity
import com.dpconde.sofiatracker.core.database.entity.toDomain
import com.dpconde.sofiatracker.core.database.entity.toEntity
import com.dpconde.sofiatracker.core.model.Event
import com.dpconde.sofiatracker.core.model.EventType
import com.dpconde.sofiatracker.core.model.SyncStatus
import com.dpconde.sofiatracker.core.network.utils.NetworkConnectivityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val syncStateDao: SyncStateDao,
    private val syncManager: SyncManager,
    private val networkManager: NetworkConnectivityManager
) : EventRepository {
    
    override suspend fun insertEvent(event: Event) {
        // Always insert locally first (offline-first approach)
        val eventWithSyncStatus = event.copy(
            syncStatus = SyncStatus.PENDING_SYNC
        )
        eventDao.insertEvent(eventWithSyncStatus.toEntity())
        
        // Try to sync immediately if network is available
        if (networkManager.isNetworkAvailable()) {
            val insertedEvents = eventDao.getEventsBySyncStatus(SyncStatus.PENDING_SYNC)
            val insertedEvent = insertedEvents.lastOrNull()
            insertedEvent?.let {
                syncManager.syncSingleEvent(it.id)
            }
        }
    }
    
    override suspend fun updateEvent(event: Event) {
        // Mark as pending sync and update locally
        val eventWithSyncStatus = event.copy(
            syncStatus = SyncStatus.PENDING_SYNC
        )
        eventDao.updateEvent(eventWithSyncStatus.toEntity())
        
        // Try to sync immediately if network is available
        if (networkManager.isNetworkAvailable()) {
            syncManager.syncSingleEvent(event.id)
        }
    }
    
    override suspend fun deleteEvent(event: Event) {
        // Delete from remote first if it was synced
        val remoteId = event.remoteId
        if (remoteId != null && networkManager.isNetworkAvailable()) {
            // Delete from remote via sync manager
            syncManager.deleteRemoteEvent(remoteId)
        }
        
        // Delete locally
        eventDao.deleteEvent(event.toEntity())
    }
    
    override suspend fun getEventById(eventId: Long): Event? {
        return eventDao.getEventById(eventId)?.toDomain()
    }
    
    override fun getAllEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getEventsByType(type: EventType): Flow<List<Event>> {
        return eventDao.getEventsByType(type).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getLastTwoEventsByType(type: EventType): Flow<List<Event>> {
        return eventDao.getLastTwoEventsByType(type).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun syncAllEvents() = if (networkManager.isNetworkAvailable()) {
        syncManager.performFullSync()
    } else {
        flowOf(SyncResult.Error(Exception("Network not available")))
    }

    
    override fun getSyncState(): Flow<SyncStateEntity?> {
        return syncManager.getSyncState()
    }
    
    override suspend fun getPendingSyncCount(): Int {
        return eventDao.getPendingSyncCount()
    }
    
    override fun isNetworkAvailable(): Boolean {
        return networkManager.isNetworkAvailable()
    }
}