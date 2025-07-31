package com.dpconde.sofiatracker.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dpconde.sofiatracker.data.local.entity.SyncStateEntity
import com.dpconde.sofiatracker.data.sync.SyncResult
import com.dpconde.sofiatracker.domain.model.Event
import com.dpconde.sofiatracker.domain.model.EventType
import com.dpconde.sofiatracker.domain.repository.EventRepository
import com.dpconde.sofiatracker.domain.usecase.GetRecentEventsByTypeUseCase
import kotlinx.coroutines.delay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val getRecentEventsByTypeUseCase: GetRecentEventsByTypeUseCase,
    private val eventRepository: EventRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()
    
    init {
        loadRecentEvents()
        // Trigger sync when the main screen loads
        triggerStartupSync()
    }
    
    private fun loadRecentEvents() {
        viewModelScope.launch {
            combine(
                getRecentEventsByTypeUseCase(EventType.SLEEP),
                getRecentEventsByTypeUseCase(EventType.EAT),
                getRecentEventsByTypeUseCase(EventType.POOP),
                eventRepository.getSyncState()
            ) { sleepEvents, eatEvents, poopEvents, syncState ->
                MainScreenUiState(
                    recentSleepEvents = sleepEvents,
                    recentEatEvents = eatEvents,
                    recentPoopEvents = poopEvents,
                    syncState = syncState,
                    isNetworkAvailable = eventRepository.isNetworkAvailable(),
                    pendingSyncCount = try { eventRepository.getPendingSyncCount() } catch (e: Exception) { 0 },
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
    
    private fun triggerStartupSync() {
        viewModelScope.launch {
            // Wait a bit for the app to fully initialize
            delay(1000)
            
            // Only sync if network is available to avoid unnecessary errors
            if (eventRepository.isNetworkAvailable()) {
                try {
                    eventRepository.syncAllEvents().collect { result ->
                        // Log sync progress but don't show UI errors on startup
                        when (result) {
                            is SyncResult.Success -> {
                                // Sync completed successfully - UI will update via state flow
                            }
                            is SyncResult.Error -> {
                                // Log error but don't interrupt user experience
                                result.exception.printStackTrace()
                            }
                            else -> {
                                // Other states (InProgress, Progress) - UI will show via state flow
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Silently handle startup sync errors
                    e.printStackTrace()
                }
            }
        }
    }
    
    fun triggerSync() {
        viewModelScope.launch {
            try {
                eventRepository.syncAllEvents().collect { result ->
                    when (result) {
                        is SyncResult.InProgress -> {
                            // Sync started - UI will update via state flow
                        }
                        is SyncResult.Success -> {
                            // Sync completed - UI will update via state flow
                        }
                        is SyncResult.Error -> {
                            // Handle error - could show a snackbar or similar
                        }
                        is SyncResult.Progress -> {
                            // Show progress - could update UI with progress message
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle sync trigger error
                e.printStackTrace()
            }
        }
    }
}

data class MainScreenUiState(
    val recentSleepEvents: List<Event> = emptyList(),
    val recentEatEvents: List<Event> = emptyList(),
    val recentPoopEvents: List<Event> = emptyList(),
    val syncState: SyncStateEntity? = null,
    val isNetworkAvailable: Boolean = false,
    val pendingSyncCount: Int = 0,
    val isLoading: Boolean = true
)