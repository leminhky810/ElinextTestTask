package com.elinex.imagestesttask.presentation.home

import app.cash.turbine.test
import com.elinex.imagestesttask.core.ITEMS_PER_PAGE
import com.elinex.imagestesttask.domain.model.ImageModel
import com.elinex.imagestesttask.domain.usecase.AddImageUseCase
import com.elinex.imagestesttask.domain.usecase.StreamImagesUseCase
import com.elinex.imagestesttask.worker.ImageSyncInitializer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var mockStreamImagesUseCase: StreamImagesUseCase
    private lateinit var mockImageSyncInitializer: ImageSyncInitializer
    private lateinit var mockAddImageUseCase: AddImageUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockStreamImagesUseCase = mockk(relaxed = true)
        mockImageSyncInitializer = mockk(relaxed = true)
        mockAddImageUseCase = mockk(relaxed = true)
        
        viewModel = HomeViewModel(
            streamImagesUseCase = mockStreamImagesUseCase,
            initializer = mockImageSyncInitializer,
            addImageUseCase = mockAddImageUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have default values`() = runTest {
        every { mockStreamImagesUseCase() } returns flowOf(emptyList())
        
        viewModel.uiState.test {
            val initialState = awaitItem()
            
            assertTrue(initialState.images.isEmpty())
            assertFalse(initialState.isLoading)
            assertFalse(initialState.isEmpty)
        }
    }

    @Test
    fun `loadImages should emit loading state initially`() = runTest {
        every { mockStreamImagesUseCase() } returns flowOf(emptyList())
        
        viewModel.uiState.test {
            // Initial state
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)
            
            // Final state after loading
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            assertTrue(finalState.isEmpty)
        }
    }

    @Test
    fun `loadImages should emit images when streamImagesUseCase returns data`() = runTest {
        val testImages = listOf(
            ImageModel(id = 1L, url = "https://example.com/image1.jpg"),
            ImageModel(id = 2L, url = "https://example.com/image2.jpg")
        )
        
        every { mockStreamImagesUseCase() } returns flowOf(testImages)
        
        viewModel.uiState.test {
            // Initial state
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)
            
            // Final state with data
            val finalState = awaitItem()
            assertEquals(2, finalState.images.size)
            assertFalse(finalState.isEmpty)
            assertFalse(finalState.isLoading)
            
            // Verify images are not padded
            assertEquals(1L, finalState.images[0].id)
            assertEquals(2L, finalState.images[1].id)
        }
    }

    @Test
    fun `loadImages should handle empty images list`() = runTest {
        every { mockStreamImagesUseCase() } returns flowOf(emptyList())
        
        viewModel.uiState.test {
            // Initial state
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)
            
            // Final state
            val finalState = awaitItem()
            assertTrue(finalState.images.isEmpty())
            assertTrue(finalState.isEmpty)
            assertFalse(finalState.isLoading)
        }
    }

    @Test
    fun `loadImages should not pad images to full pages`() = runTest {
        val testImages = listOf(
            ImageModel(id = 1L, url = "https://example.com/image1.jpg"),
            ImageModel(id = 2L, url = "https://example.com/image2.jpg")
        )
        
        every { mockStreamImagesUseCase() } returns flowOf(testImages)
        
        viewModel.uiState.test {
            // Initial state
            awaitItem()
            // Final state
            val finalState = awaitItem()
            
            assertEquals(2, finalState.images.size)
            
            // Items should be the original images without padding
            assertEquals(1L, finalState.images[0].id)
            assertEquals(2L, finalState.images[1].id)
        }
    }

    @Test
    fun `loadImages should handle exact page size images`() = runTest {
        val testImages = List(ITEMS_PER_PAGE) { index ->
            ImageModel(id = index.toLong(), url = "https://example.com/image$index.jpg")
        }
        
        every { mockStreamImagesUseCase() } returns flowOf(testImages)
        
        viewModel.uiState.test {
            // Initial state
            awaitItem()
            // Final state
            val finalState = awaitItem()
            
            assertEquals(ITEMS_PER_PAGE, finalState.images.size)
            
            // All items should be original images
            finalState.images.forEachIndexed { index, image ->
                assertEquals(index.toLong(), image.id)
                assertEquals("https://example.com/image$index.jpg", image.url)
            }
        }
    }

    @Test
    fun `refreshImages should initialize database when worker is not running`() = runTest {
        every { mockImageSyncInitializer.isInitializationRunning() } returns false
        
        viewModel.refreshImages()
        
        // Wait for the coroutine to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify { mockImageSyncInitializer.initializeDatabase() }
    }

    @Test
    fun `state should update when streamImagesUseCase emits new data`() = runTest {
        val firstImages = listOf(
            ImageModel(id = 1L, url = "https://example.com/image1.jpg")
        )
        val secondImages = listOf(
            ImageModel(id = 1L, url = "https://example.com/image1.jpg"),
            ImageModel(id = 2L, url = "https://example.com/image2.jpg")
        )
        
        val imagesFlow = MutableStateFlow(firstImages)
        every { mockStreamImagesUseCase() } returns imagesFlow
        
        viewModel.uiState.test {
            // Initial state
            awaitItem()
            // First state
            val firstState = awaitItem()
            assertEquals(1, firstState.images.count { it.id != -1L })
            
            // Update with more images
            imagesFlow.value = secondImages
            
            // Second state
            val secondState = awaitItem()
            assertEquals(2, secondState.images.count { it.id != -1L })
        }
    }

    @Test
    fun `HomeUiState should have correct default values`() {
        val uiState = HomeUiState()
        
        assertTrue(uiState.images.isEmpty())
        assertFalse(uiState.isLoading)
        assertFalse(uiState.isEmpty)
    }

    @Test
    fun `HomeUiState should copy correctly`() {
        val originalState = HomeUiState()
        val testImages = listOf(ImageModel(id = 1L, url = "test.jpg"))
        
        val newState = originalState.copy(
            images = testImages,
            isLoading = true,
            isEmpty = false
        )
        
        assertEquals(testImages, newState.images)
        assertTrue(newState.isLoading)
        assertFalse(newState.isEmpty)
    }
}
