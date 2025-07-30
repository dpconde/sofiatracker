package com.dpconde.sofiatracker.di

import android.content.Context
import androidx.room.Room
import com.dpconde.sofiatracker.data.local.SofiaTrackerDatabase
import com.dpconde.sofiatracker.data.local.dao.EventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SofiaTrackerDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            SofiaTrackerDatabase::class.java,
            "sofia_tracker_database"
        ).build()
    }
    
    @Provides
    fun provideEventDao(database: SofiaTrackerDatabase): EventDao {
        return database.eventDao()
    }
}