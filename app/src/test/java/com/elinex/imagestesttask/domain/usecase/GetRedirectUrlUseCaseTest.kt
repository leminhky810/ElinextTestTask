package com.elinex.imagestesttask.domain.usecase

import com.elinex.imagestesttask.domain.repository.ImageRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetRedirectUrlUseCaseTest {

    private lateinit var getRedirectUrlUseCase: GetRedirectUrlUseCase
    private lateinit var mockImageRepository: ImageRepository

    @Before
    fun setUp() {
        mockImageRepository = mockk()
        getRedirectUrlUseCase = GetRedirectUrlUseCase(mockImageRepository)
    }

    @Test
    fun `invoke returns success when repository returns success`() = runTest {
        // Given
        val expectedUrl = "https://example.com/redirected-image.jpg"
        val expectedResult = Result.success(expectedUrl)
        coEvery { mockImageRepository.getRedirectUrl() } returns expectedResult

        // When
        val result = getRedirectUrlUseCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUrl, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        // Given
        val exception = Exception("Network error")
        val expectedResult = Result.failure<String>(exception)
        coEvery { mockImageRepository.getRedirectUrl() } returns expectedResult

        // When
        val result = getRedirectUrlUseCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `invoke delegates to repository getRedirectUrl method`() = runTest {
        // Given
        val expectedUrl = "https://example.com/test-image.jpg"
        val expectedResult = Result.success(expectedUrl)
        coEvery { mockImageRepository.getRedirectUrl() } returns expectedResult

        // When
        val result = getRedirectUrlUseCase()

        // Then
        assertEquals(expectedResult, result)
    }

    @Test
    fun `invoke handles empty string from repository`() = runTest {
        // Given
        val expectedUrl = ""
        val expectedResult = Result.success(expectedUrl)
        coEvery { mockImageRepository.getRedirectUrl() } returns expectedResult

        // When
        val result = getRedirectUrlUseCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUrl, result.getOrNull())
    }

    @Test
    fun `invoke handles long URL from repository`() = runTest {
        // Given
        val expectedUrl = "https://very-long-url.example.com/with/many/path/segments/and/parameters?param1=value1&param2=value2&param3=value3"
        val expectedResult = Result.success(expectedUrl)
        coEvery { mockImageRepository.getRedirectUrl() } returns expectedResult

        // When
        val result = getRedirectUrlUseCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUrl, result.getOrNull())
    }

    @Test
    fun `invoke handles different types of exceptions`() = runTest {
        // Given
        val exceptions = listOf(
            Exception("Generic exception"),
            RuntimeException("Runtime exception"),
            IllegalArgumentException("Invalid argument"),
            IllegalStateException("Illegal state")
        )

        exceptions.forEach { exception ->
            val expectedResult = Result.failure<String>(exception)
            coEvery { mockImageRepository.getRedirectUrl() } returns expectedResult

            // When
            val result = getRedirectUrlUseCase()

            // Then
            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
        }
    }
}

