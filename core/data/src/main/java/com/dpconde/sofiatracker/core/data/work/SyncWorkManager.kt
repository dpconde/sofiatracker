package com.dpconde.sofiatracker.core.data.work

import androidx.work.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncWorkManager @Inject constructor(
    private val workManager: WorkManager
) {
    
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15, // 15 minutes
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = 5, // 5 minutes flex
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWork
        )
        
        // Also trigger an immediate sync on startup
        scheduleImmediateSync()
    }
    
    fun scheduleImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val immediateSync = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setInitialDelay(2, TimeUnit.SECONDS) // Small delay to allow app initialization
            .build()
        
        workManager.enqueueUniqueWork(
            "${SyncWorker.WORK_NAME}_startup",
            ExistingWorkPolicy.REPLACE,
            immediateSync
        )
    }
    
    fun schedulePendingSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncWork = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniqueWork(
            "${SyncWorker.WORK_NAME}_pending",
            ExistingWorkPolicy.REPLACE,
            syncWork
        )
    }
    
    fun cancelAllSyncWork() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME)
        workManager.cancelUniqueWork("${SyncWorker.WORK_NAME}_pending")
        workManager.cancelUniqueWork("${SyncWorker.WORK_NAME}_startup")
    }
    
    fun getSyncWorkInfo() = workManager.getWorkInfosForUniqueWorkLiveData(SyncWorker.WORK_NAME)
}