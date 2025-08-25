package com.elinex.imagestesttask.domain.usecase

import com.elinex.imagestesttask.domain.repository.ImageRepository
import javax.inject.Inject

/**
 * Use case to get the redirected URL from a source URL
 * Specifically handles HTTP 302 redirects by capturing the Location header
 */
class GetRedirectUrlUseCase(
    private val imageRepository: ImageRepository
) {
    
    /**
     * Executes the use case to get redirect URL from picsum.photos
     * @return Result<String> containing the redirected URL or failure
     */
    suspend operator fun invoke(): Result<String> {
        return imageRepository.getRedirectUrl()
    }
}
