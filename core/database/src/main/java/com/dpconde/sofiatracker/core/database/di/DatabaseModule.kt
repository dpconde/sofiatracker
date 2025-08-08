package com.dpconde.sofiatracker.core.database.di

import android.content.Context
import androidx.room.Room
import com.dpconde.sofiatracker.core.database.SofiaTrackerDatabase
import com.dpconde.sofiatracker.core.database.dao.EventDao
import com.dpconde.sofiatracker.core.database.dao.SyncStateDao
import com.dpconde.sofiatracker.core.database.migrations.MIGRATION_1_2
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
        )
        .addMigrations(MIGRATION_1_2)
        .build()
    }
    
    @Provides
    fun provideEventDao(database: SofiaTrackerDatabase): EventDao {
        return database.eventDao()
    }
    
    @Provides
    fun provideSyncStateDao(database: SofiaTrackerDatabase): SyncStateDao {
        return database.syncStateDao()
    }

}