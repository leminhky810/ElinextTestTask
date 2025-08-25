package com.elinex.imagestesttask.domain.usecase

import com.elinex.imagestesttask.core.AppDispatchers
import com.elinex.imagestesttask.domain.model.ImageModel
import com.elinex.imagestesttask.domain.repository.ImageRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddImageUseCaseTest {

    private lateinit var addImageUseCase: AddImageUseCase
    private lateinit var mockGetRedirectUrlUseCase: GetRedirectUrlUseCase
    private lateinit var mockImageRepository: ImageRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        mockGetRedirectUrlUseCase = mockk()
        mockImageRepository = mockk()
        addImageUseCase = AddImageUseCase(
            getRedirectUrlUseCase = mockGetRedirectUrlUseCase,
            imageRepository = mockImageRepository,
            ioDispatcher = testDispatcher
        )
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke returns success when redirect URL is successful and image is added`() = runTest {
        // Given
        val redirectUrl = "https://example.com/redirected-image.jpg"
        val emptyRecord = ImageModel(id = 0, url = null)
        val updatedRecord = ImageModel(id = 0, url = redirectUrl)
        val finalRecord = ImageModel(id = 5L, url = redirectUrl)

        coEvery { mockImageRepository.addImage(emptyRecord) } returns emptyRecord
        coEvery { mockGetRedirectUrlUseCase() } returns Result.success(redirectUrl)
        coEvery { mockImageRepository.addImage(updatedRecord) } returns finalRecord

        // When
        val result = addImageUseCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(finalRecord, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when getRedirectUrlUseCase fails`() = runTest {
        // Given
        val exception = Exception("Network error")
        val emptyRecord = ImageModel(id = 0, url = null)

        coEvery { mockImageRepository.addImage(emptyRecord) } returns emptyRecord
        coEvery { mockGetRedirectUrlUseCase() } returns Result.failure(exception)

        // When
        val result = addImageUseCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `invoke returns failure when second addImage call throws exception`() = runTest {
        // Given
        val redirectUrl = "https://example.com/redirected-image.jpg"
        val emptyRecord = ImageModel(id = 0, url = null)
        val updatedRecord = ImageModel(id = 0, url = redirectUrl)
        val exception = Exception("Database error")

        coEvery { mockImageRepository.addImage(emptyRecord) } returns emptyRecord
        coEvery { mockGetRedirectUrlUseCase() } returns Result.success(redirectUrl)
        coEvery { mockImageRepository.addImage(updatedRecord) } throws exception

        // When
        val result = addImageUseCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `invoke creates empty record first then updates with redirect URL`() = runTest {
        // Given
        val redirectUrl = "https://example.com/redirected-image.jpg"
        val emptyRecord = ImageModel(id = 0, url = null)
        val updatedRecord = ImageModel(id = 0, url = redirectUrl)
        val finalRecord = ImageModel(id = 3L, url = redirectUrl)

        coEvery { mockImageRepository.addImage(emptyRecord) } returns emptyRecord
        coEvery { mockGetRedirectUrlUseCase() } returns Result.success(redirectUrl)
        coEvery { mockImageRepository.addImage(updatedRecord) } returns finalRecord

        // When
        val result = addImageUseCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(finalRecord, result.getOrNull())
    }

    @Test
    fun `invoke handles empty redirect URL`() = runTest {
        // Given
        val redirectUrl = ""
        val emptyRecord = ImageModel(id = 0, url = null)
        val updatedRecord = ImageModel(id = 0, url = redirectUrl)
        val finalRecord = ImageModel(id = 1L, url = redirectUrl)

        coEvery { mockImageRepository.addImage(emptyRecord) } returns emptyRecord
        coEvery { mockGetRedirectUrlUseCase() } returns Result.success(redirectUrl)
        coEvery { mockImageRepository.addImage(updatedRecord) } returns finalRecord

        // When
        val result = addImageUseCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(finalRecord, result.getOrNull())
    }

    @Test
    fun `invoke handles long redirect URL`() = runTest {
        // Given
        val redirectUrl = "https://very-long-url.example.com/with/many/path/segments/and/parameters?param1=value1&param2=value2&param3=value3"
        val emptyRecord = ImageModel(id = 0, url = null)
        val updatedRecord = ImageModel(id = 0, url = redirectUrl)
        val finalRecord = ImageModel(id = 2L, url = redirectUrl)

        coEvery { mockImageRepository.addImage(emptyRecord) } returns emptyRecord
        coEvery { mockGetRedirectUrlUseCase() } returns Result.success(redirectUrl)
        coEvery { mockImageRepository.addImage(updatedRecord) } returns finalRecord

        // When
        val result = addImageUseCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(finalRecord, result.getOrNull())
    }

    @Test
    fun `invoke handles different types of exceptions from getRedirectUrlUseCase`() = runTest {
        // Given
        val exceptions = listOf(
            Exception("Generic exception"),
            RuntimeException("Runtime exception"),
            IllegalArgumentException("Invalid argument"),
            IllegalStateException("Illegal state")
        )
        val emptyRecord = ImageModel(id = 0, url = null)

        coEvery { mockImageRepository.addImage(emptyRecord) } returns emptyRecord

        exceptions.forEach { exception ->
            coEvery { mockGetRedirectUrlUseCase() } returns Result.failure(exception)

            // When
            val result = addImageUseCase()

            // Then
            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
        }
    }

    @Test
    fun `invoke handles different types of exceptions from addImage`() = runTest {
        // Given
        val redirectUrl = "https://example.com/redirected-image.jpg"
        val emptyRecord = ImageModel(id = 0, url = null)
        val updatedRecord = ImageModel(id = 0, url = redirectUrl)
        val exceptions = listOf(
            Exception("Database exception"),
            RuntimeException("Runtime exception"),
            IllegalArgumentException("Invalid argument"),
            IllegalStateException("Illegal state")
        )

        coEvery { mockImageRepository.addImage(emptyRecord) } returns emptyRecord
        coEvery { mockGetRedirectUrlUseCase() } returns Result.success(redirectUrl)

        exceptions.forEach { exception ->
            coEvery { mockImageRepository.addImage(updatedRecord) } throws exception

            // When
            val result = addImageUseCase()

            // Then
            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
        }
    }
}

