package com.dpconde.sofiatracker.data.repository

import com.dpconde.sofiatracker.data.local.dao.EventDao
import com.dpconde.sofiatracker.data.local.entity.toDomain
import com.dpconde.sofiatracker.data.local.entity.toEntity
import com.dpconde.sofiatracker.domain.model.Event
import com.dpconde.sofiatracker.domain.model.EventType
import com.dpconde.sofiatracker.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao
) : EventRepository {
    
    override suspend fun insertEvent(event: Event) {
        eventDao.insertEvent(event.toEntity())
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
}