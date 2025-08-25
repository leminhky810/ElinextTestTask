package com.elinex.imagestesttask.presentation.imageDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ImageDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get arguments from SavedStateHandle
    private val _imageId: Long? = savedStateHandle["imageId"]
    private val _imageUrl: String? = savedStateHandle["imageUrl"]

    private val _uiState = MutableStateFlow(
        ImageDetailUiState(
            _imageId,
            _imageUrl
        )
    )
    val uiState = _uiState.asStateFlow()

}

data class ImageDetailUiState(
    val imageId: Long? = null,
    val imageUrl: String? = null,
)