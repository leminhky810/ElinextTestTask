package com.elinex.imagestesttask.core

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for [AppDispatchers]
 */
class AppDispatchersTest {

    @Test
    fun `AppDispatchers should have Default and IO values`() {
        val values = AppDispatchers.entries.toTypedArray()
        assertEquals(2, values.size)
        assertTrue(values.contains(AppDispatchers.Default))
        assertTrue(values.contains(AppDispatchers.IO))
    }

    @Test
    fun `AppDispatchers Default should be first enum value`() {
        assertEquals(AppDispatchers.Default, AppDispatchers.values()[0])
    }

    @Test
    fun `AppDispatchers IO should be second enum value`() {
        assertEquals(AppDispatchers.IO, AppDispatchers.values()[1])
    }

    @Test
    fun `AppDispatchers should have correct ordinal values`() {
        assertEquals(0, AppDispatchers.Default.ordinal)
        assertEquals(1, AppDispatchers.IO.ordinal)
    }

    @Test
    fun `AppDispatchers should have correct names`() {
        assertEquals("Default", AppDispatchers.Default.name)
        assertEquals("IO", AppDispatchers.IO.name)
    }

    @Test
    fun `AppDispatchers valueOf should work correctly`() {
        assertEquals(AppDispatchers.Default, AppDispatchers.valueOf("Default"))
        assertEquals(AppDispatchers.IO, AppDispatchers.valueOf("IO"))
    }
}
