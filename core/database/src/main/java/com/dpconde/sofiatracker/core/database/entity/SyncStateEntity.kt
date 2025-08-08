package com.dpconde.sofiatracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dpconde.sofiatracker.core.model.SyncStatus
import java.time.LocalDateTime

@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey
    val id: String = "app_sync_state", // Single row for app sync state
    val status: SyncStatus,
    val lastSyncAttempt: LocalDateTime?,
    val lastSuccessfulSync: LocalDateTime?,
    val errorMessage: String? = null,
    val pendingEventsCount: Int = 0
)