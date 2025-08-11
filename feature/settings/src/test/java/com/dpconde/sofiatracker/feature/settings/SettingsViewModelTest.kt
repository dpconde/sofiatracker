package com.dpconde.sofiatracker.feature.settings

import com.dpconde.sofiatracker.core.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @Mock
    private lateinit var settingsRepository: SettingsRepository

    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default baby name`() = runTest {
        val defaultName = "Sofía"
        
        viewModel = SettingsViewModel(settingsRepository)
        
        val initialState = viewModel.uiState.value
        assertEquals(defaultName, initialState.babyName)
        assertNull(initialState.error)
    }

    @Test
    fun `loadSettings success updates state correctly`() = runTest {
        val expectedName = "Emma"
        whenever(settingsRepository.getBabyName()).thenReturn(flowOf(expectedName))
        
        viewModel = SettingsViewModel(settingsRepository)
        advanceUntilIdle()
        
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertEquals(expectedName, finalState.babyName)
        assertNull(finalState.error)
    }

    @Test
    fun `loadSettings error updates state with error message`() = runTest {
        val errorMessage = "Network error"
        whenever(settingsRepository.getBabyName()).thenThrow(RuntimeException(errorMessage))
        
        viewModel = SettingsViewModel(settingsRepository)
        advanceUntilIdle()
        
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertEquals("Sofía", finalState.babyName)
        assertEquals(errorMessage, finalState.error)
    }

    @Test
    fun `updateBabyName updates ui state`() = runTest {
        viewModel = SettingsViewModel(settingsRepository)
        
        val newName = "Isabella"
        viewModel.updateBabyName(newName)
        
        val updatedState = viewModel.uiState.value
        assertEquals(newName, updatedState.babyName)
    }

    @Test
    fun `saveBabyName success updates state correctly`() = runTest {
        viewModel = SettingsViewModel(settingsRepository)
        advanceUntilIdle()
        
        viewModel.updateBabyName("Lucia")
        viewModel.saveBabyName()
        advanceUntilIdle()

        verify(settingsRepository).setBabyName("Lucia")
        
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertNull(finalState.error)
    }

    @Test
    fun `saveBabyName error updates state with error message`() = runTest {
        val errorMessage = "Save failed"
        whenever(settingsRepository.getBabyName()).thenReturn(flowOf("Sofia"))
        whenever(settingsRepository.setBabyName("Lucia")).thenThrow(RuntimeException(errorMessage))
        
        viewModel = SettingsViewModel(settingsRepository)
        advanceUntilIdle()
        
        viewModel.updateBabyName("Lucia")
        viewModel.saveBabyName()
        advanceUntilIdle()
        
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertEquals(errorMessage, finalState.error)
    }

}