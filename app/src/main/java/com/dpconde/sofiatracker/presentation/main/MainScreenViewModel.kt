package com.dpconde.sofiatracker.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dpconde.sofiatracker.domain.model.Event
import com.dpconde.sofiatracker.domain.model.EventType
import com.dpconde.sofiatracker.domain.usecase.GetRecentEventsByTypeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val getRecentEventsByTypeUseCase: GetRecentEventsByTypeUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()
    
    init {
        loadRecentEvents()
    }
    
    private fun loadRecentEvents() {
        viewModelScope.launch {
            combine(
                getRecentEventsByTypeUseCase(EventType.SLEEP),
                getRecentEventsByTypeUseCase(EventType.EAT)
            ) { sleepEvents, eatEvents ->
                MainScreenUiState(
                    recentSleepEvents = sleepEvents,
                    recentEatEvents = eatEvents,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}

data class MainScreenUiState(
    val recentSleepEvents: List<Event> = emptyList(),
    val recentEatEvents: List<Event> = emptyList(),
    val isLoading: Boolean = true
)