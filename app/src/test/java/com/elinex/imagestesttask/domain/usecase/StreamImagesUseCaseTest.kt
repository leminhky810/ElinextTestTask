package com.elinex.imagestesttask.domain.usecase

import com.elinex.imagestesttask.domain.model.ImageModel
import com.elinex.imagestesttask.domain.repository.ImageRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StreamImagesUseCaseTest {

    private lateinit var streamImagesUseCase: StreamImagesUseCase
    private lateinit var mockImageRepository: ImageRepository

    @Before
    fun setUp() {
        mockImageRepository = mockk()
        streamImagesUseCase = StreamImagesUseCase(mockImageRepository)
    }

    @Test
    fun `invoke returns flow from repository`() = runTest {
        // Given
        val expectedImages = listOf(
            ImageModel(id = 1L, url = "https://example.com/image1.jpg"),
            ImageModel(id = 2L, url = "https://example.com/image2.jpg"),
            ImageModel(id = 3L, url = "https://example.com/image3.jpg")
        )
        every { mockImageRepository.streamImages() } returns flowOf(expectedImages)

        // When
        val result = streamImagesUseCase()

        // Then
        result.collect { images ->
            assertEquals(expectedImages.size, images.size)
            assertEquals(expectedImages[0], images[0])
            assertEquals(expectedImages[1], images[1])
            assertEquals(expectedImages[2], images[2])
        }
    }

    @Test
    fun `invoke returns empty flow when repository returns empty list`() = runTest {
        // Given
        every { mockImageRepository.streamImages() } returns flowOf(emptyList())

        // When
        val result = streamImagesUseCase()

        // Then
        result.collect { images ->
            assertTrue(images.isEmpty())
        }
    }

    @Test
    fun `invoke returns flow with single image`() = runTest {
        // Given
        val expectedImage = ImageModel(id = 1L, url = "https://example.com/single-image.jpg")
        every { mockImageRepository.streamImages() } returns flowOf(listOf(expectedImage))

        // When
        val result = streamImagesUseCase()

        // Then
        result.collect { images ->
            assertEquals(1, images.size)
            assertEquals(expectedImage, images[0])
        }
    }

    @Test
    fun `invoke returns flow with images containing null urls`() = runTest {
        // Given
        val expectedImages = listOf(
            ImageModel(id = 1L, url = "https://example.com/image1.jpg"),
            ImageModel(id = 2L, url = null),
            ImageModel(id = 3L, url = "https://example.com/image3.jpg")
        )
        every { mockImageRepository.streamImages() } returns flowOf(expectedImages)

        // When
        val result = streamImagesUseCase()

        // Then
        result.collect { images ->
            assertEquals(expectedImages.size, images.size)
            assertEquals(expectedImages[0], images[0])
            assertEquals(expectedImages[1], images[1])
            assertEquals(expectedImages[2], images[2])
            assertNull(images[1].url)
        }
    }

    @Test
    fun `invoke delegates to repository streamImages method`() = runTest {
        // Given
        val expectedImages = listOf(
            ImageModel(id = 1L, url = "https://example.com/image1.jpg")
        )
        every { mockImageRepository.streamImages() } returns flowOf(expectedImages)

        // When
        val result = streamImagesUseCase()

        // Then
        result.collect { images ->
            assertEquals(expectedImages, images)
        }
    }
}

