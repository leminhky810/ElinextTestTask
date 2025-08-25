package com.elinex.imagestesttask.domain.di

import com.elinex.imagestesttask.domain.repository.ImageRepository
import com.elinex.imagestesttask.domain.usecase.GetRedirectUrlUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    fun providesGetRedirectUrlUseCase(
        imageRepository: ImageRepository
    ) = GetRedirectUrlUseCase(
        imageRepository
    )

}