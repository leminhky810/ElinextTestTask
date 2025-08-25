package com.elinex.imagestesttask

import android.app.Application
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.elinex.imagestesttask.core.AppDispatchers
import com.elinex.imagestesttask.core.Dispatcher
import com.elinex.imagestesttask.core.di.HiltWorkerFactoryEntryPoint
import com.elinex.imagestesttask.domain.usecase.InitializeAppUseCase
import dagger.hilt.EntryPoints
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main Application class for the Images Test Task application.
 * 
 * This class serves as the entry point for the Android application and provides:
 * - Dependency injection setup using Hilt
 * - WorkManager configuration for background tasks
 * - Image loading configuration using Coil
 * - Application initialization logic
 * 
 * Key responsibilities:
 * - Initializes the application with required dependencies
 * - Configures WorkManager for background image synchronization
 * - Sets up Coil image loader for efficient image loading and caching
 * - Manages application lifecycle and startup tasks
 * 
 * The class implements multiple interfaces:
 * - Configuration.Provider: For WorkManager configuration
 * - SingletonImageLoader.Factory: For Coil image loading setup
 * 
 * @author Elinext Test Task Team
 * @since 1.0.0
 */
@HiltAndroidApp
class ImagesApplication : Application(), Configuration.Provider, SingletonImageLoader.Factory {

    @Inject
    lateinit var initializeAppUseCase: InitializeAppUseCase

    @Inject
    @Dispatcher(AppDispatchers.IO)
    lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    lateinit var imageLoader: dagger.Lazy<ImageLoader>

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob() + ioDispatcher).launch {
            initializeAppUseCase()
        }
    }

    override val workManagerConfiguration: Configuration =
        Configuration.Builder()
            .setWorkerFactory(
                EntryPoints.get(this, HiltWorkerFactoryEntryPoint::class.java).workerFactory()
            )
            .build()

    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader.get()

}