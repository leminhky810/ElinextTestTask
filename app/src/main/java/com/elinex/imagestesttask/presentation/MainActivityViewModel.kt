package com.elinex.imagestesttask.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for the MainActivity that manages the application's initialization state.
 * 
 * This ViewModel is responsible for:
 * - Tracking the application initialization progress
 * - Managing notification permission state
 * - Coordinating with the splash screen display
 * - Providing reactive state updates to the UI
 * 
 * Key features:
 * - Uses StateFlow for reactive state management
 * - Provides methods to update various initialization states
 * - Coordinates splash screen visibility based on app state
 * - Manages permission request lifecycle
 * 
 * The ViewModel follows the MVVM pattern and uses Hilt for dependency injection.
 * It maintains a single source of truth for the MainActivity's UI state.
 * 
 * @author Elinext Test Task Team
 * @since 1.0.0
 */
@HiltViewModel
class MainActivityViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainActivityUiState())
    val uiState = _uiState.asStateFlow()
    
    fun setNotificationPermissionRequested(requested: Boolean) {
        _uiState.value = _uiState.value.copy(
            notificationPermissionRequested = requested
        )
    }
    
    fun setNotificationPermissionGranted(granted: Boolean) {
        _uiState.value = _uiState.value.copy(
            notificationPermissionGranted = granted
        )
    }
    
    fun setAppInitialized(initialized: Boolean) {
        _uiState.value = _uiState.value.copy(
            appInitialized = initialized
        )
    }
}

/**
 * Data class representing the UI state for MainActivity.
 * 
 * This state class encapsulates all the information needed to determine:
 * - Whether the splash screen should remain visible
 * - The current status of notification permissions
 * - The application initialization progress
 * 
 * Properties:
 * - notificationPermissionRequested: Whether permission has been requested
 * - notificationPermissionGranted: Whether permission was granted
 * - appInitialized: Whether the app has completed initialization
 * 
 * The shouldKeepSplashScreen() method determines if the splash screen
 * should remain visible based on the current state.
 */
data class MainActivityUiState(
    val notificationPermissionRequested: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val appInitialized: Boolean = false
) {
    /**
     * Determines whether the splash screen should remain visible.
     * 
     * The splash screen is kept visible until both:
     * - Notification permission has been handled (requested)
     * - Application initialization is complete
     * 
     * @return true if splash screen should remain visible, false otherwise
     */
    fun shouldKeepSplashScreen(): Boolean {
        // Keep splash screen until notification permission is handled and app is initialized
        return !notificationPermissionRequested || !appInitialized
    }
}
