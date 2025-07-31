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
    private val addEventUseCase: AddEventUseCase
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
    
    fun addEvent() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.selectedEventType != null) {
                val event = Event(
                    type = currentState.selectedEventType,
                    timestamp = currentState.customTimestamp ?: LocalDateTime.now(),
                    note = currentState.note,
                    bottleAmountMl = currentState.bottleAmountMl
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
}

data class AddEventUiState(
    val selectedEventType: EventType? = null,
    val note: String = "",
    val customTimestamp: LocalDateTime? = null,
    val bottleAmountMl: Int? = null,
    val isLoading: Boolean = false,
    val eventAdded: Boolean = false,
    val error: String? = null
)