package com.dpconde.sofiatracker.data.repository

import com.dpconde.sofiatracker.data.local.dao.EventDao
import com.dpconde.sofiatracker.data.local.dao.SyncStateDao
import com.dpconde.sofiatracker.data.local.entity.SyncStateEntity
import com.dpconde.sofiatracker.data.local.entity.toDomain
import com.dpconde.sofiatracker.data.local.entity.toEntity
import com.dpconde.sofiatracker.data.network.NetworkConnectivityManager
import com.dpconde.sofiatracker.data.sync.SyncManager
import com.dpconde.sofiatracker.domain.model.Event
import com.dpconde.sofiatracker.domain.model.EventType
import com.dpconde.sofiatracker.domain.model.SyncStatus
import com.dpconde.sofiatracker.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
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
        eventDao.deleteEvent(event.toEntity())
        
        // Delete from remote if it was synced before
        if (event.remoteId != null && networkManager.isNetworkAvailable()) {
            // Note: This would need to be handled by the sync manager
            // For now, we'll just delete locally
        }
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
    
    override suspend fun syncAllEvents(): Flow<com.dpconde.sofiatracker.data.sync.SyncResult> {
        return syncManager.performFullSync()
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