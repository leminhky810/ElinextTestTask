package com.elinex.imagestesttask.domain.usecase

import com.elinex.imagestesttask.core.AppDispatchers
import com.elinex.imagestesttask.core.Dispatcher
import com.elinex.imagestesttask.preferences.AppPreferences
import com.elinex.imagestesttask.worker.ImageSyncInitializer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InitializeAppUseCase @Inject constructor(
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val imageSyncInitializer: ImageSyncInitializer,
    private val appPreferences: AppPreferences
) {
    suspend operator fun invoke() = withContext(ioDispatcher) {
        val isFirstLaunch = appPreferences.isFirstLaunch().first()
        if (!isFirstLaunch) return@withContext
        imageSyncInitializer.initializeDatabase()
        appPreferences.markFirstLaunchCompleted()
    }
}