package com.elinex.imagestesttask.data.mapper

import com.elinex.imagestesttask.database.ImageEntity
import com.elinex.imagestesttask.domain.model.ImageModel
import com.elinex.imagestesttask.data.mapper.ModelMapper.toDomainModel
import com.elinex.imagestesttask.data.mapper.ModelMapper.toDomainModels
import com.elinex.imagestesttask.data.mapper.ModelMapper.toLocalEntity
import com.elinex.imagestesttask.data.mapper.ModelMapper.toLocalEntities
import org.junit.Assert.*
import org.junit.Test

class ModelMapperTest {

    @Test
    fun `toLocalEntity maps ImageModel to ImageEntity correctly`() {
        // Given
        val imageModel = ImageModel(
            id = 1L,
            url = "https://example.com/image.jpg"
        )

        // When
        val result = imageModel.toLocalEntity()

        // Then
        assertEquals(imageModel.id, result.id)
        assertEquals(imageModel.url, result.redirectUrl)
    }

    @Test
    fun `toLocalEntity maps ImageModel with null url correctly`() {
        // Given
        val imageModel = ImageModel(
            id = 2L,
            url = null
        )

        // When
        val result = imageModel.toLocalEntity()

        // Then
        assertEquals(imageModel.id, result.id)
        assertNull(result.redirectUrl)
    }

    @Test
    fun `toDomainModel maps ImageEntity to ImageModel correctly`() {
        // Given
        val imageEntity = ImageEntity(
            id = 3L,
            redirectUrl = "https://example.com/redirected.jpg"
        )

        // When
        val result = imageEntity.toDomainModel()

        // Then
        assertEquals(imageEntity.id, result.id)
        assertEquals(imageEntity.redirectUrl, result.url)
    }

    @Test
    fun `toDomainModel maps ImageEntity with null redirectUrl correctly`() {
        // Given
        val imageEntity = ImageEntity(
            id = 4L,
            redirectUrl = null
        )

        // When
        val result = imageEntity.toDomainModel()

        // Then
        assertEquals(imageEntity.id, result.id)
        assertNull(result.url)
    }

    @Test
    fun `toDomainModels maps list of ImageEntity to list of ImageModel correctly`() {
        // Given
        val imageEntities = listOf(
            ImageEntity(id = 1L, redirectUrl = "https://example.com/image1.jpg"),
            ImageEntity(id = 2L, redirectUrl = "https://example.com/image2.jpg"),
            ImageEntity(id = 3L, redirectUrl = null)
        )

        // When
        val result = imageEntities.toDomainModels()

        // Then
        assertEquals(3, result.size)
        assertEquals(1L, result[0].id)
        assertEquals("https://example.com/image1.jpg", result[0].url)
        assertEquals(2L, result[1].id)
        assertEquals("https://example.com/image2.jpg", result[1].url)
        assertEquals(3L, result[2].id)
        assertNull(result[2].url)
    }

    @Test
    fun `toDomainModels returns empty list when input is empty`() {
        // Given
        val imageEntities = emptyList<ImageEntity>()

        // When
        val result = imageEntities.toDomainModels()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toLocalEntities maps list of ImageModel to list of ImageEntity correctly`() {
        // Given
        val imageModels = listOf(
            ImageModel(id = 1L, url = "https://example.com/image1.jpg"),
            ImageModel(id = 2L, url = "https://example.com/image2.jpg"),
            ImageModel(id = 3L, url = null)
        )

        // When
        val result = imageModels.toLocalEntities()

        // Then
        assertEquals(3, result.size)
        assertEquals(1L, result[0].id)
        assertEquals("https://example.com/image1.jpg", result[0].redirectUrl)
        assertEquals(2L, result[1].id)
        assertEquals("https://example.com/image2.jpg", result[1].redirectUrl)
        assertEquals(3L, result[2].id)
        assertNull(result[2].redirectUrl)
    }

    @Test
    fun `toLocalEntities returns empty list when input is empty`() {
        // Given
        val imageModels = emptyList<ImageModel>()

        // When
        val result = imageModels.toLocalEntities()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `mapping round trip preserves data correctly`() {
        // Given
        val originalModel = ImageModel(
            id = 5L,
            url = "https://example.com/original.jpg"
        )

        // When
        val entity = originalModel.toLocalEntity()
        val resultModel = entity.toDomainModel()

        // Then
        assertEquals(originalModel.id, resultModel.id)
        assertEquals(originalModel.url, resultModel.url)
    }
}
