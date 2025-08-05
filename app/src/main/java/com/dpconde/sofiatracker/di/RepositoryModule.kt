package com.dpconde.sofiatracker.di

import com.dpconde.sofiatracker.data.repository.EventRepositoryImpl
import com.dpconde.sofiatracker.data.repository.SettingsRepositoryImpl
import com.dpconde.sofiatracker.domain.repository.EventRepository
import com.dpconde.sofiatracker.domain.repository.SettingsRepository
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