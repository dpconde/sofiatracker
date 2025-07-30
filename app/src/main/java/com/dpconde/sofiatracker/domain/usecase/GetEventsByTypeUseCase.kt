package com.dpconde.sofiatracker.domain.usecase

import com.dpconde.sofiatracker.domain.model.Event
import com.dpconde.sofiatracker.domain.model.EventType
import com.dpconde.sofiatracker.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEventsByTypeUseCase @Inject constructor(
    private val repository: EventRepository
) {
    operator fun invoke(type: EventType): Flow<List<Event>> {
        return repository.getEventsByType(type)
    }
}