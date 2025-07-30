package com.dpconde.sofiatracker.domain.usecase

import com.dpconde.sofiatracker.domain.model.Event
import com.dpconde.sofiatracker.domain.repository.EventRepository
import javax.inject.Inject

class AddEventUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(event: Event) {
        repository.insertEvent(event)
    }
}