package com.dpconde.sofiatracker.domain.usecase

import com.dpconde.sofiatracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBabyNameUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<String> {
        return settingsRepository.getBabyName()
    }
}