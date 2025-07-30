package com.dpconde.sofiatracker.domain.repository

import com.dpconde.sofiatracker.domain.model.Event
import com.dpconde.sofiatracker.domain.model.EventType
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    suspend fun insertEvent(event: Event)
    fun getAllEvents(): Flow<List<Event>>
    fun getEventsByType(type: EventType): Flow<List<Event>>
    fun getLastTwoEventsByType(type: EventType): Flow<List<Event>>
}