package com.elinex.imagestesttask.presentation

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityViewModelTest {

    private lateinit var viewModel: MainActivityViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MainActivityViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have default values`() = runTest {
        val initialState = viewModel.uiState.first()
        
        assertFalse(initialState.notificationPermissionRequested)
        assertFalse(initialState.notificationPermissionGranted)
        assertFalse(initialState.appInitialized)
        assertTrue(initialState.shouldKeepSplashScreen())
    }

    @Test
    fun `setNotificationPermissionRequested should update state`() = runTest {
        viewModel.setNotificationPermissionRequested(true)
        
        val state = viewModel.uiState.first()
        assertTrue(state.notificationPermissionRequested)
        assertFalse(state.notificationPermissionGranted)
        assertFalse(state.appInitialized)
        assertTrue(state.shouldKeepSplashScreen()) // Still true because app not initialized
    }

    @Test
    fun `setNotificationPermissionGranted should update state`() = runTest {
        viewModel.setNotificationPermissionGranted(true)
        
        val state = viewModel.uiState.first()
        assertFalse(state.notificationPermissionRequested)
        assertTrue(state.notificationPermissionGranted)
        assertFalse(state.appInitialized)
        assertTrue(state.shouldKeepSplashScreen()) // Still true because app not initialized
    }

    @Test
    fun `setAppInitialized should update state`() = runTest {
        viewModel.setAppInitialized(true)
        
        val state = viewModel.uiState.first()
        assertFalse(state.notificationPermissionRequested)
        assertFalse(state.notificationPermissionGranted)
        assertTrue(state.appInitialized)
        assertTrue(state.shouldKeepSplashScreen()) // Still true because permission not requested
    }

    @Test
    fun `shouldKeepSplashScreen should return false when both conditions are met`() = runTest {
        viewModel.setNotificationPermissionRequested(true)
        viewModel.setAppInitialized(true)
        
        val state = viewModel.uiState.first()
        assertTrue(state.notificationPermissionRequested)
        assertTrue(state.appInitialized)
        assertFalse(state.shouldKeepSplashScreen())
    }

    @Test
    fun `shouldKeepSplashScreen should return true when permission not requested`() = runTest {
        viewModel.setAppInitialized(true)
        
        val state = viewModel.uiState.first()
        assertFalse(state.notificationPermissionRequested)
        assertTrue(state.appInitialized)
        assertTrue(state.shouldKeepSplashScreen())
    }

    @Test
    fun `shouldKeepSplashScreen should return true when app not initialized`() = runTest {
        viewModel.setNotificationPermissionRequested(true)
        
        val state = viewModel.uiState.first()
        assertTrue(state.notificationPermissionRequested)
        assertFalse(state.appInitialized)
        assertTrue(state.shouldKeepSplashScreen())
    }

    @Test
    fun `state updates should be emitted correctly`() = runTest {
        viewModel.uiState.test {
            // Initial state
            assertEquals(MainActivityUiState(), awaitItem())
            
            // Update permission requested
            viewModel.setNotificationPermissionRequested(true)
            assertEquals(
                MainActivityUiState(notificationPermissionRequested = true),
                awaitItem()
            )
            
            // Update permission granted
            viewModel.setNotificationPermissionGranted(true)
            assertEquals(
                MainActivityUiState(
                    notificationPermissionRequested = true,
                    notificationPermissionGranted = true
                ),
                awaitItem()
            )
            
            // Update app initialized
            viewModel.setAppInitialized(true)
            assertEquals(
                MainActivityUiState(
                    notificationPermissionRequested = true,
                    notificationPermissionGranted = true,
                    appInitialized = true
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `multiple state updates should work correctly`() = runTest {
        viewModel.setNotificationPermissionRequested(true)
        viewModel.setNotificationPermissionGranted(true)
        viewModel.setAppInitialized(true)
        
        val finalState = viewModel.uiState.first()
        
        assertTrue(finalState.notificationPermissionRequested)
        assertTrue(finalState.notificationPermissionGranted)
        assertTrue(finalState.appInitialized)
        assertFalse(finalState.shouldKeepSplashScreen())
    }
}
