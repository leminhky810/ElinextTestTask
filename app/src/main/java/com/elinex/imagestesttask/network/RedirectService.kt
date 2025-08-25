package com.elinex.imagestesttask.network

import com.elinex.imagestesttask.BuildConfig
import com.elinex.imagestesttask.core.AppDispatchers
import com.elinex.imagestesttask.core.Dispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID

/**
 * Service responsible for handling URL redirects for the application's API.
 * 
 * This service manages HTTP redirects by following redirect responses from the
 * configured API endpoint. It implements robust error handling and retry logic
 * to ensure reliable URL resolution for image loading.
 * 
 * Key features:
 * - Follows HTTP redirects (301, 302, 303, 307, 308)
 * - Implements exponential backoff retry mechanism
 * - Handles network failures gracefully
 * - Uses dependency injection for configuration
 * - Operates on IO dispatcher for network operations
 * 
 * The service is used to resolve the final URLs for images by following
 * any redirects that the API might return, ensuring that the application
 * can load images from the correct endpoints.
 * 
 * @param ioDispatcher Coroutine dispatcher for IO operations
 * @param okHttpClient HTTP client for network requests
 * @param apiUrl Base API URL for redirect resolution
 * @param maxRetries Maximum number of retry attempts for failed requests
 * @param baseDelayMs Base delay for exponential backoff calculation
 * 
 * @author Elinext Test Task Team
 * @since 1.0.0
 */
class RedirectService(
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val okHttpClient: OkHttpClient,
    private val apiUrl: String = BuildConfig.API_URL,
    private val maxRetries: Int = 3,
    private val baseDelayMs: Long = 1000L
) {

    /**
     * Follows redirects for the configured API URL and returns the final URL.
     * 
     * This method performs HTTP requests to the configured API endpoint and
     * follows any redirect responses to determine the final URL. It implements
     * a retry mechanism with exponential backoff to handle network failures.
     * 
     * The method handles various HTTP response codes:
     * - 301, 302, 303, 307, 308: Redirect responses (follows Location header)
     * - 200: Successful response (returns original URL)
     * - Other codes: Treated as errors with retry logic
     * 
     * @return Result containing the final URL on success or an error on failure
     * 
     * @throws Exception When network requests fail after all retry attempts
     */
    suspend fun getRedirectUrl(): Result<String> {
        return withContext(ioDispatcher) {
            var lastException: Exception? = null
            
            repeat(maxRetries + 1) { attempt ->
                try {
                    val request = Request.Builder()
//                    .url("$apiUrl/?random=${UUID.randomUUID()}")
                        .url(apiUrl)
                        .get()
                        .build()

                    okHttpClient.newCall(request).execute().use { response ->
                        when (response.code) {
                            301, 302, 303, 307, 308 -> {
                                // Handle redirect responses
                                val locationHeader = response.header("Location")
                                if (locationHeader != null) {
                                    return@withContext Result.success(locationHeader)
                                } else {
                                    val exception = Exception("Redirect response but no Location header found")
                                    if (attempt < maxRetries) {
                                        lastException = exception
                                        delay(calculateDelay(attempt))
                                        return@repeat
                                    } else {
                                        return@withContext Result.failure(exception)
                                    }
                                }
                            }
                            200 -> {
                                // Some services might return 200 with the final URL
                                // In this case, we return the original URL since no redirect occurred
                                return@withContext Result.success(apiUrl)
                            }
                            else -> {
                                val exception = Exception("Unexpected response code: ${response.code}")
                                if (attempt < maxRetries) {
                                    lastException = exception
                                    delay(calculateDelay(attempt))
                                    return@repeat
                                } else {
                                    return@withContext Result.failure(exception)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    lastException = e
                    if (attempt < maxRetries) {
                        delay(calculateDelay(attempt))
                    } else {
                        return@withContext Result.failure(e)
                    }
                }
            }
            
            Result.failure(lastException ?: Exception("Unknown error occurred"))
        }
    }

    /**
     * Calculates delay for retry attempts using exponential backoff.
     * 
     * This method implements exponential backoff strategy to prevent overwhelming
     * the server with rapid retry attempts. The delay increases exponentially
     * with each retry attempt: baseDelay * 2^attempt.
     * 
     * Example delays with baseDelayMs = 1000:
     * - Attempt 0: 1000ms
     * - Attempt 1: 2000ms
     * - Attempt 2: 4000ms
     * 
     * @param attempt Current attempt number (0-based)
     * @return Delay in milliseconds for the next retry attempt
     */
    private fun calculateDelay(attempt: Int): Long {
        return baseDelayMs * (1L shl attempt) // 2^attempt * baseDelayMs
    }

}
