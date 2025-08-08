package com.dpconde.sofiatracker.core.model

import java.time.LocalDateTime

data class Event(
    val id: Long = 0,
    val type: EventType,
    val timestamp: LocalDateTime,
    val note: String = "",
    val bottleAmountMl: Int? = null, // For EAT events: amount left in bottle (0-180ml)
    val diaperType: String? = null, // For DIAPER events: type of diaper
    val sleepType: String? = null, // For SLEEP events: type of sleep
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val lastSyncAttempt: LocalDateTime? = null,
    val remoteId: String? = null,
    val version: Int = 1
)