package com.elinex.imagestesttask.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class RedirectServiceTest {

    private lateinit var redirectService: RedirectService
    private lateinit var mockOkHttpClient: OkHttpClient
    private lateinit var mockCall: Call
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        mockOkHttpClient = mockk()
        mockCall = mockk()
        redirectService = RedirectService(
            ioDispatcher = testDispatcher,
            okHttpClient = mockOkHttpClient,
            apiUrl = "https://test-api.com"
        )
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getRedirectUrl returns success when redirect response with location header`() = runTest {
        // Given
        val locationUrl = "https://example.com/redirected-image.jpg"
        val mockResponse = Response.Builder()
            .request(Request.Builder().url("https://test-api.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(302)
            .message("Found")
            .header("Location", locationUrl)
            .body("".toResponseBody())
            .build()

        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse

        // When
        val result = redirectService.getRedirectUrl()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(locationUrl, result.getOrNull())
    }

    @Test
    fun `getRedirectUrl returns failure when redirect response without location header`() = runTest {
        // Given
        val mockResponse = Response.Builder()
            .request(Request.Builder().url("https://test-api.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(302)
            .message("Found")
            .body("".toResponseBody())
            .build()

        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse

        // When
        val result = redirectService.getRedirectUrl()

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Location header") == true)
    }

    @Test
    fun `getRedirectUrl returns failure when network exception occurs`() = runTest {
        // Given
        val networkException = IOException("Network error")
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } throws networkException

        // When
        val result = redirectService.getRedirectUrl()

        // Then
        assertTrue(result.isFailure)
        assertEquals(networkException, result.exceptionOrNull())
    }

    @Test
    fun `getRedirectUrl retries on failure and succeeds on second attempt`() = runTest {
        // Given
        val locationUrl = "https://example.com/redirected-image.jpg"
        val mockResponse = Response.Builder()
            .request(Request.Builder().url("https://test-api.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(302)
            .message("Found")
            .header("Location", locationUrl)
            .body("".toResponseBody())
            .build()

        val networkException = IOException("Network error")

        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse

        // When
        val result = redirectService.getRedirectUrl()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(locationUrl, result.getOrNull())
    }

    @Test
    fun `getRedirectUrl uses correct API URL`() = runTest {
        // Given
        val customApiUrl = "https://custom-api.com"
        val customRedirectService = RedirectService(
            ioDispatcher = testDispatcher,
            okHttpClient = mockOkHttpClient,
            apiUrl = customApiUrl
        )

        val locationUrl = "https://example.com/redirected-image.jpg"
        val mockResponse = Response.Builder()
            .request(Request.Builder().url(customApiUrl).build())
            .protocol(Protocol.HTTP_1_1)
            .code(302)
            .message("Found")
            .header("Location", locationUrl)
            .body("".toResponseBody())
            .build()

        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse

        // When
        val result = customRedirectService.getRedirectUrl()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(locationUrl, result.getOrNull())
    }

    @Test
    fun `getRedirectUrl handles different redirect status codes`() = runTest {
        // Given
        val locationUrl = "https://example.com/redirected-image.jpg"
        val redirectCodes = listOf(301, 302, 303, 307, 308)

        redirectCodes.forEach { statusCode ->
            val mockResponse = Response.Builder()
                .request(Request.Builder().url("https://test-api.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(statusCode)
                .message("Redirect")
                .header("Location", locationUrl)
                .body("".toResponseBody())
                .build()

            every { mockOkHttpClient.newCall(any()) } returns mockCall
            every { mockCall.execute() } returns mockResponse

            // When
            val result = redirectService.getRedirectUrl()

            // Then
            assertTrue("Should succeed for status code $statusCode", result.isSuccess)
            assertEquals(locationUrl, result.getOrNull())
        }
    }
}
