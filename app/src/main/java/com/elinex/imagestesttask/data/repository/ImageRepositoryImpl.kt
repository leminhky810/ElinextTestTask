package com.elinex.imagestesttask.data.repository

import com.elinex.imagestesttask.database.ImageDao
import com.elinex.imagestesttask.data.mapper.ModelMapper.toDomainModels
import com.elinex.imagestesttask.data.mapper.ModelMapper.toLocalEntity
import com.elinex.imagestesttask.domain.model.ImageModel
import com.elinex.imagestesttask.domain.repository.ImageRepository
import com.elinex.imagestesttask.network.RedirectService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val imageDao: ImageDao,
    private val redirectService: RedirectService
) : ImageRepository {

    override fun streamImages(): Flow<List<ImageModel>> {
        return imageDao.streamImages().map { entities ->
            entities.toDomainModels()
        }
    }
    
    override suspend fun addImage(imageModel: ImageModel): ImageModel {
        val entity = imageModel.toLocalEntity()
        val insertedId = imageDao.insertImage(entity)
        return imageModel.copy(id = insertedId)
    }

    override suspend fun getRedirectUrl(sourceUrl: String?): Result<String> {
        return redirectService.getRedirectUrl()
    }
    
}