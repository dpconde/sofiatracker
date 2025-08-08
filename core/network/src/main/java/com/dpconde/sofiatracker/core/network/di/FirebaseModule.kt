package com.dpconde.sofiatracker.core.network.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            
            // Enable offline persistence for offline-first functionality
            try {
                @Suppress("DEPRECATION")
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build()
                
                firestore.firestoreSettings = settings
            } catch (e: Exception) {
                // Settings already applied or failed to apply
                e.printStackTrace()
            }
            
            firestore
        } catch (e: Exception) {
            // Return default instance if initialization fails
            e.printStackTrace()
            FirebaseFirestore.getInstance()
        }
    }
}