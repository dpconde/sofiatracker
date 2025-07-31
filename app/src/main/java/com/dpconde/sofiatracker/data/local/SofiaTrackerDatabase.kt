package com.dpconde.sofiatracker.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.dpconde.sofiatracker.data.local.dao.EventDao
import com.dpconde.sofiatracker.data.local.dao.SyncStateDao
import com.dpconde.sofiatracker.data.local.entity.EventEntity
import com.dpconde.sofiatracker.data.local.entity.SyncStateEntity
import com.dpconde.sofiatracker.data.local.migrations.MIGRATION_1_2
import com.dpconde.sofiatracker.data.local.migrations.MIGRATION_2_3

@Database(
    entities = [EventEntity::class, SyncStateEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SofiaTrackerDatabase : RoomDatabase() {
    
    abstract fun eventDao(): EventDao
    abstract fun syncStateDao(): SyncStateDao
    
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}