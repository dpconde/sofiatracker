package com.dpconde.sofiatracker.core.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: com.dpconde.sofiatracker.core.data.sync.SyncManager,
    private val networkManager: com.dpconde.sofiatracker.core.network.utils.NetworkConnectivityManager
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // Check if network is available
            if (!networkManager.isNetworkAvailable()) {
                return Result.retry()
            }
            
            // Perform sync
            val syncResult = syncManager.performFullSync().first { result ->
                result is com.dpconde.sofiatracker.core.data.sync.SyncResult.Success || result is com.dpconde.sofiatracker.core.data.sync.SyncResult.Error
            }

            when (syncResult) {
                is com.dpconde.sofiatracker.core.data.sync.SyncResult.Success -> Result.success()
                is com.dpconde.sofiatracker.core.data.sync.SyncResult.Error -> Result.retry()
                else -> Result.retry() //TODO: refactor. Not reachable option
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    companion object {
        const val WORK_NAME = "sync_events_work"
    }
}