package com.dpconde.sofiatracker.data.remote

import com.dpconde.sofiatracker.data.remote.dto.RemoteEventDto
import com.dpconde.sofiatracker.data.remote.dto.toRemoteDto
import com.dpconde.sofiatracker.domain.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteEventDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    private val eventsCollection by lazy { 
        try {
            firestore.collection("events")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    
    suspend fun saveEvent(event: Event): Result<String> {
        return try {
            val remoteEvent = event.toRemoteDto()
            val documentRef = if (event.remoteId != null) {
                eventsCollection.document(event.remoteId)
            } else {
                eventsCollection.document()
            }
            
            // Ensure lastModified is always updated when saving
            val updatedRemoteEvent = remoteEvent.copy(lastModified = System.currentTimeMillis())
            documentRef.set(updatedRemoteEvent).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun saveRemoteEvent(remoteEvent: RemoteEventDto): Result<String> {
        return try {
            val documentRef = if (remoteEvent.id.isNotEmpty()) {
                eventsCollection.document(remoteEvent.id)
            } else {
                eventsCollection.document()
            }
            
            // Ensure lastModified is always updated when saving
            val updatedRemoteEvent = remoteEvent.copy(lastModified = System.currentTimeMillis())
            documentRef.set(updatedRemoteEvent).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getEvent(remoteId: String): Result<RemoteEventDto> {
        return try {
            val snapshot = eventsCollection.document(remoteId).get().await()
            val remoteEvent = snapshot.toObject(RemoteEventDto::class.java)
            if (remoteEvent != null) {
                Result.success(remoteEvent.copy(id = snapshot.id))
            } else {
                Result.failure(Exception("Event not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllEvents(): Result<List<RemoteEventDto>> {
        return try {
            val snapshot = eventsCollection.get().await()
            val events = snapshot.documents.mapNotNull { doc ->
                doc.toObject(RemoteEventDto::class.java)?.copy(id = doc.id)
            }
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEventsModifiedAfter(timestamp: Long): Result<List<RemoteEventDto>> {
        return try {
            val snapshot = eventsCollection
                .whereGreaterThan("lastModified", timestamp)
                .get()
                .await()
            
            val events = snapshot.documents.mapNotNull { doc ->
                doc.toObject(RemoteEventDto::class.java)?.copy(id = doc.id)
            }
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteEvent(remoteId: String): Result<Unit> {
        return try {
            eventsCollection.document(remoteId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun observeEvents(): Flow<List<RemoteEventDto>> = callbackFlow {
        val listener: ListenerRegistration = eventsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val events = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(RemoteEventDto::class.java)?.copy(id = doc.id)
                    }
                    trySend(events)
                }
            }
        
        awaitClose { listener.remove() }
    }
}