package com.elinex.imagestesttask.domain.usecase

import com.elinex.imagestesttask.core.AppDispatchers
import com.elinex.imagestesttask.core.Dispatcher
import com.elinex.imagestesttask.domain.model.ImageModel
import com.elinex.imagestesttask.domain.repository.ImageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddImageUseCase @Inject constructor(
    private val getRedirectUrlUseCase: GetRedirectUrlUseCase,
    private val imageRepository: ImageRepository,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Fetches a redirect URL and adds it to the database
     * @return Result<Long> of the inserted row ID, or failure
     */
    suspend operator fun invoke() = withContext(ioDispatcher) {
        val emptyRecord = ImageModel(
            id = 0,
            url = null
        )
        val insertedRecord = imageRepository.addImage(emptyRecord)
        getRedirectUrlUseCase().fold(
            onSuccess = { redirectUrl ->
                try {
                    val insertedId = imageRepository.addImage(
                        insertedRecord.copy(
                            url = redirectUrl
                        )
                    )
                    Result.success(insertedId)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
}
