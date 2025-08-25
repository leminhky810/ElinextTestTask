package com.elinex.imagestesttask.core

/**
 * Application-wide constants used throughout the Images Test Task application.
 * 
 * This file contains all the constant values that define the application's
 * behavior, layout configurations, and data management parameters.
 * 
 * These constants are used for:
 * - Grid layout configuration
 * - Pagination settings
 * - UI spacing and decoration
 * - Data management limits
 */

/**
 * Total number of image records available in the system.
 * This constant defines the maximum number of images that can be loaded
 * and displayed in the application.
 */
const val DEFAULT_TOTAL_RECORDS = 140

/**
 * Number of columns in the grid layout for image display.
 * This defines the horizontal layout of images in the RecyclerView grid.
 */
const val COLUMN_EXPECTED = 7 // Horizontal

/**
 * Number of rows in the grid layout for image display.
 * This defines the vertical layout of images in the RecyclerView grid.
 */
const val ROW_EXPECTED = 10  // Horizontal

/**
 * Total number of items displayed per page in the grid.
 * Calculated as the product of columns and rows for pagination purposes.
 */
const val ITEMS_PER_PAGE = COLUMN_EXPECTED * ROW_EXPECTED