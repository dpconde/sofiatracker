package com.dpconde.sofiatracker.core.network.firebase

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseConnectionChecker @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    suspend fun checkConnection(): Result<String> {
        return try {
            Log.d("FirebaseConnection", "Checking Firebase connection...")
            
            // Check if Firebase is initialized
            val app = FirebaseApp.getInstance()
            Log.d("FirebaseConnection", "Firebase app: ${app.name}, options: ${app.options.projectId}")
            
            // Try a simple read operation
            val testCollection = firestore.collection("connection_test")
            val snapshot = testCollection.limit(1).get().await()
            
            Log.d("FirebaseConnection", "Firebase connection successful")
            Result.success("Connected to Firebase project: ${app.options.projectId}")
            
        } catch (e: Exception) {
            Log.e("FirebaseConnection", "Firebase connection failed", e)
            Result.failure(e)
        }
    }
    
    suspend fun enableOfflineFeatures(): Result<Unit> {
        return try {
            firestore.enableNetwork().await()
            Log.d("FirebaseConnection", "Firebase network enabled")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseConnection", "Failed to enable Firebase network", e)
            Result.failure(e)
        }
    }
}