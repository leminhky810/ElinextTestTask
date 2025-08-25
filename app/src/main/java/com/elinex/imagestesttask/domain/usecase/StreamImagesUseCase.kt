package com.elinex.imagestesttask.domain.usecase

import com.elinex.imagestesttask.domain.model.ImageModel
import com.elinex.imagestesttask.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StreamImagesUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    
    operator fun invoke(): Flow<List<ImageModel>> {
        return imageRepository.streamImages()
    }
}
