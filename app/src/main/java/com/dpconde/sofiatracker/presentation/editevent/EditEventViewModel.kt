package com.dpconde.sofiatracker.presentation.editevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dpconde.sofiatracker.domain.model.Event
import com.dpconde.sofiatracker.domain.model.EventType
import com.dpconde.sofiatracker.domain.usecase.DeleteEventUseCase
import com.dpconde.sofiatracker.domain.usecase.GetEventByIdUseCase
import com.dpconde.sofiatracker.domain.usecase.UpdateEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EditEventViewModel @Inject constructor(
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val updateEventUseCase: UpdateEventUseCase,
    private val deleteEventUseCase: DeleteEventUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EditEventUiState())
    val uiState: StateFlow<EditEventUiState> = _uiState.asStateFlow()
    
    fun loadEvent(eventId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val event = getEventByIdUseCase(eventId)
                if (event != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        originalEvent = event,
                        eventType = event.type,
                        note = event.note,
                        timestamp = event.timestamp,
                        bottleAmountMl = event.bottleAmountMl
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Event not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load event"
                )
            }
        }
    }
    
    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }
    
    fun updateTimestamp(timestamp: LocalDateTime) {
        _uiState.value = _uiState.value.copy(timestamp = timestamp)
    }
    
    fun updateBottleAmount(amount: Int?) {
        _uiState.value = _uiState.value.copy(bottleAmountMl = amount)
    }
    
    fun saveEvent() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val originalEvent = currentState.originalEvent
            
            if (originalEvent != null) {
                _uiState.value = currentState.copy(isLoading = true)
                
                try {
                    val updatedEvent = originalEvent.copy(
                        note = currentState.note,
                        timestamp = currentState.timestamp,
                        bottleAmountMl = currentState.bottleAmountMl
                    )
                    
                    updateEventUseCase(updatedEvent)
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        eventSaved = true
                    )
                } catch (e: Exception) {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to save event"
                    )
                }
            }
        }
    }
    
    fun deleteEvent() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val originalEvent = currentState.originalEvent
            
            if (originalEvent != null) {
                _uiState.value = currentState.copy(isLoading = true)
                
                try {
                    deleteEventUseCase(originalEvent)
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        eventDeleted = true
                    )
                } catch (e: Exception) {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to delete event"
                    )
                }
            }
        }
    }
    
    fun clearEventSaved() {
        _uiState.value = _uiState.value.copy(eventSaved = false)
    }
    
    fun clearEventDeleted() {
        _uiState.value = _uiState.value.copy(eventDeleted = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class EditEventUiState(
    val isLoading: Boolean = false,
    val originalEvent: Event? = null,
    val eventType: EventType? = null,
    val note: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val bottleAmountMl: Int? = null,
    val eventSaved: Boolean = false,
    val eventDeleted: Boolean = false,
    val error: String? = null
)