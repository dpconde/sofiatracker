package com.dpconde.sofiatracker.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getBabyName(): Flow<String>
    suspend fun setBabyName(name: String)
}