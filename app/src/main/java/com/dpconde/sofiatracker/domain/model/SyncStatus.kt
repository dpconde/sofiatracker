package com.dpconde.sofiatracker.domain.model

enum class SyncStatus {
    SYNCED,           // Successfully synced with remote
    PENDING_SYNC,     // Needs to be synced to remote
    SYNCING,          // Currently being synced
    SYNC_ERROR        // Failed to sync
}