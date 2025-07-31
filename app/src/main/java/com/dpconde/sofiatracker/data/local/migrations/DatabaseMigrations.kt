package com.dpconde.sofiatracker.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add sync fields to events table
        database.execSQL(
            """
            ALTER TABLE events ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING_SYNC'
            """
        )
        database.execSQL(
            """
            ALTER TABLE events ADD COLUMN lastSyncAttempt TEXT
            """
        )
        database.execSQL(
            """
            ALTER TABLE events ADD COLUMN remoteId TEXT
            """
        )
        database.execSQL(
            """
            ALTER TABLE events ADD COLUMN version INTEGER NOT NULL DEFAULT 1
            """
        )
        
        // Create sync_state table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS sync_state (
                id TEXT NOT NULL PRIMARY KEY,
                status TEXT NOT NULL,
                lastSyncAttempt TEXT,
                lastSuccessfulSync TEXT,
                errorMessage TEXT,
                pendingEventsCount INTEGER NOT NULL DEFAULT 0
            )
            """
        )
        
        // Insert initial sync state
        database.execSQL(
            """
            INSERT INTO sync_state (id, status, pendingEventsCount) 
            VALUES ('app_sync_state', 'PENDING_SYNC', 0)
            """
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add bottleAmountMl field to events table
        database.execSQL(
            """
            ALTER TABLE events ADD COLUMN bottleAmountMl INTEGER
            """
        )
    }
}