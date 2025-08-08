package com.dpconde.sofiatracker.core.domain

import com.dpconde.sofiatracker.core.model.Event
import com.dpconde.sofiatracker.core.data.repository.EventRepository
import javax.inject.Inject

class GetEventByIdUseCase @Inject constructor(
    private val repository: com.dpconde.sofiatracker.core.data.repository.EventRepository
) {
    suspend operator fun invoke(eventId: Long): Event? {
        return repository.getEventById(eventId)
    }
}