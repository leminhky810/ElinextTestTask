package com.elinex.imagestesttask.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for image database operations.
 * 
 * This interface defines all the database operations for the images table.
 * It provides a clean abstraction layer for database queries and operations,
 * following Room framework conventions and best practices.
 * 
 * Key features:
 * - CRUD operations for individual images and image lists
 * - Reactive data streaming with Flow
 * - Conflict resolution strategies for data integrity
 * - Suspending functions for asynchronous operations
 * 
 * The DAO is used by the repository layer to perform database operations
 * and provides type-safe database access with compile-time query validation.
 * 
 * @see ImageEntity
 * @see ImageDatabase
 * @see com.elinex.imagestesttask.data.repository.ImageRepositoryImpl
 * 
 * @author Elinext Test Task Team
 * @since 1.0.0
 */
@Dao
interface ImageDao {
    
    /**
     * Retrieves all images from the database.
     * 
     * @return List of all ImageEntity objects in the database
     */
    @Query("SELECT * FROM images")
    fun getAllImages(): List<ImageEntity>

    /**
     * Streams all images from the database as a Flow.
     * 
     * This method provides reactive updates when the database content changes,
     * allowing the UI to automatically update when new images are added or
     * existing ones are modified.
     * 
     * @return Flow of ImageEntity lists that updates when database changes
     */
    @Query("SELECT * FROM images")
    fun streamImages(): Flow<List<ImageEntity>>
    
    /**
     * Retrieves a specific image by its ID.
     * 
     * @param id The unique identifier of the image to retrieve
     * @return The ImageEntity if found, null otherwise
     */
    @Query("SELECT * FROM images WHERE id = :id")
    suspend fun getImageById(id: Int): ImageEntity?
    
    /**
     * Inserts a single image into the database.
     * 
     * Uses REPLACE conflict strategy to handle duplicate entries.
     * 
     * @param image The ImageEntity to insert
     * @return The row ID of the inserted image
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ImageEntity): Long
    
    /**
     * Inserts multiple images into the database.
     * 
     * Uses REPLACE conflict strategy to handle duplicate entries.
     * 
     * @param images List of ImageEntity objects to insert
     * @return List of row IDs for the inserted images
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<ImageEntity>): List<Long>
    
    /**
     * Updates an existing image in the database.
     * 
     * @param image The ImageEntity to update
     */
    @Update
    suspend fun updateImage(image: ImageEntity)
    
    /**
     * Updates multiple images in the database.
     * 
     * @param images List of ImageEntity objects to update
     */
    @Update
    suspend fun updateImages(images: List<ImageEntity>)
    
    /**
     * Deletes a specific image from the database.
     * 
     * @param image The ImageEntity to delete
     */
    @Delete
    suspend fun deleteImage(image: ImageEntity)
    
    /**
     * Deletes all images from the database.
     * 
     * This method clears the entire images table.
     */
    @Query("DELETE FROM images")
    suspend fun deleteAllImages()
}
