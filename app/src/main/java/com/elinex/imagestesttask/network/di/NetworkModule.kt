package com.elinex.imagestesttask.network.di

import android.content.Context
import coil3.ImageLoader
import com.elinex.imagestesttask.core.AppDispatchers
import com.elinex.imagestesttask.core.Dispatcher
import com.elinex.imagestesttask.network.RedirectService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .followRedirects(false) // Important: Don't follow redirects automatically
            .followSslRedirects(false) // Important: Don't follow SSL redirects automatically
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRedirectService(
        @Dispatcher(AppDispatchers.IO) ioDispatcher: CoroutineDispatcher,
        okHttpClient: OkHttpClient
    ): RedirectService {
        return RedirectService(ioDispatcher, okHttpClient)
    }
}
