package com.dpconde.sofiatracker.domain.usecase

import com.dpconde.sofiatracker.domain.model.Event
import com.dpconde.sofiatracker.domain.repository.EventRepository
import javax.inject.Inject

class GetEventByIdUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(eventId: Long): Event? {
        return repository.getEventById(eventId)
    }
}