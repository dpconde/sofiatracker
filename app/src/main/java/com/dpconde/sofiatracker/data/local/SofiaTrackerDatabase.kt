package com.dpconde.sofiatracker.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.dpconde.sofiatracker.data.local.dao.EventDao
import com.dpconde.sofiatracker.data.local.entity.EventEntity

@Database(
    entities = [EventEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SofiaTrackerDatabase : RoomDatabase() {
    
    abstract fun eventDao(): EventDao
    
    companion object {
        @Volatile
        private var INSTANCE: SofiaTrackerDatabase? = null
        
        fun getDatabase(context: Context): SofiaTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SofiaTrackerDatabase::class.java,
                    "sofia_tracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}