package com.elinex.imagestesttask.data.repository

import com.elinex.imagestesttask.database.ImageDao
import com.elinex.imagestesttask.database.ImageEntity
import com.elinex.imagestesttask.domain.model.ImageModel
import com.elinex.imagestesttask.network.RedirectService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ImageRepositoryImplTest {

    private lateinit var imageRepository: ImageRepositoryImpl
    private lateinit var imageDao: ImageDao
    private lateinit var redirectService: RedirectService

    @Before
    fun setUp() {
        imageDao = mockk()
        redirectService = mockk()
        imageRepository = ImageRepositoryImpl(imageDao, redirectService)
    }

    @Test
    fun `streamImages returns mapped domain models`() = runTest {
        // Given
        val entities = listOf(
            ImageEntity(id = 1L, redirectUrl = "https://example.com/image1.jpg"),
            ImageEntity(id = 2L, redirectUrl = "https://example.com/image2.jpg")
        )
        val expectedModels = listOf(
            ImageModel(id = 1L, url = "https://example.com/image1.jpg"),
            ImageModel(id = 2L, url = "https://example.com/image2.jpg")
        )

        coEvery { imageDao.streamImages() } returns flowOf(entities)

        // When
        val result = imageRepository.streamImages()

        // Then
        result.collect { models ->
            assertEquals(expectedModels.size, models.size)
            assertEquals(expectedModels[0], models[0])
            assertEquals(expectedModels[1], models[1])
        }
    }

    @Test
    fun `streamImages returns empty list when dao returns empty list`() = runTest {
        // Given
        coEvery { imageDao.streamImages() } returns flowOf(emptyList())

        // When
        val result = imageRepository.streamImages()

        // Then
        result.collect { models ->
            assertTrue(models.isEmpty())
        }
    }

    @Test
    fun `addImage maps domain model to entity and returns updated model`() = runTest {
        // Given
        val inputModel = ImageModel(id = 0L, url = "https://example.com/new-image.jpg")
        val expectedEntity = ImageEntity(id = 0L, redirectUrl = "https://example.com/new-image.jpg")
        val insertedId = 5L
        val expectedModel = inputModel.copy(id = insertedId)

        coEvery { imageDao.insertImage(expectedEntity) } returns insertedId

        // When
        val result = imageRepository.addImage(inputModel)

        // Then
        assertEquals(expectedModel, result)
    }

    @Test
    fun `addImage handles null url correctly`() = runTest {
        // Given
        val inputModel = ImageModel(id = 0L, url = null)
        val expectedEntity = ImageEntity(id = 0L, redirectUrl = null)
        val insertedId = 3L
        val expectedModel = inputModel.copy(id = insertedId)

        coEvery { imageDao.insertImage(expectedEntity) } returns insertedId

        // When
        val result = imageRepository.addImage(inputModel)

        // Then
        assertEquals(expectedModel, result)
    }

    @Test
    fun `getRedirectUrl delegates to redirectService`() = runTest {
        // Given
        val expectedUrl = "https://example.com/redirected-image.jpg"
        val expectedResult = Result.success(expectedUrl)

        coEvery { redirectService.getRedirectUrl() } returns expectedResult

        // When
        val result = imageRepository.getRedirectUrl("https://example.com/source.jpg")

        // Then
        assertEquals(expectedResult, result)
    }

    @Test
    fun `getRedirectUrl handles failure from redirectService`() = runTest {
        // Given
        val exception = Exception("Network error")
        val expectedResult = Result.failure<String>(exception)

        coEvery { redirectService.getRedirectUrl() } returns expectedResult

        // When
        val result = imageRepository.getRedirectUrl("https://example.com/source.jpg")

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getRedirectUrl ignores sourceUrl parameter`() = runTest {
        // Given
        val expectedUrl = "https://example.com/redirected-image.jpg"
        val expectedResult = Result.success(expectedUrl)

        coEvery { redirectService.getRedirectUrl() } returns expectedResult

        // When
        val result = imageRepository.getRedirectUrl("https://example.com/source.jpg")

        // Then
        assertEquals(expectedResult, result)
    }

    @Test
    fun `getRedirectUrl works with null sourceUrl`() = runTest {
        // Given
        val expectedUrl = "https://example.com/redirected-image.jpg"
        val expectedResult = Result.success(expectedUrl)

        coEvery { redirectService.getRedirectUrl() } returns expectedResult

        // When
        val result = imageRepository.getRedirectUrl(null)

        // Then
        assertEquals(expectedResult, result)
    }
}
