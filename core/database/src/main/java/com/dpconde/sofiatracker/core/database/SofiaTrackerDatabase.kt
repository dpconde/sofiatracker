package com.dpconde.sofiatracker.core.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context

@Database(
    entities = [com.dpconde.sofiatracker.core.database.entity.EventEntity::class, com.dpconde.sofiatracker.core.database.entity.SyncStateEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SofiaTrackerDatabase : RoomDatabase() {
    
    abstract fun eventDao(): com.dpconde.sofiatracker.core.database.dao.EventDao
    abstract fun syncStateDao(): com.dpconde.sofiatracker.core.database.dao.SyncStateDao
    
    companion object {
        @Volatile
        private var INSTANCE: SofiaTrackerDatabase? = null
        
        fun getDatabase(context: Context): SofiaTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SofiaTrackerDatabase::class.java,
                    "sofia_tracker_database"
                )
                .addMigrations(
                    com.dpconde.sofiatracker.core.database.migrations.MIGRATION_1_2,
                    com.dpconde.sofiatracker.core.database.migrations.MIGRATION_2_3
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}