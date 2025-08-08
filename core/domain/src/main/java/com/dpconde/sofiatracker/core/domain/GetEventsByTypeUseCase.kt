package com.dpconde.sofiatracker.core.domain

import com.dpconde.sofiatracker.core.model.Event
import com.dpconde.sofiatracker.core.model.EventType
import com.dpconde.sofiatracker.core.data.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEventsByTypeUseCase @Inject constructor(
    private val repository: com.dpconde.sofiatracker.core.data.repository.EventRepository
) {
    operator fun invoke(type: EventType): Flow<List<Event>> {
        return repository.getEventsByType(type)
    }
}