package com.dpconde.sofiatracker.presentation.addevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dpconde.sofiatracker.domain.model.Event
import com.dpconde.sofiatracker.domain.model.EventType
import com.dpconde.sofiatracker.domain.usecase.AddEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AddEventViewModel @Inject constructor(
    private val addEventUseCase: AddEventUseCase,
    private val getEventByIdUseCase: com.dpconde.sofiatracker.domain.usecase.GetEventByIdUseCase,
    private val updateEventUseCase: com.dpconde.sofiatracker.domain.usecase.UpdateEventUseCase,
    private val deleteEventUseCase: com.dpconde.sofiatracker.domain.usecase.DeleteEventUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddEventUiState())
    val uiState: StateFlow<AddEventUiState> = _uiState.asStateFlow()
    
    fun setEventType(eventType: EventType) {
        _uiState.value = _uiState.value.copy(selectedEventType = eventType)
    }
    
    fun updateEventType(eventType: EventType) {
        _uiState.value = _uiState.value.copy(selectedEventType = eventType)
    }
    
    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }
    
    fun setCustomTimestamp(timestamp: LocalDateTime) {
        _uiState.value = _uiState.value.copy(customTimestamp = timestamp)
    }
    
    fun updateBottleAmount(amount: Int?) {
        _uiState.value = _uiState.value.copy(bottleAmountMl = amount)
    }
    
    fun updateDiaperType(diaperType: String?) {
        _uiState.value = _uiState.value.copy(diaperType = diaperType)
    }
    
    fun updateSleepType(sleepType: String?) {
        _uiState.value = _uiState.value.copy(sleepType = sleepType)
    }
    
    fun addEvent() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.selectedEventType != null) {
                val event = Event(
                    type = currentState.selectedEventType,
                    timestamp = currentState.customTimestamp ?: LocalDateTime.now(),
                    note = currentState.note,
                    bottleAmountMl = currentState.bottleAmountMl,
                    diaperType = currentState.diaperType,
                    sleepType = currentState.sleepType
                )
                
                _uiState.value = currentState.copy(isLoading = true)
                
                try {
                    addEventUseCase(event)
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        eventAdded = true,
                        note = ""
                    )
                } catch (e: Exception) {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun clearEventAdded() {
        _uiState.value = _uiState.value.copy(eventAdded = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun loadEventForEditing(eventId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val event = getEventByIdUseCase(eventId)
                if (event != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        editingEventId = eventId,
                        originalEvent = event,
                        selectedEventType = event.type,
                        note = event.note,
                        customTimestamp = event.timestamp,
                        bottleAmountMl = event.bottleAmountMl,
                        diaperType = event.diaperType,
                        sleepType = event.sleepType
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
    
    fun updateEvent() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val originalEvent = currentState.originalEvent
            
            if (originalEvent != null) {
                _uiState.value = currentState.copy(isLoading = true)
                
                try {
                    val updatedEvent = originalEvent.copy(
                        note = currentState.note,
                        timestamp = currentState.customTimestamp ?: originalEvent.timestamp,
                        bottleAmountMl = currentState.bottleAmountMl,
                        diaperType = currentState.diaperType,
                        sleepType = currentState.sleepType
                    )
                    
                    updateEventUseCase(updatedEvent)
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        eventAdded = true
                    )
                } catch (e: Exception) {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update event"
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
    
    fun clearEventDeleted() {
        _uiState.value = _uiState.value.copy(eventDeleted = false)
    }
    
    val isEditMode: Boolean get() = _uiState.value.editingEventId != null
}

data class AddEventUiState(
    val selectedEventType: EventType? = null,
    val note: String = "",
    val customTimestamp: LocalDateTime? = null,
    val bottleAmountMl: Int? = null,
    val diaperType: String? = null,
    val sleepType: String? = null,
    val isLoading: Boolean = false,
    val eventAdded: Boolean = false,
    val error: String? = null,
    val editingEventId: Long? = null,
    val originalEvent: Event? = null,
    val eventDeleted: Boolean = false
)