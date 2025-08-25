package com.elinex.imagestesttask.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import com.elinex.imagestesttask.R
import com.elinex.imagestesttask.core.AppDispatchers
import com.elinex.imagestesttask.core.Dispatcher
import com.elinex.imagestesttask.core.COLUMN_EXPECTED
import com.elinex.imagestesttask.core.DEFAULT_TOTAL_RECORDS
import com.elinex.imagestesttask.core.utils.PermissionUtils
import com.elinex.imagestesttask.database.ImageDao
import com.elinex.imagestesttask.database.ImageEntity
import com.elinex.imagestesttask.domain.usecase.GetRedirectUrlUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.CancellationException

/**
 * Worker responsible for initializing the database on first app launch.
 * This worker:
 * 1. Clears existing data from the images table
 * 2. Inserts 140 empty records
 * 3. Loops through each record and fetches redirect URLs
 * 4. Updates each record with the fetched URL
 * 5. Uses WorkManager's Result.retry() with a maximum of 1 retry attempt
 */
@HiltWorker
class ImageSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val imageDao: ImageDao,
    private val getRedirectUrlUseCase: GetRedirectUrlUseCase
) : CoroutineWorker(context, workerParams) {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(0, DEFAULT_TOTAL_RECORDS)
    }

    @RequiresPermission(value = "android.permission.POST_NOTIFICATIONS")
    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        // Get current retry count from WorkManager
        val retryCount = runAttemptCount
        val maxRetries = 1 // Only retry once
        
        Log.d(TAG, "Starting database initialization (attempt ${retryCount + 1}/${maxRetries + 1})...")
        
        return@withContext runCatching {
            // Check for cancellation before starting
            if (!isActive) {
                Log.d(TAG, "Worker cancelled before starting")
                return@withContext Result.success()
            }
            
            // Show initial notification - getForegroundInfo() will handle the foreground service
            if (hasNotificationPermission()) {
                showProgressNotification(0, DEFAULT_TOTAL_RECORDS, 0, 0)
            } else {
                Log.w(TAG, "Notification permission not granted - notifications will not be shown")
            }

            // Step 1: Clear excess data, keeping first 140 records
            clearImages()

            // Step 2: Check how many records we have and insert if needed
            val existing = imageDao.getAllImages()
            val existingCount = existing.size
            val recordsToInsert = maxOf(0, DEFAULT_TOTAL_RECORDS - existingCount)

            if (recordsToInsert > 0) {
                Log.d(TAG, "Need to insert $recordsToInsert new records to reach $DEFAULT_TOTAL_RECORDS")
                val insertedIds = insertEmptyRecords(recordsToInsert)

                // Step 3: Populate all records (existing + new) with fresh redirect URLs
                populateRedirectUrls(insertedIds)
            } else {
                Log.d(TAG, "Already have $existingCount records, clearing URLs for fresh population")

                // Step 3: Clear URLs from existing records and populate with fresh URLs
                clearUrlsFromExistingRecords()
                val allRecordIds = existing.map { it.id }
                populateRedirectUrls(allRecordIds)
            }

            Log.d(TAG, "Database initialization completed successfully")
            if (hasNotificationPermission()) {
                showCompletionNotification(true)
            }
        }
        .onSuccess {
            Log.d(TAG, "Database initialization completed successfully")
        }
        .onFailure { exception ->
            when (exception) {
                is CancellationException -> {
                    Log.d(TAG, "Worker cancelled during execution")
                    throw exception // Re-throw cancellation
                }
                else -> {
                    if (retryCount < maxRetries) {
                        Log.e(TAG, "Database initialization failed on attempt ${retryCount + 1}/${maxRetries + 1}, will retry", exception)
                        if (hasNotificationPermission()) {
                            showCompletionNotification(false)
                        }
                    } else {
                        Log.e(TAG, "Database initialization failed after ${maxRetries + 1} attempts, giving up", exception)
                        if (hasNotificationPermission()) {
                            showCompletionNotification(false)
                        }
                    }
                }
            }
        }
        .fold(
            onSuccess = { Result.success() },
            onFailure = { 
                if (retryCount < maxRetries) {
                    // Return Result.retry() only if we haven't exceeded max retries
                    Log.d(TAG, "Returning Result.retry() for attempt ${retryCount + 2}/${maxRetries + 1}")
                    Result.retry()
                } else {
                    // Return Result.failure() after max retries exceeded
                    Log.d(TAG, "Max retries exceeded, returning Result.failure()")
                    Result.failure()
                }
            }
        )
    }

    /**
     * Clears excess data from the images table, keeping only the first 140 records.
     */
    private suspend fun clearImages() {
        Log.d(TAG, "Clearing excess database data, keeping first $DEFAULT_TOTAL_RECORDS records...")
        
        // Get all existing images
        val existingImages = imageDao.getAllImages()
        Log.d(TAG, "Found ${existingImages.size} existing images in database")
        
        if (existingImages.size > DEFAULT_TOTAL_RECORDS) {
            // Keep only the first DEFAULT_TOTAL_RECORDS records
            val imagesToKeep = existingImages.take(DEFAULT_TOTAL_RECORDS)
            val imagesToDelete = existingImages.drop(DEFAULT_TOTAL_RECORDS)
            
            Log.d(TAG, "Keeping ${imagesToKeep.size} images, deleting ${imagesToDelete.size} excess images")
            
            // Delete excess images
            imagesToDelete.forEach { image ->
                imageDao.deleteImage(image)
            }
            
            Log.d(TAG, "Excess images deleted, kept ${imagesToKeep.size} images with existing URLs")
        } else {
            Log.d(TAG, "No excess images to delete, database has ${existingImages.size} images")
        }
    }

    /**
     * Inserts empty records into the images table.
     * @param count Number of empty records to insert
     * @return List of inserted record IDs
     */
    private suspend fun insertEmptyRecords(count: Int): List<Long> {
        Log.d(TAG, "Inserting $count empty records...")

        val emptyEntities = (1..count).map {
            ImageEntity(
                id = 0, // Auto-generated
                redirectUrl = null
            )
        }

        val insertedIds = imageDao.insertImages(emptyEntities)

        Log.d(TAG, "Inserted ${insertedIds.size} empty records with null URLs")
        Log.d(TAG, "⚠️ WARNING: UI will show empty images until URLs are populated!")
        return insertedIds
    }

    /**
     * Clears URLs from existing records to prepare for fresh URL population.
     */
    private suspend fun clearUrlsFromExistingRecords() {
        Log.d(TAG, "Clearing URLs from existing records for fresh population...")
        
        val existingRecords = imageDao.getAllImages()
        val recordsWithClearedUrls = existingRecords.map { entity ->
            entity.copy(redirectUrl = null)
        }
        
        imageDao.updateImages(recordsWithClearedUrls)
        Log.d(TAG, "Cleared URLs from ${recordsWithClearedUrls.size} existing records")
        Log.d(TAG, "⚠️ WARNING: UI will show empty images until fresh URLs are populated!")
    }

    /**
     * Populates each record with a redirect URL by calling the GetRedirectUrlUseCase.
     * Records are processed in batches asynchronously for better performance and resource management.
     * @param insertedIds List of record IDs to populate
     * @param batchSize Number of records to process concurrently in each batch (default: 20)
     */
    @RequiresPermission(value = "android.permission.POST_NOTIFICATIONS")
    private suspend fun populateRedirectUrls(insertedIds: List<Long>, batchSize: Int = DEFAULT_BATCH_SIZE) = supervisorScope {
        try {
            // Add overall timeout for the entire population process
            withTimeout(BATCH_TIMEOUT_MS) {
        Log.d(TAG, "Starting to populate redirect URLs for ${insertedIds.size} records in batches of $batchSize...")

        val allResults = mutableListOf<PopulationResult>()
        val indexedIds = insertedIds.mapIndexed { index, id -> index to id }

        // Process records in chunks
        indexedIds.chunked(batchSize).forEachIndexed { batchIndex, batch ->
            // Check for cancellation before processing each batch
            if (!this.isActive) {
                Log.d(TAG, "Worker cancelled during batch processing")
                return@withTimeout
            }
            
            Log.d(TAG, "Processing batch ${batchIndex + 1}/${(insertedIds.size + batchSize - 1) / batchSize} with ${batch.size} records...")

            // Launch async operations for current batch
            val batchResults = batch.map { (originalIndex, id) ->
                async {
                    processRecord(originalIndex, id)
                }
            }

            // Wait for current batch to complete with timeout
            val batchResultsList = try {
                withTimeout(NETWORK_TIMEOUT_MS * 2) { // Double timeout for batch processing
                    batchResults.awaitAll()
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.w(TAG, "Batch ${batchIndex + 1} timed out, cancelling remaining operations")
                batchResults.forEach { it.cancel() }
                batch.map { (originalIndex, id) ->
                    PopulationResult.Failure(originalIndex + 1, id, e)
                }
            }
            
            // Extract successful entities for batch update
            val successfulResults = batchResultsList.filterIsInstance<PopulationResult.Success>()
            val failedResults = batchResultsList.filterIsInstance<PopulationResult.Failure>()
            
            val successfulEntities = successfulResults.map { it.entity }
            
            // Perform batch database update
            val finalResults = if (successfulEntities.isNotEmpty()) {
                runCatching {
                    imageDao.updateImages(successfulEntities)
                    Log.d(TAG, "Batch updated ${successfulEntities.size} entities in database with URLs")
                    Log.d(TAG, "✅ URLs populated for batch ${batchIndex + 1} - UI should now show images!")
                    batchResultsList // Keep original results on success
                }.getOrElse { exception ->
                    Log.e(TAG, "Failed to batch update entities", exception)
                    // Convert successful results to failures due to database update failure
                    val updatedResults = successfulResults.map { result ->
                        PopulationResult.Failure(result.recordNumber, result.id, exception)
                    }
                    updatedResults + failedResults // Combine failed updates with original failures
                }
            } else {
                batchResultsList // No successful entities, keep original results
            }
            
            allResults.addAll(finalResults)

            val batchSuccessCount = finalResults.count { it is PopulationResult.Success }
            val batchFailureCount = finalResults.count { it is PopulationResult.Failure }
            
            Log.d(TAG, "Completed batch ${batchIndex + 1}: $batchSuccessCount successful, $batchFailureCount failed. Total processed: ${allResults.size}/${insertedIds.size}")
            
            // Update progress notification with more detailed info
            if (hasNotificationPermission()) {
                val successCount = allResults.count { it is PopulationResult.Success }
                val failureCount = allResults.count { it is PopulationResult.Failure }
                showProgressNotification(allResults.size, insertedIds.size, successCount, failureCount)
            }
        }

        // Count final results
        val successCount = allResults.count { it is PopulationResult.Success }
        val failureCount = allResults.count { it is PopulationResult.Failure }

        Log.d(TAG, "URL population completed: $successCount successful, $failureCount failed")
            }
        } catch (e: Exception) {
            when (e) {
                is kotlinx.coroutines.TimeoutCancellationException -> {
                    Log.e(TAG, "URL population timed out after ${BATCH_TIMEOUT_MS / 1000} seconds", e)
                    throw e
                }
                else -> {
                    Log.e(TAG, "Error during URL population", e)
                    throw e
                }
            }
        }
    }

    /**
     * Processes a single record to get redirect URL without database update.
     * Database updates will be done in batches for better performance.
     * Uses runCatching for functional error handling with retry mechanism.
     * 
     * @param originalIndex The original index of the record (0-based)
     * @param id The database ID of the record
     * @return PopulationResult indicating success or failure
     */
    private suspend fun processRecord(originalIndex: Int, id: Long): PopulationResult {
        var lastException: Throwable? = null
        
        // Retry loop
        repeat(MAX_RETRIES + 1) { attempt ->
            try {
                // Get redirect URL using the use case with timeout
                return withTimeout(NETWORK_TIMEOUT_MS) {
                    getRedirectUrlUseCase()
                        .fold(
                            onSuccess = { redirectUrl ->
                                Log.d(
                                    TAG,
                                    "Fetched redirect URL for record ${originalIndex + 1}/$DEFAULT_TOTAL_RECORDS (ID: $id): $redirectUrl"
                                )
                                
                                // Create entity for batch update
                                val updatedEntity = ImageEntity(
                                    id = id,
                                    redirectUrl = redirectUrl
                                )
                                
                                PopulationResult.Success(originalIndex + 1, id, redirectUrl, updatedEntity)
                            },
                            onFailure = { error ->
                                Log.w(
                                    TAG,
                                    "Failed to get redirect URL for record ${originalIndex + 1}/$DEFAULT_TOTAL_RECORDS (ID: $id): $error"
                                )
                                
                                PopulationResult.Failure(originalIndex + 1, id, error)
                            }
                        )
                }
            } catch (exception: Exception) {
                lastException = exception
                
                when (exception) {
                    is CancellationException -> {
                        Log.d(TAG, "Record processing cancelled for record ${originalIndex + 1}/$DEFAULT_TOTAL_RECORDS (ID: $id)")
                        throw exception // Re-throw cancellation
                    }
                    is kotlinx.coroutines.TimeoutCancellationException -> {
                        if (attempt < MAX_RETRIES) {
                            Log.w(
                                TAG,
                                "Network timeout for record ${originalIndex + 1}/$DEFAULT_TOTAL_RECORDS (ID: $id) after ${NETWORK_TIMEOUT_MS / 1000}s (attempt ${attempt + 1}/${MAX_RETRIES + 1}), retrying..."
                            )
                            delay(RETRY_DELAY_MS)
                            return@repeat
                        } else {
                            Log.w(
                                TAG,
                                "Network timeout for record ${originalIndex + 1}/$DEFAULT_TOTAL_RECORDS (ID: $id) after ${MAX_RETRIES + 1} attempts"
                            )
                            return PopulationResult.Failure(originalIndex + 1, id, exception)
                        }
                    }
                    else -> {
                        if (attempt < MAX_RETRIES) {
                            Log.w(
                                TAG,
                                "Exception while processing record ${originalIndex + 1}/$DEFAULT_TOTAL_RECORDS (ID: $id) (attempt ${attempt + 1}/${MAX_RETRIES + 1}), retrying...",
                                exception
                            )
                            delay(RETRY_DELAY_MS)
                            return@repeat
                        } else {
                            Log.e(
                                TAG,
                                "Exception while processing record ${originalIndex + 1}/$DEFAULT_TOTAL_RECORDS (ID: $id) after ${MAX_RETRIES + 1} attempts",
                                exception
                            )
                            return PopulationResult.Failure(originalIndex + 1, id, exception)
                        }
                    }
                }
            }
        }
        
        // This should never be reached, but just in case
        return PopulationResult.Failure(originalIndex + 1, id, lastException)
    }

    /**
     * Creates a notification channel for sync notifications (Android 8.0+).
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                applicationContext.getString(R.string.sync_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = applicationContext.getString(R.string.sync_notification_channel_description)
            }
            
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Creates ForegroundInfo for the worker.
     */
    private fun createForegroundInfo(progress: Int, max: Int): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.sync_notification_title))
            .setContentText(applicationContext.getString(R.string.sync_notification_progress, progress, max))
            .setSmallIcon(R.drawable.ic_stat_name)
            .setProgress(max, progress, false)
            .setOngoing(true)
            .setSilent(true)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    /**
     * Shows progress notification with current sync status.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showProgressNotification(progress: Int, max: Int, successCount: Int = 0, failureCount: Int = 0) {
        if (hasNotificationPermission()) {
            val contentText = if (successCount > 0 || failureCount > 0) {
                "Processed: $progress/$max (Success: $successCount, Failed: $failureCount)"
            } else {
                applicationContext.getString(R.string.sync_notification_progress, progress, max)
            }
            
            val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(applicationContext.getString(R.string.sync_notification_title))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setProgress(max, progress, false)
                .setOngoing(true)
                .setSilent(true)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    /**
     * Shows completion notification.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showCompletionNotification(success: Boolean) {
        if (hasNotificationPermission()) {
            val message = if (success) {
                applicationContext.getString(R.string.sync_notification_completed)
            } else {
                applicationContext.getString(R.string.sync_notification_failed)
            }

            val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(applicationContext.getString(R.string.sync_notification_title))
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setProgress(0, 0, false)
                .setOngoing(false)
                .setSilent(false)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    /**
     * Checks if the app has notification permission (Android 13+).
     */
    private fun hasNotificationPermission(): Boolean {
        return PermissionUtils.hasNotificationPermission(applicationContext)
    }



    /**
     * Sealed class to represent the result of populating a single record.
     */
    private sealed class PopulationResult {
        data class Success(val recordNumber: Int, val id: Long, val url: String?, val entity: ImageEntity) : PopulationResult()
        data class Failure(val recordNumber: Int, val id: Long, val error: Throwable?) : PopulationResult()
    }

    companion object Companion {
        private const val TAG = "ImageSyncWorker"
        private const val TOTAL_RECORDS = com.elinex.imagestesttask.core.DEFAULT_TOTAL_RECORDS
        private const val DEFAULT_BATCH_SIZE = COLUMN_EXPECTED.plus(1)
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_CHANNEL_ID = "image_sync_channel"
        private const val NETWORK_TIMEOUT_MS = 60_000L // 60 seconds timeout for network operations
        private const val BATCH_TIMEOUT_MS = 300_000L // 5 minutes timeout for entire batch
        private const val MAX_RETRIES = 2 // Maximum number of retries for failed requests
        private const val RETRY_DELAY_MS = 1000L // 1 second delay between retries

        /**
         * Unique work name for this worker to prevent duplicate work.
         */
        fun imageSyncWork() = OneTimeWorkRequestBuilder<ImageSyncWorker>()
            .addTag(TAG)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
    }
}
