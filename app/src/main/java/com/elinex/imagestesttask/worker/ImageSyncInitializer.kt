package com.elinex.imagestesttask.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager

/**
 * Initializer class for managing image synchronization background work.
 * 
 * This class provides a centralized way to manage the background work
 * responsible for initializing and synchronizing the image database.
 * It uses WorkManager to ensure reliable execution of background tasks
 * even when the app is not in the foreground.
 * 
 * Key features:
 * - Manages unique background work to prevent duplicate execution
 * - Provides status checking for ongoing work
 * - Uses WorkManager for reliable background task execution
 * - Implements singleton work policy to ensure single execution
 * 
 * The initializer is used by the application to start database
 * population and synchronization tasks in the background, ensuring
 * that the app has data available for display.
 * 
 * @param context Application context for WorkManager initialization
 * 
 * @author Elinext Test Task Team
 * @since 1.0.0
 */
class ImageSyncInitializer(context: Context) {

    /**
     * WorkManager instance for managing background work.
     */
    private val workManager = WorkManager.getInstance(context)

    /**
     * Starts the database initialization process.
     * 
     * This method enqueues a unique background work task that will
     * populate the database with image data. The work is configured
     * to replace any existing work using ExistingWorkPolicy.REPLACE,
     * ensuring fresh execution when called.
     * 
     * The initialization process:
     * - Fetches image data from the network
     * - Stores the data in the local database
     * - Handles errors and retries automatically
     * - Updates the UI when complete
     * 
     * @see ImageSyncWorker
     * @see androidx.work.ExistingWorkPolicy.REPLACE
     */
    fun initializeDatabase() {
        workManager.enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE, // Replace existing work if running
            ImageSyncWorker.imageSyncWork()
        )
    }

    /**
     * Checks if the database initialization work is currently running.
     * 
     * This method queries WorkManager to determine if the background
     * initialization task is currently in progress. It checks all work
     * infos for the unique work and returns true if any work is not
     * in a finished state.
     * 
     * @return true if initialization work is currently running, false otherwise
     * 
     * @see androidx.work.WorkInfo.State
     */
    fun isInitializationRunning(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(WORK_NAME).get()
        return workInfos.any { !it.state.isFinished }
    }

    /**
     * Companion object containing constants and shared configuration.
     * 
     * This object provides:
     * - Unique work name for database initialization
     * - Shared constants used across the worker system
     */
    companion object Companion {
        /**
         * Unique name for the image database initialization work.
         * This name is used to identify and manage the background work
         * in WorkManager, ensuring that only one instance runs at a time.
         */
        const val WORK_NAME = "image_database_work"

    }
}
