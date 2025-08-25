package com.elinex.imagestesttask.domain.repository

import com.elinex.imagestesttask.domain.model.ImageModel
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    
    fun streamImages(): Flow<List<ImageModel>>

    suspend fun addImage(imageModel: ImageModel): ImageModel

    /**
     * Gets the redirected URL from a source URL
     * Handles HTTP 302 redirects by capturing the Location header
     * @param sourceUrl The original URL to follow
     * @return Result<String> containing the redirected URL or failure
     */
    suspend fun getRedirectUrl(sourceUrl: String? = null): Result<String>
}
