package com.dpconde.sofiatracker.core.domain

import com.dpconde.sofiatracker.core.data.repository.SettingsRepository
import javax.inject.Inject

class GetBabyNameUseCase @Inject constructor(
    private val settingsRepository: com.dpconde.sofiatracker.core.data.repository.SettingsRepository
) {

    operator fun invoke() =  settingsRepository.getBabyName()

}