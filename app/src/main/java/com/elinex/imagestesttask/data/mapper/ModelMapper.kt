package com.elinex.imagestesttask.data.mapper

import com.elinex.imagestesttask.database.ImageEntity as LocalImageEntity
import com.elinex.imagestesttask.domain.model.ImageModel

object ModelMapper {
    
    // Domain Model to Local Entity
    fun ImageModel.toLocalEntity(): LocalImageEntity {
        return LocalImageEntity(
            id = this.id,
            redirectUrl = this.url
        )
    }
    
    // Local Entity to Domain Model
    fun LocalImageEntity.toDomainModel(): ImageModel {
        return ImageModel(
            id = this.id,
            url = this.redirectUrl
        )
    }
    
    // Extension functions for lists
    fun List<LocalImageEntity>.toDomainModels(): List<ImageModel> {
        return this.map { it.toDomainModel() }
    }
    
    fun List<ImageModel>.toLocalEntities(): List<LocalImageEntity> {
        return this.map { it.toLocalEntity() }
    }
}