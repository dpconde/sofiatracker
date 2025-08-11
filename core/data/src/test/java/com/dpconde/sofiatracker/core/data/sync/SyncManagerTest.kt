package com.dpconde.sofiatracker.core.data.sync

import com.dpconde.sofiatracker.core.database.dao.EventDao
import com.dpconde.sofiatracker.core.database.dao.SyncStateDao
import com.dpconde.sofiatracker.core.database.entity.EventEntity
import com.dpconde.sofiatracker.core.database.entity.SyncStateEntity
import com.dpconde.sofiatracker.core.model.Event
import com.dpconde.sofiatracker.core.model.EventType
import com.dpconde.sofiatracker.core.model.SyncStatus
import com.dpconde.sofiatracker.core.network.firebase.RemoteEventDataSource
import com.dpconde.sofiatracker.core.network.model.RemoteEventDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SyncManagerTest {

    @Mock
    private lateinit var eventDao: EventDao

    @Mock
    private lateinit var syncStateDao: SyncStateDao

    @Mock
    private lateinit var remoteDataSource: RemoteEventDataSource

    @Mock
    private lateinit var conflictResolutionStrategy: ConflictResolutionStrategy

    private lateinit var syncManager: SyncManager
    private val testDispatcher = StandardTestDispatcher()

    private val sampleLocalEvent = EventEntity(
        id = 1L,
        type = EventType.POOP,
        timestamp = LocalDateTime.now(),
        sleepType = null,
        diaperType = "WET",
        bottleAmountMl = null,
        note = "Test note",
        syncStatus = SyncStatus.PENDING_SYNC,
        lastSyncAttempt = null,
        remoteId = null,
        version = 1
    )

    private val sampleRemoteEvent = RemoteEventDto(
        id = "remote123",
        type = "POOP",
        timestamp = "2024-01-01T12:00:00",
        sleepType = null,
        diaperType = "WET",
        bottleAmountMl = null,
        note = "Remote note",
        version = 2,
        lastModified = System.currentTimeMillis(),
        deleted = false
    )

    private val sampleSyncState = SyncStateEntity(
        status = SyncStatus.SYNCED,
        lastSyncAttempt = LocalDateTime.now(),
        lastSuccessfulSync = LocalDateTime.now().minusHours(1),
        errorMessage = null,
        pendingEventsCount = 0
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        syncManager = SyncManager(eventDao, syncStateDao, remoteDataSource, conflictResolutionStrategy)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `performFullSync success with no pending events`() = runTest {
        whenever(eventDao.getEventsBySyncStatus(SyncStatus.PENDING_SYNC)).thenReturn(emptyList())
        whenever(eventDao.getPendingSyncCount()).thenReturn(0)
        whenever(syncStateDao.getSyncStateOnce()).thenReturn(sampleSyncState)
        whenever(remoteDataSource.getEventsModifiedAfter(any())).thenReturn(Result.success(emptyList()))

        val results = syncManager.performFullSync().toList()

        assertTrue(results.any { it is SyncResult.InProgress })
        assertTrue(results.any { it is SyncResult.Success })
        verify(syncStateDao, times(2)).updateSyncState(any())
    }

    @Test
    fun `performFullSync uploads pending events successfully`() = runTest {
        whenever(eventDao.getEventsBySyncStatus(SyncStatus.PENDING_SYNC)).thenReturn(listOf(sampleLocalEvent))
        whenever(eventDao.getPendingSyncCount()).thenReturn(0)
        whenever(syncStateDao.getSyncStateOnce()).thenReturn(sampleSyncState)
        whenever(remoteDataSource.saveEvent(any())).thenReturn(Result.success("remote123"))
        whenever(remoteDataSource.getEventsModifiedAfter(any())).thenReturn(Result.success(emptyList()))

        val results = syncManager.performFullSync().toList()

        verify(remoteDataSource).saveEvent(any())
        verify(eventDao).updateSyncStatusWithRemoteId(1L, SyncStatus.SYNCED, "remote123")
        assertTrue(results.any { it is SyncResult.Success })
    }

    @Test
    fun `performFullSync handles upload failure`() = runTest {
        whenever(eventDao.getEventsBySyncStatus(SyncStatus.PENDING_SYNC)).thenReturn(listOf(sampleLocalEvent))
        whenever(eventDao.getPendingSyncCount()).thenReturn(1)
        whenever(syncStateDao.getSyncStateOnce()).thenReturn(sampleSyncState)
        whenever(remoteDataSource.saveEvent(any())).thenReturn(Result.failure(Exception("Upload failed")))

        val results = syncManager.performFullSync().toList()

        verify(eventDao).updateSyncStatus(1L, SyncStatus.SYNC_ERROR)
        assertTrue(results.any { it is SyncResult.Error })
        verify(syncStateDao, times(2)).updateSyncState(any()) // Called for start and error
    }

    @Test
    fun `performFullSync downloads and processes new remote events`() = runTest {
        whenever(eventDao.getEventsBySyncStatus(SyncStatus.PENDING_SYNC)).thenReturn(emptyList())
        whenever(eventDao.getPendingSyncCount()).thenReturn(0)
        whenever(syncStateDao.getSyncStateOnce()).thenReturn(sampleSyncState)
        whenever(remoteDataSource.getEventsModifiedAfter(any())).thenReturn(Result.success(listOf(sampleRemoteEvent)))
        whenever(eventDao.getEventsBySyncStatus(SyncStatus.SYNCED)).thenReturn(emptyList())

        val results = syncManager.performFullSync().toList()

        verify(eventDao).insertEvent(any())
        assertTrue(results.any { it is SyncResult.Success })
    }

    @Test
    fun `performFullSync handles conflicts with remote events`() = runTest {
        val conflictingLocalEvent = sampleLocalEvent.copy(
            syncStatus = SyncStatus.SYNCED,
            remoteId = "remote123"
        )
        val resolution = ConflictResolution(
            policy = ConflictResolutionPolicy.REMOTE_WINS,
            resolvedEvent = conflictingLocalEvent.copy(note = "Resolved note"),
            conflictReason = "Remote version newer"
        )

        whenever(eventDao.getEventsBySyncStatus(SyncStatus.PENDING_SYNC)).thenReturn(emptyList())
        whenever(eventDao.getPendingSyncCount()).thenReturn(0)
        whenever(syncStateDao.getSyncStateOnce()).thenReturn(sampleSyncState)
        whenever(remoteDataSource.getEventsModifiedAfter(any())).thenReturn(Result.success(listOf(sampleRemoteEvent)))
        whenever(eventDao.getEventsBySyncStatus(SyncStatus.SYNCED)).thenReturn(listOf(conflictingLocalEvent))
        whenever(conflictResolutionStrategy.hasConflict(any(), any())).thenReturn(true)
        whenever(conflictResolutionStrategy.resolveConflict(any(), any(), any())).thenReturn(resolution)

        val results = syncManager.performFullSync().toList()

        verify(conflictResolutionStrategy).resolveConflict(
            eq(conflictingLocalEvent),
            eq(sampleRemoteEvent),
            eq(ConflictResolutionPolicy.REMOTE_WINS)
        )
        verify(eventDao).updateEvent(resolution.resolvedEvent)
        assertTrue(results.any { it is SyncResult.Success })
    }

    @Test
    fun `performFullSync handles deleted remote events`() = runTest {
        val deletedRemoteEvent = sampleRemoteEvent.copy(deleted = true)
        val localEventToDelete = sampleLocalEvent.copy(
            syncStatus = SyncStatus.SYNCED,
            remoteId = "remote123"
        )

        whenever(eventDao.getEventsBySyncStatus(SyncStatus.PENDING_SYNC)).thenReturn(emptyList())
        whenever(eventDao.getPendingSyncCount()).thenReturn(0)
        whenever(syncStateDao.getSyncStateOnce()).thenReturn(sampleSyncState)
        whenever(remoteDataSource.getEventsModifiedAfter(any())).thenReturn(Result.success(listOf(deletedRemoteEvent)))
        whenever(eventDao.getEventsBySyncStatus(SyncStatus.SYNCED)).thenReturn(listOf(localEventToDelete))

        val results = syncManager.performFullSync().toList()

        verify(eventDao).deleteEvent(localEventToDelete)
        assertTrue(results.any { it is SyncResult.Success })
    }

    @Test
    fun `syncSingleEvent success`() = runTest {
        whenever(eventDao.getEventsBySyncStatus(SyncStatus.PENDING_SYNC)).thenReturn(listOf(sampleLocalEvent))
        whenever(remoteDataSource.saveEvent(any())).thenReturn(Result.success("remote123"))

        val result = syncManager.syncSingleEvent(1L)

        assertTrue(result.isSuccess)
        verify(eventDao).updateSyncStatusWithTime(eq(1L), eq(SyncStatus.SYNCING), any())
        verify(eventDao).updateSyncStatusWithRemoteId(1L, SyncStatus.SYNCED, "remote123")
    }

    @Test
    fun `syncSingleEvent handles event not found`() = runTest {
        whenever(eventDao.getEventsBySyncStatus(SyncStatus.PENDING_SYNC)).thenReturn(emptyList())

        val result = syncManager.syncSingleEvent(1L)

        assertTrue(result.isFailure)
        assertEquals("Event not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `syncSingleEvent handles remote failure`() = runTest {
        whenever(eventDao.getEventsBySyncStatus(SyncStatus.PENDING_SYNC)).thenReturn(listOf(sampleLocalEvent))
        whenever(remoteDataSource.saveEvent(any())).thenReturn(Result.failure(Exception("Network error")))

        val result = syncManager.syncSingleEvent(1L)

        assertTrue(result.isFailure)
        verify(eventDao).updateSyncStatus(1L, SyncStatus.SYNC_ERROR)
    }

    @Test
    fun `deleteRemoteEvent success`() = runTest {
        whenever(remoteDataSource.getEvent("remote123")).thenReturn(Result.success(sampleRemoteEvent))
        whenever(remoteDataSource.saveRemoteEvent(any())).thenReturn(Result.success("remote123"))

        val result = syncManager.deleteRemoteEvent("remote123")

        assertTrue(result.isSuccess)
        verify(remoteDataSource).saveRemoteEvent(any())
    }

    @Test
    fun `deleteRemoteEvent handles event not found`() = runTest {
        whenever(remoteDataSource.getEvent("remote123")).thenReturn(Result.failure(Exception("Not found")))

        val result = syncManager.deleteRemoteEvent("remote123")

        assertTrue(result.isFailure)
        verify(remoteDataSource, never()).saveRemoteEvent(any())
    }

    @Test
    fun `deleteRemoteEvent handles save failure`() = runTest {
        whenever(remoteDataSource.getEvent("remote123")).thenReturn(Result.success(sampleRemoteEvent))
        whenever(remoteDataSource.saveRemoteEvent(any())).thenReturn(Result.failure(Exception("Save failed")))

        val result = syncManager.deleteRemoteEvent("remote123")

        assertTrue(result.isFailure)
        verify(remoteDataSource).saveRemoteEvent(any())
    }

    @Test
    fun `getSyncState returns flow from dao`() = runTest {
        whenever(syncStateDao.getSyncState()).thenReturn(flowOf(sampleSyncState))

        val stateFlow = syncManager.getSyncState()
        val states = stateFlow.toList()

        assertEquals(1, states.size)
        assertEquals(sampleSyncState, states.first())
        verify(syncStateDao).getSyncState()
    }

    @Test
    fun `performFullSync progress messages are emitted correctly`() = runTest {
        whenever(eventDao.getEventsBySyncStatus(SyncStatus.PENDING_SYNC)).thenReturn(listOf(sampleLocalEvent))
        whenever(eventDao.getPendingSyncCount()).thenReturn(0)
        whenever(syncStateDao.getSyncStateOnce()).thenReturn(sampleSyncState)
        whenever(remoteDataSource.saveEvent(any())).thenReturn(Result.success("remote123"))
        whenever(remoteDataSource.getEventsModifiedAfter(any())).thenReturn(Result.success(listOf(sampleRemoteEvent)))
        whenever(eventDao.getEventsBySyncStatus(SyncStatus.SYNCED)).thenReturn(emptyList())

        val results = syncManager.performFullSync().toList()

        val progressMessages = results.filterIsInstance<SyncResult.Progress>()
        assertTrue(progressMessages.isNotEmpty())
        assertTrue(progressMessages.any { it.message.contains("Uploading") })
        assertTrue(progressMessages.any { it.message.contains("Downloading") })
    }
}