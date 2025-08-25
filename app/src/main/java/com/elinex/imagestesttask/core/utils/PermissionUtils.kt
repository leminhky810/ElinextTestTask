package com.elinex.imagestesttask.core.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Utility class for handling permissions, specifically notification permissions.
 * 
 * This object provides a centralized way to handle permission-related operations
 * throughout the application, with special focus on notification permissions
 * that were introduced in Android 13 (API 33).
 * 
 * Key features:
 * - Version-aware permission checking
 * - Notification permission handling for Android 13+
 * - Backward compatibility for older Android versions
 * - Clean API for permission-related operations
 * 
 * The utility follows Android best practices for permission handling and
 * provides a consistent interface regardless of the target Android version.
 * 
 * @author Elinext Test Task Team
 * @since 1.0.0
 */
object PermissionUtils {

    /**
     * Checks if the app has notification permission.
     * 
     * This method provides version-aware permission checking:
     * - For Android 13+ (API 33+): Checks POST_NOTIFICATIONS permission
     * - For older versions: Always returns true as permission is not required
     * 
     * The method uses ContextCompat for consistent behavior across different
     * Android versions and handles the permission check safely.
     *
     * @param context The application context for permission checking
     * @return true if notification permission is granted or not required, false otherwise
     * 
     * @see android.Manifest.permission.POST_NOTIFICATIONS
     * @see androidx.core.content.ContextCompat.checkSelfPermission
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Permission not required for Android 12 and below
            true
        }
    }

    /**
     * Checks if notification permission is required for the current Android version.
     * 
     * This method determines whether the application needs to request
     * notification permission based on the current Android version.
     * Notification permissions were introduced in Android 13 (API 33).
     *
     * @return true if notification permission is required (Android 13+), false otherwise
     * 
     * @see android.os.Build.VERSION_CODES.TIRAMISU
     */
    fun isNotificationPermissionRequired(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    /**
     * Gets the notification permission name for the current Android version.
     * 
     * This method returns the appropriate permission name based on the
     * current Android version. For Android 13+, it returns POST_NOTIFICATIONS.
     * For older versions, it returns null since the permission is not required.
     *
     * @return The permission name (POST_NOTIFICATIONS) or null if not required
     * 
     * @see android.Manifest.permission.POST_NOTIFICATIONS
     */
    fun getNotificationPermissionName(): String? {
        return if (isNotificationPermissionRequired()) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            null
        }
    }
}
