package com.dpconde.sofiatracker.core.domain

import com.dpconde.sofiatracker.core.model.Event
import com.dpconde.sofiatracker.core.data.repository.EventRepository
import javax.inject.Inject

class AddEventUseCase @Inject constructor(
    private val repository: com.dpconde.sofiatracker.core.data.repository.EventRepository
) {
    suspend operator fun invoke(event: Event) {
        repository.insertEvent(event)
    }
}