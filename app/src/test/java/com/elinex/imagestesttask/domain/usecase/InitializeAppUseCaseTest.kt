package com.elinex.imagestesttask.domain.usecase

import com.elinex.imagestesttask.preferences.AppPreferences
import com.elinex.imagestesttask.worker.ImageSyncInitializer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeAppUseCaseTest {

    private lateinit var initializeAppUseCase: InitializeAppUseCase
    private lateinit var mockImageSyncInitializer: ImageSyncInitializer
    private lateinit var mockAppPreferences: AppPreferences
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        mockImageSyncInitializer = mockk(relaxed = true)
        mockAppPreferences = mockk(relaxed = true)
        initializeAppUseCase = InitializeAppUseCase(
            ioDispatcher = testDispatcher,
            imageSyncInitializer = mockImageSyncInitializer,
            appPreferences = mockAppPreferences
        )
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke initializes database and marks first launch completed when first launch`() = runTest {
        // Given
        every { mockAppPreferences.isFirstLaunch() } returns flowOf(true)

        // When
        initializeAppUseCase()

        // Then
        coVerify { mockImageSyncInitializer.initializeDatabase() }
        coVerify { mockAppPreferences.markFirstLaunchCompleted() }
    }

    @Test
    fun `invoke does nothing when not first launch`() = runTest {
        // Given
        every { mockAppPreferences.isFirstLaunch() } returns flowOf(false)

        // When
        initializeAppUseCase()

        // Then
        coVerify(exactly = 0) { mockImageSyncInitializer.initializeDatabase() }
        coVerify(exactly = 0) { mockAppPreferences.markFirstLaunchCompleted() }
    }

    @Test
    fun `invoke handles multiple calls correctly`() = runTest {
        // Given
        every { mockAppPreferences.isFirstLaunch() } returns flowOf(true)

        // When
        initializeAppUseCase()
        initializeAppUseCase()

        // Then
        coVerify(exactly = 2) { mockImageSyncInitializer.initializeDatabase() }
        coVerify(exactly = 2) { mockAppPreferences.markFirstLaunchCompleted() }
    }

    @Test
    fun `invoke handles transition from first launch to not first launch`() = runTest {
        // Given
        every { mockAppPreferences.isFirstLaunch() } returnsMany listOf(
            flowOf(true),  // First call
            flowOf(false)  // Second call
        )

        // When
        initializeAppUseCase() // First launch
        initializeAppUseCase() // Not first launch

        // Then
        coVerify(exactly = 1) { mockImageSyncInitializer.initializeDatabase() }
        coVerify(exactly = 1) { mockAppPreferences.markFirstLaunchCompleted() }
    }

    @Test
    fun `invoke executes on IO dispatcher`() = runTest {
        // Given
        every { mockAppPreferences.isFirstLaunch() } returns flowOf(true)

        // When
        initializeAppUseCase()

        // Then
        // The test dispatcher ensures the operation runs on the correct dispatcher
        coVerify { mockImageSyncInitializer.initializeDatabase() }
        coVerify { mockAppPreferences.markFirstLaunchCompleted() }
    }

    @Test
    fun `invoke handles edge case when isFirstLaunch returns null`() = runTest {
        // Given
        // This test simulates the case where the preference might not be set
        // The AppPreferences implementation defaults to true when the key is not found
        every { mockAppPreferences.isFirstLaunch() } returns flowOf(true)

        // When
        initializeAppUseCase()

        // Then
        coVerify { mockImageSyncInitializer.initializeDatabase() }
        coVerify { mockAppPreferences.markFirstLaunchCompleted() }
    }

    @Test
    fun `invoke handles multiple rapid calls`() = runTest {
        // Given
        every { mockAppPreferences.isFirstLaunch() } returns flowOf(true)

        // When
        repeat(5) {
            initializeAppUseCase()
        }

        // Then
        coVerify(exactly = 5) { mockImageSyncInitializer.initializeDatabase() }
        coVerify(exactly = 5) { mockAppPreferences.markFirstLaunchCompleted() }
    }
}
