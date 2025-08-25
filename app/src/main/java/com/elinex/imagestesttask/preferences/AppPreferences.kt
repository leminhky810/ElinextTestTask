package com.elinex.imagestesttask.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Service for managing user preferences using DataStore.
 * Provides type-safe access to stored preferences.
 */
class AppPreferences(
    private val dataStore: DataStore<Preferences>
) {

    /**
     * Checks if this is the first time the app is launched.
     * @return Flow<Boolean> - true if first launch, false otherwise
     */
    fun isFirstLaunch(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[FIRST_LAUNCH_KEY] ?: true // Default to true if not set
        }
    }

    /**
     * Marks the first launch as completed.
     * This should be called after the user completes onboarding or first-time setup.
     */
    suspend fun markFirstLaunchCompleted() {
        dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH_KEY] = false
        }
    }

    /**
     * Resets the first launch flag (useful for testing or re-enabling onboarding).
     */
    suspend fun resetFirstLaunch() {
        dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH_KEY] = true
        }
    }

    /**
     * Clears all preferences (useful for logout or reset functionality).
     */
    suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        /**
         * Key for storing first launch status.
         * Private to ensure type safety and prevent external access.
         */
        private val FIRST_LAUNCH_KEY = booleanPreferencesKey("first_launch")
        
        /**
         * DataStore file name.
         */
        const val PREFERENCES_NAME = "app_preferences"
    }
}
