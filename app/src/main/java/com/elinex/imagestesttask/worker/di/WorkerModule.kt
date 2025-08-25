package com.elinex.imagestesttask.worker.di

import android.content.Context
import androidx.work.WorkManager
import com.elinex.imagestesttask.worker.ImageSyncInitializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    @Provides
    @Singleton
    fun providesImageDatabaseInitializer(
        @ApplicationContext context: Context
    ) = ImageSyncInitializer(context)

}