package com.elinex.imagestesttask.core

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for [Constants]
 */
class ConstantsTest {

    @Test
    fun `IMAGE_BATCH_SIZE should be 7`() {
        assertEquals(7, COLUMN_EXPECTED)
    }

    @Test
    fun `DEFAULT_TOTAL_RECORDS should be 140`() {
        assertEquals(140, DEFAULT_TOTAL_RECORDS)
    }

    @Test
    fun `IMAGE_BATCH_SIZE should be positive`() {
        assertTrue(COLUMN_EXPECTED > 0)
    }

    @Test
    fun `DEFAULT_TOTAL_RECORDS should be positive`() {
        assertTrue(DEFAULT_TOTAL_RECORDS > 0)
    }

    @Test
    fun `DEFAULT_TOTAL_RECORDS should be greater than IMAGE_BATCH_SIZE`() {
        assertTrue(DEFAULT_TOTAL_RECORDS > COLUMN_EXPECTED)
    }
}
