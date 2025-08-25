package com.elinex.imagestesttask.core.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for [PermissionUtils]
 */
class PermissionUtilsTest {

    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        mockContext = mockk()
        mockkStatic(ContextCompat::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `hasNotificationPermission should return true when permission is granted`() {
        // Given
        every { 
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.POST_NOTIFICATIONS) 
        } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = PermissionUtils.hasNotificationPermission(mockContext)

        // Then
        // The result depends on the actual Android version running the test
        // We can only test the logic when permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            assertTrue(result)
        } else {
            assertTrue(result) // Always true for Android 12 and below
        }
    }

    @Test
    fun `hasNotificationPermission should return false when permission is denied on Android 13+`() {
        // Given
        every { 
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.POST_NOTIFICATIONS) 
        } returns PackageManager.PERMISSION_DENIED

        // When
        val result = PermissionUtils.hasNotificationPermission(mockContext)

        // Then
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            assertFalse(result)
        } else {
            assertTrue(result) // Always true for Android 12 and below
        }
    }

    @Test
    fun `isNotificationPermissionRequired should return correct value based on Android version`() {
        // When
        val result = PermissionUtils.isNotificationPermissionRequired()

        // Then
        val expected = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        assertEquals(expected, result)
    }

    @Test
    fun `getNotificationPermissionName should return correct value based on Android version`() {
        // When
        val result = PermissionUtils.getNotificationPermissionName()

        // Then
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            assertEquals(Manifest.permission.POST_NOTIFICATIONS, result)
        } else {
            assertNull(result)
        }
    }

    @Test
    fun `getNotificationPermissionName should return correct permission string`() {
        // When
        val result = PermissionUtils.getNotificationPermissionName()

        // Then
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            assertEquals("android.permission.POST_NOTIFICATIONS", result)
        } else {
            assertNull(result)
        }
    }

    @Test
    fun `permission behavior should be consistent across multiple calls`() {
        // Given
        every { 
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.POST_NOTIFICATIONS) 
        } returns PackageManager.PERMISSION_GRANTED

        // When
        val result1 = PermissionUtils.hasNotificationPermission(mockContext)
        val result2 = PermissionUtils.hasNotificationPermission(mockContext)
        val result3 = PermissionUtils.isNotificationPermissionRequired()
        val result4 = PermissionUtils.getNotificationPermissionName()

        // Then
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            assertTrue(result1)
            assertTrue(result2)
            assertTrue(result3)
            assertEquals(Manifest.permission.POST_NOTIFICATIONS, result4)
        } else {
            assertTrue(result1)
            assertTrue(result2)
            assertFalse(result3)
            assertNull(result4)
        }
    }

    @Test
    fun `POST_NOTIFICATIONS permission constant should be correct`() {
        // When
        val permission = Manifest.permission.POST_NOTIFICATIONS

        // Then
        assertEquals("android.permission.POST_NOTIFICATIONS", permission)
    }
}
