package com.elinex.imagestesttask.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elinex.imagestesttask.core.ITEMS_PER_PAGE
import com.elinex.imagestesttask.domain.model.ImageModel
import com.elinex.imagestesttask.domain.usecase.AddImageUseCase
import com.elinex.imagestesttask.domain.usecase.StreamImagesUseCase
import com.elinex.imagestesttask.worker.ImageSyncInitializer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Home screen that manages image display and user interactions.
 * 
 * This ViewModel is responsible for:
 * - Loading and streaming images from the database
 * - Managing the UI state for the home screen
 * - Handling user actions like adding new images and refreshing
 * - Coordinating with background workers for data synchronization
 * - Providing reactive state updates to the UI
 * - Managing concurrent add image operations
 * 
 * Key features:
 * - Uses StateFlow for reactive state management
 * - Supports multiple concurrent add image operations
 * - Cancels all add operations when refresh is triggered
 * - Integrates with use cases for business logic
 * - Manages loading states and empty states
 * 
 * The ViewModel follows the MVVM pattern and uses Hilt for dependency injection.
 * It maintains a single source of truth for the Home screen's UI state.
 * 
 * @author Elinext Test Task Team
 * @since 1.0.0
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val streamImagesUseCase: StreamImagesUseCase,
    private val initializer: ImageSyncInitializer,
    private val addImageUseCase: AddImageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> =
        _uiState
            .onStart {
                loadImages()
            }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState())

    /**
     * Tracks multiple concurrent add image operations.
     * Jobs are automatically removed when completed to prevent memory leaks.
     */
    private val addImageJobs = mutableListOf<kotlinx.coroutines.Job>()

    private fun loadImages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            streamImagesUseCase().collect { images ->
                _uiState.update {
                    it.copy(
                        images = images,
                        isLoading = false,
                        isEmpty = images.isEmpty()
                    )
                }
            }
        }
    }

    /**
     * Adds a new image to the database.
     * 
     * This method supports multiple concurrent calls. Each call creates a new job
     * that runs independently. All jobs are tracked and can be cancelled when
     * refreshImages() is called.
     */
    fun addNewImage() {
        // Start new add image operation and track it
        val job = viewModelScope.launch {
            addImageUseCase().fold(
                onSuccess = {
                    // Image added successfully
                },
                onFailure = {
                    // Image addition failed
                }
            )
        }
        
        // Add job to the list and remove it when completed
        addImageJobs.add(job)
        job.invokeOnCompletion {
            addImageJobs.remove(job)
        }
    }

    /**
     * Refreshes the image database by reinitializing it.
     * 
     * This method cancels all running add image operations before starting
     * the refresh to ensure data consistency.
     */
    fun refreshImages() {
        // Cancel all running add image operations before refreshing
        addImageJobs.forEach { it.cancel() }
        addImageJobs.clear()
        
        viewModelScope.launch {
            initializer.initializeDatabase()
        }
    }

}

/**
 * Data class representing the UI state for the Home screen.
 * 
 * This state class encapsulates all the information needed to render the
 * home screen, including the list of images, loading states, and empty states.
 * 
 * Properties:
 * - images: List of images to display in the grid
 * - isLoading: Whether the screen is in a loading state
 * - isEmpty: Whether there are no images to display
 * 
 * The state is managed reactively and automatically updates the UI when
 * the underlying data changes.
 */
data class HomeUiState(
    val images: List<ImageModel> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false
)