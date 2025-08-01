package com.dpconde.sofiatracker

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dpconde.sofiatracker.data.remote.FirebaseConnectionChecker
import com.dpconde.sofiatracker.data.sync.SyncManager
import com.dpconde.sofiatracker.data.work.SyncWorkManager
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SofiaTrackerApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var syncWorkManager: SyncWorkManager
    
    @Inject
    lateinit var firebaseConnectionChecker: FirebaseConnectionChecker
    
    @Inject
    lateinit var syncManager: SyncManager
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
            Log.d("SofiaTracker", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("SofiaTracker", "Firebase initialization failed", e)
        }
        
        // Test Firebase connection and trigger initial sync
//        applicationScope.launch {
//            try {
//                val result = firebaseConnectionChecker.checkConnection()
//                if (result.isSuccess) {
//                    Log.d("SofiaTracker", "Firebase connection test: ${result.getOrNull()}")
//
//                    // Schedule background sync
//                    syncWorkManager.schedulePeriodicSync()
//
//                    // Trigger immediate sync on app startup
//                    Log.d("SofiaTracker", "Triggering initial sync on app startup")
//                    syncManager.performFullSync().collect { syncResult ->
//                        when (syncResult) {
//                            is com.dpconde.sofiatracker.data.sync.SyncResult.InProgress -> {
//                                Log.d("SofiaTracker", "Initial sync in progress")
//                            }
//                            is com.dpconde.sofiatracker.data.sync.SyncResult.Progress -> {
//                                Log.d("SofiaTracker", "Initial sync progress: ${syncResult.message}")
//                            }
//                            is com.dpconde.sofiatracker.data.sync.SyncResult.Success -> {
//                                Log.d("SofiaTracker", "Initial sync completed: ${syncResult.message}")
//                            }
//                            is com.dpconde.sofiatracker.data.sync.SyncResult.Error -> {
//                                Log.e("SofiaTracker", "Initial sync failed", syncResult.exception)
//                            }
//                        }
//                    }
//                } else {
//                    Log.e("SofiaTracker", "Firebase connection test failed: ${result.exceptionOrNull()}")
//                    // Still schedule periodic sync for when connection is restored
//                    syncWorkManager.schedulePeriodicSync()
//                }
//            } catch (e: Exception) {
//                Log.e("SofiaTracker", "Error testing Firebase connection", e)
//                // Still schedule periodic sync for when connection is restored
//                syncWorkManager.schedulePeriodicSync()
//            }
//        }
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}