package com.dpconde.sofiatracker.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dpconde.sofiatracker.core.model.Event
import com.dpconde.sofiatracker.core.model.EventType
import com.dpconde.sofiatracker.core.domain.GetRecentEventsByTypeUseCase
import com.dpconde.sofiatracker.core.domain.GetBabyNameUseCase
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
    private val eventRepository: com.dpconde.sofiatracker.core.data.repository.EventRepository,
    private val getBabyNameUseCase: GetBabyNameUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()
    
    init {
        loadRecentEvents()
    }
    
    private fun loadRecentEvents() {
        viewModelScope.launch {
            try {
                combine(
                    getBabyNameUseCase(),
                    getRecentEventsByTypeUseCase(EventType.SLEEP),
                    getRecentEventsByTypeUseCase(EventType.EAT),
                    getRecentEventsByTypeUseCase(EventType.POOP),
                    eventRepository.getSyncState()
                ) { babyName, sleepEvents, eatEvents, poopEvents, syncState ->
                    MainScreenUiState(
                        babyName = babyName,
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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun triggerSync() {
        viewModelScope.launch {
            try {
                eventRepository.syncAllEvents().collect { result ->
                    when (result) {
                        is com.dpconde.sofiatracker.core.data.sync.SyncResult.InProgress -> {
                            // Sync started - UI will update via state flow
                        }
                        is com.dpconde.sofiatracker.core.data.sync.SyncResult.Success -> {
                            // Sync completed - UI will update via state flow
                        }
                        is com.dpconde.sofiatracker.core.data.sync.SyncResult.Error -> {
                            // Handle error - could show a snackbar or similar
                        }
                        is com.dpconde.sofiatracker.core.data.sync.SyncResult.Progress -> {
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
    val babyName: String = "Sofía",
    val recentSleepEvents: List<Event> = emptyList(),
    val recentEatEvents: List<Event> = emptyList(),
    val recentPoopEvents: List<Event> = emptyList(),
    val syncState: com.dpconde.sofiatracker.core.database.entity.SyncStateEntity? = null,
    val isNetworkAvailable: Boolean = false,
    val pendingSyncCount: Int = 0,
    val isLoading: Boolean = true
)