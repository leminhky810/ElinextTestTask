package com.elinex.imagestesttask.presentation.imageDetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImageDetailViewModelTest {

    private lateinit var viewModel: ImageDetailViewModel
    private lateinit var mockSavedStateHandle: SavedStateHandle
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have correct values when both imageId and imageUrl are provided`() = runTest {
        // Arrange
        val testImageId = 123L
        val testImageUrl = "https://example.com/image.jpg"
        mockSavedStateHandle = mockk {
            every { get<Long>("imageId") } returns testImageId
            every { get<String>("imageUrl") } returns testImageUrl
        }

        // Act
        viewModel = ImageDetailViewModel(mockSavedStateHandle)

        // Assert
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(testImageId, initialState.imageId)
            assertEquals(testImageUrl, initialState.imageUrl)
        }
    }

    @Test
    fun `initial state should have null values when no arguments are provided`() = runTest {
        // Arrange
        mockSavedStateHandle = mockk {
            every { get<Long>("imageId") } returns null
            every { get<String>("imageUrl") } returns null
        }

        // Act
        viewModel = ImageDetailViewModel(mockSavedStateHandle)

        // Assert
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertNull(initialState.imageId)
            assertNull(initialState.imageUrl)
        }
    }

    @Test
    fun `initial state should handle only imageId provided`() = runTest {
        // Arrange
        val testImageId = 456L
        mockSavedStateHandle = mockk {
            every { get<Long>("imageId") } returns testImageId
            every { get<String>("imageUrl") } returns null
        }

        // Act
        viewModel = ImageDetailViewModel(mockSavedStateHandle)

        // Assert
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(testImageId, initialState.imageId)
            assertNull(initialState.imageUrl)
        }
    }

    @Test
    fun `initial state should handle only imageUrl provided`() = runTest {
        // Arrange
        val testImageUrl = "https://example.com/another-image.jpg"
        mockSavedStateHandle = mockk {
            every { get<Long>("imageId") } returns null
            every { get<String>("imageUrl") } returns testImageUrl
        }

        // Act
        viewModel = ImageDetailViewModel(mockSavedStateHandle)

        // Assert
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertNull(initialState.imageId)
            assertEquals(testImageUrl, initialState.imageUrl)
        }
    }

    @Test
    fun `initial state should handle zero imageId`() = runTest {
        // Arrange
        val testImageId = 0L
        val testImageUrl = "https://example.com/zero-image.jpg"
        mockSavedStateHandle = mockk {
            every { get<Long>("imageId") } returns testImageId
            every { get<String>("imageUrl") } returns testImageUrl
        }

        // Act
        viewModel = ImageDetailViewModel(mockSavedStateHandle)

        // Assert
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(testImageId, initialState.imageId)
            assertEquals(testImageUrl, initialState.imageUrl)
        }
    }

    @Test
    fun `initial state should handle empty imageUrl`() = runTest {
        // Arrange
        val testImageId = 789L
        val testImageUrl = ""
        mockSavedStateHandle = mockk {
            every { get<Long>("imageId") } returns testImageId
            every { get<String>("imageUrl") } returns testImageUrl
        }

        // Act
        viewModel = ImageDetailViewModel(mockSavedStateHandle)

        // Assert
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(testImageId, initialState.imageId)
            assertEquals(testImageUrl, initialState.imageUrl)
        }
    }

    @Test
    fun `initial state should handle large imageId`() = runTest {
        // Arrange
        val testImageId = Long.MAX_VALUE
        val testImageUrl = "https://example.com/large-image.jpg"
        mockSavedStateHandle = mockk {
            every { get<Long>("imageId") } returns testImageId
            every { get<String>("imageUrl") } returns testImageUrl
        }

        // Act
        viewModel = ImageDetailViewModel(mockSavedStateHandle)

        // Assert
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(testImageId, initialState.imageId)
            assertEquals(testImageUrl, initialState.imageUrl)
        }
    }

    @Test
    fun `initial state should handle negative imageId`() = runTest {
        // Arrange
        val testImageId = -1L
        val testImageUrl = "https://example.com/negative-image.jpg"
        mockSavedStateHandle = mockk {
            every { get<Long>("imageId") } returns testImageId
            every { get<String>("imageUrl") } returns testImageUrl
        }

        // Act
        viewModel = ImageDetailViewModel(mockSavedStateHandle)

        // Assert
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(testImageId, initialState.imageId)
            assertEquals(testImageUrl, initialState.imageUrl)
        }
    }

    @Test
    fun `initial state should handle complex imageUrl`() = runTest {
        // Arrange
        val testImageId = 999L
        val testImageUrl = "https://example.com/path/to/image.jpg?param=value&another=param#fragment"
        mockSavedStateHandle = mockk {
            every { get<Long>("imageId") } returns testImageId
            every { get<String>("imageUrl") } returns testImageUrl
        }

        // Act
        viewModel = ImageDetailViewModel(mockSavedStateHandle)

        // Assert
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(testImageId, initialState.imageId)
            assertEquals(testImageUrl, initialState.imageUrl)
        }
    }

    @Test
    fun `ImageDetailUiState should have correct default values`() {
        // Act
        val uiState = ImageDetailUiState()

        // Assert
        assertNull(uiState.imageId)
        assertNull(uiState.imageUrl)
    }

    @Test
    fun `ImageDetailUiState should copy correctly`() {
        // Arrange
        val originalState = ImageDetailUiState()
        val testImageId = 123L
        val testImageUrl = "https://example.com/copied-image.jpg"

        // Act
        val newState = originalState.copy(
            imageId = testImageId,
            imageUrl = testImageUrl
        )

        // Assert
        assertEquals(testImageId, newState.imageId)
        assertEquals(testImageUrl, newState.imageUrl)
    }

    @Test
    fun `ImageDetailUiState should copy with partial parameters`() {
        // Arrange
        val originalState = ImageDetailUiState(
            imageId = 456L,
            imageUrl = "https://example.com/original-image.jpg"
        )

        // Act
        val newState = originalState.copy(imageId = 789L)

        // Assert
        assertEquals(789L, newState.imageId)
        assertEquals("https://example.com/original-image.jpg", newState.imageUrl)
    }

    @Test
    fun `uiState should emit single value and complete`() = runTest {
        // Arrange
        val testImageId = 111L
        val testImageUrl = "https://example.com/single-value.jpg"
        mockSavedStateHandle = mockk {
            every { get<Long>("imageId") } returns testImageId
            every { get<String>("imageUrl") } returns testImageUrl
        }

        // Act
        viewModel = ImageDetailViewModel(mockSavedStateHandle)

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(testImageId, state.imageId)
            assertEquals(testImageUrl, state.imageUrl)
            
            // Verify no more emissions
            expectNoEvents()
        }
    }

    @Test
    fun `uiState should be stable and not change after initialization`() = runTest {
        // Arrange
        val testImageId = 222L
        val testImageUrl = "https://example.com/stable-image.jpg"
        mockSavedStateHandle = mockk {
            every { get<Long>("imageId") } returns testImageId
            every { get<String>("imageUrl") } returns testImageUrl
        }

        // Act
        viewModel = ImageDetailViewModel(mockSavedStateHandle)

        // Assert
        viewModel.uiState.test {
            val firstState = awaitItem()
            assertEquals(testImageId, firstState.imageId)
            assertEquals(testImageUrl, firstState.imageUrl)
            
            // Should not emit any new values
            expectNoEvents()
        }
    }
}
