package com.elinex.imagestesttask.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

/**
 * Room database for storing image data in the application.
 * 
 * This abstract class defines the database configuration and provides access
 * to the Data Access Objects (DAOs) for database operations. It serves as
 * the central point for all database-related functionality in the application.
 * 
 * Database configuration:
 * - Entities: ImageEntity for storing image information
 * - Version: 1 (initial database version)
 * - Schema export: Disabled for simplicity
 * 
 * Key features:
 * - Provides access to ImageDao for CRUD operations
 * - Defines database name constant for consistent usage
 * - Extends RoomDatabase for Room framework integration
 * 
 * The database is configured through dependency injection and provides
 * a clean abstraction layer for data persistence operations.
 * 
 * @see ImageEntity
 * @see ImageDao
 * @see com.elinex.imagestesttask.database.di.DatabaseModule
 * 
 * @author Elinext Test Task Team
 * @since 1.0.0
 */
@Database(
    entities = [ImageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ImageDatabase : RoomDatabase() {
    
    /**
     * Provides access to the ImageDao for database operations.
     * 
     * This method returns the Data Access Object that contains all the
     * database query methods for image-related operations.
     * 
     * @return ImageDao instance for database operations
     */
    abstract fun imageDao(): ImageDao
    
    companion object {
        /**
         * Database name constant used for database creation and identification.
         * This constant ensures consistent database naming across the application.
         */
        const val DATABASE_NAME = "image_database"
    }
}
