package com.elinex.imagestesttask.core

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

/**
 * Custom qualifier annotation for dependency injection of coroutine dispatchers.
 * 
 * This annotation is used with Hilt dependency injection to distinguish between
 * different types of coroutine dispatchers. It allows the application to inject
 * specific dispatchers (IO, Default) based on the use case requirements.
 * 
 * Usage example:
 * ```kotlin
 * @Inject
 * @Dispatcher(AppDispatchers.IO)
 * lateinit var ioDispatcher: CoroutineDispatcher
 * ```
 * 
 * @param appDispatcher The specific dispatcher type to be injected
 * @see AppDispatchers
 */
@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val appDispatcher: AppDispatchers)

/**
 * Enumeration of available coroutine dispatchers in the application.
 * 
 * This enum defines the different types of coroutine dispatchers that can be
 * used throughout the application for managing asynchronous operations.
 * 
 * Available dispatchers:
 * - Default: For CPU-intensive operations and general coroutine execution
 * - IO: For input/output operations like network calls and database access
 * 
 * These dispatchers are configured in the DispatchersModule and can be
 * injected using the @Dispatcher annotation.
 * 
 * @see Dispatcher
 * @see com.elinex.imagestesttask.core.di.DispatchersModule
 */
enum class AppDispatchers {
    Default,
    IO,
}
