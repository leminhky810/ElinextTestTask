package com.elinex.imagestesttask.database.di

import android.content.Context
import androidx.room.Room
import com.elinex.imagestesttask.database.ImageDao
import com.elinex.imagestesttask.database.ImageDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideImageDatabase(
        @ApplicationContext context: Context
    ): ImageDatabase {
        return Room.databaseBuilder(
            context,
            ImageDatabase::class.java,
            ImageDatabase.Companion.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideImageDao(database: ImageDatabase): ImageDao {
        return database.imageDao()
    }
}