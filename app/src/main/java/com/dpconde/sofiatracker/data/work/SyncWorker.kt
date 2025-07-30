package com.dpconde.sofiatracker.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dpconde.sofiatracker.data.network.NetworkConnectivityManager
import com.dpconde.sofiatracker.data.sync.SyncManager
import com.dpconde.sofiatracker.data.sync.SyncResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager,
    private val networkManager: NetworkConnectivityManager
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // Check if network is available
            if (!networkManager.isNetworkAvailable()) {
                return Result.retry()
            }
            
            // Perform sync
            val syncResult = syncManager.performFullSync().first { result ->
                result is SyncResult.Success || result is SyncResult.Error
            }
            
            when (syncResult) {
                is SyncResult.Success -> {
                    Result.success()
                }
                is SyncResult.Error -> {
                    // Retry on error, but with exponential backoff
                    Result.retry()
                }
                else -> Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    companion object {
        const val WORK_NAME = "sync_events_work"
    }
}