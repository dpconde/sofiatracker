package com.dpconde.sofiatracker.core.data.di

import com.dpconde.sofiatracker.core.data.repository.EventRepository
import com.dpconde.sofiatracker.core.data.repository.EventRepositoryImpl
import com.dpconde.sofiatracker.core.data.repository.SettingsRepository
import com.dpconde.sofiatracker.core.data.repository.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindEventRepository(
        eventRepositoryImpl: EventRepositoryImpl
    ): EventRepository
    
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}