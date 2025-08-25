package com.elinex.imagestesttask.domain.model

/**
 * Domain model representing an image in the application.
 * 
 * This data class encapsulates the core information about an image:
 * - Unique identifier for the image
 * - URL for loading the image content
 * 
 * The ImageModel is used throughout the application as the primary
 * representation of image data in the domain layer, following clean
 * architecture principles.
 * 
 * @param id Unique identifier for the image (Long value)
 * @param url Optional URL string for loading the image content
 * 
 * @author Elinext Test Task Team
 * @since 1.0.0
 */
data class ImageModel(
    val id: Long,
    val url: String?
)


